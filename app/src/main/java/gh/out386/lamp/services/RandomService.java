package gh.out386.lamp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import gh.out386.lamp.MainActivity;
import gh.out386.lamp.R;
import gh.out386.lamp.RandomRunnable;
import gh.out386.lamp.RequestRunnable;
import gh.out386.lamp.Utils;

public class RandomService extends Service {
    private final IBinder binder = new LocalBinder();
    private NotificationCompat.Builder notificationBuilder;
    private MutableLiveData<Integer> red = new MutableLiveData<>();
    private MutableLiveData<Integer> green = new MutableLiveData<>();
    private MutableLiveData<Integer> blue = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRandomStarted = new MutableLiveData<>();
    private RandomRunnable randomRunnable;
    private Thread randomThread;
    private PowerManager.WakeLock wakeLock;
    /**
     * Used because white is not touched here. The server will set white to 0 if the URL doesn't have a white.
     */
    private int white;

    public RandomService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public class LocalBinder extends Binder {
        public RandomService getService() {
            return RandomService.this;
        }
    }

    private void foregroundify() {
        final String CHANNEL_ID = "channelStandard";
        if (notificationBuilder == null)
            notificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .setContentTitle(getString(R.string.notif_running))
                .setContentText(getString(R.string.notif_subtext))
                .setSmallIcon(R.drawable.ic_notif_lightbulb)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setTicker(getString(R.string.notif_running));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_NAME = getString(R.string.notif_channel_name);
            final String CHANNEL_DESC = getString(R.string.notif_channel_desc);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(false);
            channel.enableVibration(false);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }

        startForeground(1, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void startRandom(int initialWhite) {
        foregroundify();
        setupWakelock();
        white = initialWhite;
        if (randomRunnable == null) {
            randomRunnable = new RandomRunnable((r, g, b) -> {
                red.postValue(r);
                green.postValue(g);
                blue.postValue(b);
                adjustRgb(r, g, b);
            });
        } else {
            randomRunnable.start();
        }
        if (randomThread == null)
            randomThread = new Thread(randomRunnable);
        randomThread.start();
        isRandomStarted.setValue(true);
    }

    private void setupWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "RandomServiceWakelock");
        wakeLock.acquire();
    }

    public void stopRandom() {
        stopRunnable();
        stopForeground(true);
        stopSelf();
    }

    private void stopRunnable() {
        if (randomRunnable != null)
            randomRunnable.stop();
        isRandomStarted.setValue(false);
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }

    private void adjustRgb(int r, int g, int b) {
        String url = Utils.buildUrlRgb(r, g, b, white);
        // The HTTP requests should only be made once every 40s or thereabouts, to avoid flooding the server
        // Relying on the RandomRunnable to set the delay
        // The app is for use on local networks, so this should complete fast without errors.
        // No point in having a client-side crossfade over a network with a 80ms ping.
        new Thread(new RequestRunnable(url)).start();
    }

    public MutableLiveData<Integer> getRed() {
        return red;
    }

    public MutableLiveData<Integer> getGreen() {
        return green;
    }

    public MutableLiveData<Integer> getBlue() {
        return blue;
    }

    public MutableLiveData<Boolean> getIsRandomStarted() {
        return isRandomStarted;
    }

    @Override
    public void onDestroy() {
        stopRunnable();
        super.onDestroy();
    }
}
