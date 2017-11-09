package gh.out386.lamp;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.Switch;

import com.sdsmdg.harjot.crollerTest.Croller;

public class MainActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        redSeek = findViewById(R.id.redScroller);
        greenSeek = findViewById(R.id.greenScroller);
        blueSeek = findViewById(R.id.blueScroller);
        whiteSeek = findViewById(R.id.whiteScroller);
        tempSeek = findViewById(R.id.tempScroller);
        brSeek = findViewById(R.id.brScroller);
        randomSwitch = findViewById(R.id.randomSwitch);
        brSeek.setProgress(100);
        rgbViewModel = ViewModelProviders.of(this).get(RgbViewModel.class);
        preventSeekRunnable = () -> isSeekChanging = false;

        setupSeekbarColours();
        setupLiveData();
        setupObservers();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(finishReceiver, new IntentFilter(GetAsync.ACTION_SERVER_FAIL));
        new GetAsync(this, redSeek, greenSeek, blueSeek, whiteSeek)
                .execute(Utils.GET_URL);
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
        randomSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rgbViewModel.setRandom(isChecked);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(finishReceiver);
    }

    public interface RandomRgb {
        void setRgb(int r, int g, int b);
    }
}
