package gh.out386.lamp;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.SystemClock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gh.out386.lamp.network.RequestRunnable;

/**
 * Created by J on 12/25/2017.
 */

public class RgbViewModel extends ViewModel {
    private MutableLiveData<Integer> red = new MutableLiveData<>();
    private MutableLiveData<Integer> green = new MutableLiveData<>();
    private MutableLiveData<Integer> blue = new MutableLiveData<>();
    private MutableLiveData<Integer> white = new MutableLiveData<>();
    private MutableLiveData<Integer> temp = new MutableLiveData<>();
    private MutableLiveData<Integer> brightness = new MutableLiveData<>();
    private int oR = 0;
    private int oG = 0;
    private int oB = 0;
    private int oW = 0;
    private ExecutorService httpThreadPool;
    private Handler setValuesHandler;
    private Runnable setValuesRunnable;
    private Handler brResetHandler;
    private Runnable brResetRunnable;
    private long lastFireTime;
    private boolean isChangeForTemp = false;
    private final int FIRE_DELAY_MS = 40;
    private String targetIp;

    public RgbViewModel() {
        red.setValue(0);
        green.setValue(0);
        blue.setValue(0);
        white.setValue(0);
        temp.setValue(0);
        brightness.setValue(0);

        httpThreadPool = Executors.newSingleThreadExecutor();
        setValuesHandler = new Handler();
        brResetHandler = new Handler();
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public void setRed(Integer red) {
        this.red.setValue(red);
        adjustRgb();
    }

    public void setGreen(Integer green) {
        this.green.setValue(green);
        adjustRgb();
    }

    public void setBlue(Integer blue) {
        this.blue.setValue(blue);
        adjustRgb();
    }

    public void setWhite(Integer white) {
        this.white.setValue(white);
        adjustRgb();
    }

    public void setBrightness(int brightness) {
        this.brightness.setValue(brightness);
        adjustBrightness();
    }

    public void setTemp(int temp) {
        this.temp.setValue(temp);
        adjustTemp();
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

    public MutableLiveData<Integer> getWhite() {
        return white;
    }

    public MutableLiveData<Integer> getBrightness() {
        return brightness;
    }

    private void adjustRgb() {
        if (isChangeForTemp) {
            isChangeForTemp = false;
            lastFireTime = 0;
        }
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - lastFireTime >= FIRE_DELAY_MS) {
            if (setValuesRunnable != null)
                setValuesHandler.removeCallbacks(setValuesRunnable);
            setValuesRunnable = () -> {
                String url = Utils.buildRgbMessage(
                        Utils.getInt(red), Utils.getInt(green), Utils.getInt(blue), Utils.getInt(white));
                resetBr();
                httpThreadPool.execute(new RequestRunnable(url, targetIp));
            };
            lastFireTime = currentTime;
            setValuesHandler.postDelayed(setValuesRunnable, FIRE_DELAY_MS);
        }
    }

    private void resetBr() {
        oR = Utils.getInt(red);
        oG = Utils.getInt(green);
        oB = Utils.getInt(blue);
        oW = Utils.getInt(white);

        brightness.setValue(100);
    }

    private void adjustTemp() {
        if (!isChangeForTemp) {
            isChangeForTemp = true;
            lastFireTime = 0;
        }
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - lastFireTime >= FIRE_DELAY_MS) {
            if (setValuesRunnable != null)
                setValuesHandler.removeCallbacks(setValuesRunnable);
            setValuesRunnable = () -> {
                TempModel model = Utils.buildHellandTempMessage(
                        Utils.getInt(temp), Utils.getInt(white));
                red.setValue(model.r);
                green.setValue(model.g);
                blue.setValue(model.b);
                resetBr();
                httpThreadPool.execute(new RequestRunnable(model.data, targetIp));
            };
            lastFireTime = currentTime;
            setValuesHandler.postDelayed(setValuesRunnable, FIRE_DELAY_MS);
        }
    }

    private void adjustBrightness() {
        if (oR == 0 && oG == 0 && oB == 0 && oW == 0)
            return;
        if (isChangeForTemp) {
            isChangeForTemp = false;
            lastFireTime = 0;
        }
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - lastFireTime >= FIRE_DELAY_MS) {
            if (setValuesRunnable != null)
                setValuesHandler.removeCallbacks(setValuesRunnable);
            setValuesRunnable = () -> {
                int tR, tG, tB, tW;
                int brightness = Utils.getInt(this.brightness);
                tR = (int) (oR * (brightness / 100f));
                tG = (int) (oG * (brightness / 100f));
                tB = (int) (oB * (brightness / 100f));
                tW = (int) (oW * (brightness / 100f));
                tR = tR > 1023 ? 1023 : tR;
                tG = tG > 1023 ? 1023 : tG;
                tB = tB > 1023 ? 1023 : tB;
                tW = tW > 1023 ? 1023 : tW;
                red.setValue(tR);
                green.setValue(tG);
                blue.setValue(tB);
                white.setValue(tW);

                // Will allow increase over 100%, but snap back to 100 when done
                if (brightness > 100) {
                    if (brResetRunnable != null)
                        brResetHandler.removeCallbacks(brResetRunnable);
                    brResetRunnable = this::resetBr;
                    brResetHandler.postDelayed(brResetRunnable, 1000);
                }
                String url = Utils.buildRgbMessage(tR, tG, tB, tW);
                httpThreadPool.execute(new RequestRunnable(url, targetIp));
            };
            lastFireTime = currentTime;
            setValuesHandler.postDelayed(setValuesRunnable, FIRE_DELAY_MS);
        }
    }

}
