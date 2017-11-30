package gh.out386.lamp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.sdsmdg.harjot.crollerTest.Croller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int FIRE_DELAY_MS = 40;
    private Croller redSeek;
    private Croller greenSeek;
    private Croller blueSeek;
    private Croller whiteSeek;
    private Croller tempSeek;
    private ExecutorService httpThreadPool;
    private int red = 0;
    private int green = 0;
    private int blue = 0;
    private int white = 0;
    private int temp = 0;
    private boolean isRgbChanged = false;
    private boolean isTempChanged = false;
    private boolean isSeekChanging = false;
    private boolean isChangeForTemp = false;
    private Handler setValuesHandler;
    private Runnable setValuesRunnable;
    private long lastFireTime;
    private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        httpThreadPool = Executors.newSingleThreadExecutor();
        setValuesHandler = new Handler();
        redSeek = findViewById(R.id.redScroller);
        greenSeek = findViewById(R.id.greenScroller);
        blueSeek = findViewById(R.id.blueScroller);
        whiteSeek = findViewById(R.id.whiteScroller);
        tempSeek = findViewById(R.id.tempScroller);

        redSeek.setOnProgressChangedListener((progress) -> {
            // Prevents issues when listener fires on activity create
            // Assuming all Crollers use the same min
            if (progress > redSeek.getMin())
                isRgbChanged = true;
            if (isRgbChanged && !isSeekChanging) {
                red = progress;
                setRgb();
            }
        });
        greenSeek.setOnProgressChangedListener(progress -> {
            if (progress > redSeek.getMin())
                isRgbChanged = true;
            if (isRgbChanged && !isSeekChanging) {
                green = progress;
                setRgb();
            }
        });
        blueSeek.setOnProgressChangedListener(progress -> {
            if (progress > redSeek.getMin())
                isRgbChanged = true;
            if (isRgbChanged && !isSeekChanging) {
                blue = progress;
                setRgb();
            }
        });
        whiteSeek.setOnProgressChangedListener(progress -> {
            if (progress > redSeek.getMin())
                isRgbChanged = true;
            if (isRgbChanged) {
                white = progress;
                setRgb();
            }
        });
        tempSeek.setOnProgressChangedListener(progress -> {
            if (progress > tempSeek.getMin())
                isTempChanged = true;
            if (isTempChanged) {
                temp = progress;
                setTemp();
            }
        });

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(finishReceiver, new IntentFilter(GetAsync.ACTION_SERVER_FAIL));
        new GetAsync(this, redSeek, greenSeek, blueSeek, whiteSeek)
                .execute(Utils.GET_URL);
    }

    private void setRgb() {
        if (isChangeForTemp) {
            isChangeForTemp = false;
            lastFireTime = 0;
        }
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - lastFireTime >= FIRE_DELAY_MS) {
            if (setValuesRunnable != null)
                setValuesHandler.removeCallbacks(setValuesRunnable);
            setValuesRunnable = () -> {
                String url = Utils.buildUrlRgb(red, green, blue, white);
                httpThreadPool.execute(new RequestRunnable(url));
            };
            lastFireTime = currentTime;
            setValuesHandler.postDelayed(setValuesRunnable, FIRE_DELAY_MS);
        }
    }

    private void setTemp() {
        if (!isChangeForTemp) {
            isChangeForTemp = true;
            lastFireTime = 0;
        }
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - lastFireTime >= FIRE_DELAY_MS) {
            if (setValuesRunnable != null)
                setValuesHandler.removeCallbacks(setValuesRunnable);
            setValuesRunnable = () -> {
                TempModel model = Utils.buildUrlHellandTemp(temp, white);
                red = model.r;
                green = model.g;
                blue = model.b;
                setSeek();
                httpThreadPool.execute(new RequestRunnable(model.url));
            };
            lastFireTime = currentTime;
            setValuesHandler.postDelayed(setValuesRunnable, FIRE_DELAY_MS);
        }
    }

    private void setSeek() {
        isSeekChanging = true;
        redSeek.setProgress(red);
        greenSeek.setProgress(green);
        blueSeek.setProgress(blue);
        new Handler().postDelayed(() -> isSeekChanging = false, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(finishReceiver);
    }
}
