package gh.out386.lamp;

import android.app.Activity;
import android.os.Bundle;

import com.sdsmdg.harjot.crollerTest.Croller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private ExecutorService httpThreadPool;
    private int red = 0;
    private int green = 0;
    private int blue = 0;
    private int white = 0;
    private int temp = 0;
    private boolean isRgbChanged = false;
    private boolean isTempChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        httpThreadPool = Executors.newSingleThreadExecutor();

        Croller redSeek = findViewById(R.id.redScroller);
        Croller greenSeek = findViewById(R.id.greenScroller);
        Croller blueSeek = findViewById(R.id.blueScroller);
        Croller whiteSeek = findViewById(R.id.whiteScroller);
        Croller tempSeek = findViewById(R.id.tempScroller);

        redSeek.setOnProgressChangedListener((progress) -> {
            // Prevents issues when listener fires on activity create
            // Assuming all Crollers use the same min
            if (progress > redSeek.getMin())
                isRgbChanged = true;
            if (isRgbChanged) {
                red = progress;
                setRgb();
            }
        });
        greenSeek.setOnProgressChangedListener(progress -> {
            if (progress > redSeek.getMin())
                isRgbChanged = true;
            if (isRgbChanged) {
                green = progress;
                setRgb();
            }
        });
        blueSeek.setOnProgressChangedListener(progress -> {
            if (progress > redSeek.getMin())
                isRgbChanged = true;
            if (isRgbChanged) {
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
                setTemp(progress);
            }
        });
    }

    private void setRgb() {
        httpThreadPool.execute(new RequestRunnable(
                Utils.buildUrlRgb(red, green, blue, white)));
    }

    private void setTemp(int temp) {
        httpThreadPool.execute(new RequestRunnable(
                Utils.buildUrlHellandTemp(temp, white)
        ));
    }
}
