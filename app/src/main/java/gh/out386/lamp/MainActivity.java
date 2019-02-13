package gh.out386.lamp;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;

import com.sdsmdg.harjot.crollerTest.Croller;

import gh.out386.lamp.network.DiscoverSsdp;
import gh.out386.lamp.services.RandomService;

import static gh.out386.lamp.Utils.GET_ENDPOINT;

public class MainActivity extends AppCompatActivity {
    private String targetIp;
    private SeekBar redSeek;
    private SeekBar greenSeek;
    private SeekBar blueSeek;
    private SeekBar whiteSeek;
    private Croller tempSeek;
    private Croller brSeek;
    private Switch randomSwitch;
    private boolean isRgbChanged = false;
    private boolean isSeekChanging = false;
    private boolean isTempChanged = false;
    private Handler preventSeekHandler;
    private Runnable preventSeekRunnable;
    private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private RgbViewModel rgbViewModel;
    private RandomService randomService;
    private boolean randomBound = false;
    private MutableLiveData<Integer> randomRed;
    private MutableLiveData<Integer> randomGreen;
    private MutableLiveData<Integer> randomBlue;
    private MutableLiveData<Boolean> randomRunning;
    private ServiceConnection randomServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RandomService.LocalBinder binder = (RandomService.LocalBinder) service;
            randomService = binder.getService();
            randomRed = randomService.getRed();
            randomGreen = randomService.getGreen();
            randomBlue = randomService.getBlue();
            randomRunning = randomService.getIsRandomStarted();
            randomBound = true;
            setupRandomObservers();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            randomBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        targetIp = getIntent().getStringExtra(ScanActivity.IP_ADDR);
        if (targetIp == null) {
            //TODO: Start screaming. Loudly.
            Log.i("MainActivity", "onCreate: Target IP is null");
            finish();
        }

        redSeek = findViewById(R.id.redScroller);
        greenSeek = findViewById(R.id.greenScroller);
        blueSeek = findViewById(R.id.blueScroller);
        whiteSeek = findViewById(R.id.whiteScroller);
        tempSeek = findViewById(R.id.tempScroller);
        brSeek = findViewById(R.id.brScroller);
        randomSwitch = findViewById(R.id.randomSwitch);
        brSeek.setProgress(100);
        rgbViewModel = ViewModelProviders.of(this).get(RgbViewModel.class);
        rgbViewModel.setTargetIp(targetIp); // This app is getting way too hacky now. Better just use a Factory.
        preventSeekRunnable = () -> isSeekChanging = false;

        setupSeekbarColours();
        setupLiveData();
        setupObservers();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(finishReceiver, new IntentFilter(GetAsync.ACTION_SERVER_FAIL));
        setupInitialGet();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, RandomService.class);
        bindService(intent, randomServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(randomServiceConnection);
        randomBound = false;
    }

    private void setupInitialGet() {
        GetAsync getAsync = new GetAsync(this);
        MutableLiveData<Integer> initialRed = getAsync.getRed();
        MutableLiveData<Integer> initialGreen = getAsync.getGreen();
        MutableLiveData<Integer> initialBlue = getAsync.getBlue();
        MutableLiveData<Integer> initialWhite = getAsync.getWhite();
        if (initialRed != null)
            initialRed.observe(this, value -> rgbViewModel.setRed(value));
        if (initialGreen != null)
            initialGreen.observe(this, value -> rgbViewModel.setGreen(value));
        if (initialBlue != null)
            initialBlue.observe(this, value -> rgbViewModel.setBlue(value));
        if (initialWhite != null)
            initialWhite.observe(this, value -> rgbViewModel.setWhite(value));
        getAsync.execute(String.format(GET_ENDPOINT, targetIp));
    }

    private void setupSeekbarColours() {
        redSeek.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.redSeek), PorterDuff.Mode.SRC_IN);
        redSeek.getThumb().setColorFilter(
                ContextCompat.getColor(this, R.color.redSeek), PorterDuff.Mode.SRC_IN);
        greenSeek.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.greenSeek), PorterDuff.Mode.SRC_IN);
        greenSeek.getThumb().setColorFilter(
                ContextCompat.getColor(this, R.color.greenSeek), PorterDuff.Mode.SRC_IN);
        blueSeek.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.blueSeek), PorterDuff.Mode.SRC_IN);
        blueSeek.getThumb().setColorFilter(
                ContextCompat.getColor(this, R.color.blueSeek), PorterDuff.Mode.SRC_IN);
        whiteSeek.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.whiteSeek), PorterDuff.Mode.SRC_IN);
        whiteSeek.getThumb().setColorFilter(
                ContextCompat.getColor(this, R.color.whiteSeek), PorterDuff.Mode.SRC_IN);
    }

    private void setupLiveData() {
        redSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    rgbViewModel.setRed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        greenSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    rgbViewModel.setGreen(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        blueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    rgbViewModel.setBlue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        whiteSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    rgbViewModel.setWhite(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        brSeek.setOnProgressChangedListener((progress) -> {
            if (progress > brSeek.getMin())
                rgbViewModel.setBrightness(progress);
        });
        tempSeek.setOnProgressChangedListener(progress -> {
            if (progress > tempSeek.getMin())
                rgbViewModel.setTemp(progress);
        });
        randomSwitch.setOnClickListener(v -> {
            if (randomBound) {
                setRandomServiceState(randomSwitch.isChecked());
            }
        });
    }

    private void setupObservers() {
        rgbViewModel.getRed().observe(this, value -> {
            if (value != null)
                setSeek(value, -1, -1, -1, -1);
        });
        rgbViewModel.getGreen().observe(this, value -> {
            if (value != null)
                setSeek(-1, value, -1, -1, -1);
        });
        rgbViewModel.getBlue().observe(this, value -> {
            if (value != null)
                setSeek(-1, -1, value, -1, -1);
        });
        rgbViewModel.getWhite().observe(this, value -> {
            if (value != null)
                setSeek(-1, -1, -1, value, -1);
        });
        rgbViewModel.getBrightness().observe(this, value -> {
            if (value != null && value != brSeek.getProgress())
                setSeek(-1, -1, -1, -1, value);
        });
    }

    private void setupRandomObservers() {
        randomRed.observe(this, value -> {
            if (value != null)
                setSeek(value, -1, -1, -1, -1);
        });
        randomGreen.observe(this, value -> {
            if (value != null)
                setSeek(-1, value, -1, -1, -1);
        });
        randomBlue.observe(this, value -> {
            if (value != null)
                setSeek(-1, -1, value, -1, -1);
        });
        randomRunning.observe(this, value -> {
            if (value == null)
                return;
            if (!value) {
                updateVmValues(
                        randomRed.getValue(), randomGreen.getValue(), randomBlue.getValue(), null);
            }
            setSlidersEnabled(!value);
        });
    }

    private void updateVmValues(Integer red, Integer green, Integer blue, Integer white) {
        rgbViewModel.setRed(red);
        rgbViewModel.setGreen(green);
        rgbViewModel.setBlue(blue);
        if (white != null)
            rgbViewModel.setWhite(white);
    }

    private void setSlidersEnabled(boolean enabled) {
        redSeek.setEnabled(enabled);
        greenSeek.setEnabled(enabled);
        blueSeek.setEnabled(enabled);
        whiteSeek.setEnabled(enabled);
        brSeek.setEnabled(enabled);
        tempSeek.setEnabled(enabled);
        randomSwitch.setChecked(!enabled);
    }

    private void setSeek(int red, int green, int blue, int white, int brightness) {
        if (red > -1)
            redSeek.setProgress(red);
        if (green > -1)
            greenSeek.setProgress(green);
        if (blue > -1)
            blueSeek.setProgress(blue);
        if (white > -1)
            whiteSeek.setProgress(white);
        if (brightness > -1)
            brSeek.setProgress(brightness);
    }

    private void setRandomServiceState(boolean start) {
        if (start) {
            startService(new Intent(getApplicationContext(), RandomService.class));
            randomService.startRandom(whiteSeek.getProgress(), targetIp);
        } else {
            randomService.stopRandom();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(finishReceiver);
    }
}
