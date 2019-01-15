package gh.out386.lamp;

import android.content.SharedPreferences;
import android.media.audiofx.Visualizer;
import android.util.Log;
import android.util.Pair;

import java.util.concurrent.ArrayBlockingQueue;

import static gh.out386.lamp.MainActivity.BLUE_HIGH_SEEK;
import static gh.out386.lamp.MainActivity.BLUE_LOW_SEEK;
import static gh.out386.lamp.MainActivity.GREEN_HIGH_SEEK;
import static gh.out386.lamp.MainActivity.GREEN_LOW_SEEK;
import static gh.out386.lamp.MainActivity.RED_HIGH_SEEK;
import static gh.out386.lamp.MainActivity.RED_LOW_SEEK;

/**
 * Created by J on 12/2/2017.
 */

public class ProcessMusic {
    private final static int RANGE = 64;
    private final static int MAX_RANGE = 1024;
    private final int SLOP = 5;
    private final int OFFSET = 5;
    private final int MULTIPLIER = 40;
    private final int MULTIPLIER_L = 30;
    private int averageLength;
    private int bucketALow;
    private int bucketAHigh;
    private int bucketBLow;
    private int bucketBHigh;
    private int bucketCLow;
    private int bucketCHigh;
    private Visualizer visualizer;
    private VisListener visListener;
    private ArrayBlockingQueue<Float> averagingQueueL;
    private ArrayBlockingQueue<Float> averagingQueueM;
    private ArrayBlockingQueue<Float> averagingQueueH;
    private float averageL = 0f;
    private float averageM = 0f;
    private float averageH = 0f;
    private SharedPreferences prefs;
    private PrefsListener prefsListener;

    public ProcessMusic(VisListener visListener, int averageLength, SharedPreferences preferences) {
        this.visListener = visListener;
        this.averageLength = averageLength;
        prefs = preferences;
        prefsListener = new PrefsListener();
        visualizer = new Visualizer(0);
        visualizer.setCaptureSize(MAX_RANGE);
        visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        Pair<Integer, Pair<Integer, Integer>> buckets =
                generateBuckets(RANGE, MAX_RANGE / 2 - 2);

        readBuckets(buckets);
        bucketALow = 0;
        bucketAHigh = buckets.first;
        bucketBLow = bucketAHigh;
        bucketBHigh = buckets.second.first - buckets.first;
        bucketCLow = bucketBHigh;
        bucketCHigh = buckets.second.second - buckets.second.first;

        prefs.registerOnSharedPreferenceChangeListener(prefsListener);
        averagingQueueL = new ArrayBlockingQueue<>(averageLength, true);
        averagingQueueM = new ArrayBlockingQueue<>(averageLength, true);
        averagingQueueH = new ArrayBlockingQueue<>(averageLength, true);
        for (int i = 0; i < averageLength; i++) {
            averagingQueueL.offer(-1f);
            averagingQueueM.offer(-1f);
            averagingQueueH.offer(-1f);
        }
    }

    Pair<Integer, Pair<Integer, Integer>> getBuckets() {
        return new Pair<>(bucketAHigh, new Pair<>(bucketBHigh, bucketCHigh));
    }

    private void readBuckets(Pair<Integer, Pair<Integer, Integer>> buckets) {
        bucketALow = prefs.getInt(RED_LOW_SEEK, 0);
        bucketAHigh = prefs.getInt(RED_HIGH_SEEK, buckets.first);
        bucketBLow = prefs.getInt(GREEN_LOW_SEEK, buckets.first);
        bucketBHigh = prefs.getInt(GREEN_HIGH_SEEK, buckets.second.first);
        bucketCLow = prefs.getInt(BLUE_LOW_SEEK, buckets.second.first);
        bucketCHigh = prefs.getInt(BLUE_HIGH_SEEK, buckets.second.second);
    }

    private class PrefsListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (RED_LOW_SEEK.equals(key))
                bucketALow = sharedPreferences.getInt(RED_LOW_SEEK, bucketALow);
            else if (RED_HIGH_SEEK.equals(key))
                bucketAHigh = sharedPreferences.getInt(RED_HIGH_SEEK, bucketAHigh);
            else if (GREEN_LOW_SEEK.equals(key))
                bucketBLow = sharedPreferences.getInt(GREEN_LOW_SEEK, bucketBLow);
            else if (GREEN_HIGH_SEEK.equals(key))
                bucketBHigh = sharedPreferences.getInt(GREEN_HIGH_SEEK, bucketBHigh);
            else if (BLUE_LOW_SEEK.equals(key))
                bucketCLow = sharedPreferences.getInt(BLUE_LOW_SEEK, bucketCLow);
            else if (BLUE_HIGH_SEEK.equals(key))
                bucketCHigh = sharedPreferences.getInt(BLUE_HIGH_SEEK, bucketCHigh);
        }
    }

    public void initVisualizer() {
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                /*int l, m, h;
                int temp = 0;
                for (int i = 0; i < 43; i++)
                    temp = temp + waveform[i];
                l = temp / 43 + 128;
                temp = 0;
                for (int i = 43; i < 85; i++)
                    temp = temp + waveform[i];
                m = temp / 42 + 128;
                temp = 0;
                for (int i = 85; i < 128; i++)
                    temp = temp + waveform[i];
                h = temp / 43 + 128;
                //Log.i("music", l + "  " + m + "  "+ h);
                for (byte t : waveform) {
                    System.out.print((t + 128) + " ");
                }
                System.out.println();*/

            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                //System.out.print(samplingRate + "ss   " + Arrays.toString(Visualizer.getCaptureSizeRange()) + "bbb ");

                /*System.out.printf("%3d : %3d " , fft.length, fft[0]);
                for (int i = 2; i < fft.length - 1; i+=2) {
                    System.out.printf("%3d " ,fft[i]);
                }
                System.out.print(fft[1]);
                System.out.println();*/
                //new Thread(() -> {


                int amplituides[] = new int[MAX_RANGE / 2];
                //System.out.print(Visualizer.getCaptureSizeRange()[1] + " ");
                for (int i = 1; i < MAX_RANGE / 2; i++) {
                    byte rfk = fft[i * 2];
                    byte ifk = fft[i * 2 + 1];
                    //amplituides[i - 1] = (int) (Math.hypot(rfk, ifk));
                    amplituides[i - 1] = rfk * rfk + ifk * ifk;
                }

                float a, b, c;
                int countA = bucketAHigh - bucketALow;
                int countB = bucketBHigh - bucketBLow;
                int countC = bucketCHigh - bucketCLow;

                int temp = 0;
                for (int i = bucketALow; i < bucketAHigh; i++) {
                    if (amplituides[i] > SLOP)
                        temp = temp + amplituides[i];
                    else
                        countA--;
                }
                if (countA > 0)
                    a = temp / countA;
                else
                    a = OFFSET;
                a = 2 * (float) Math.sqrt(a);
                //a = (float) (20 * Math.log10(a));
                a = a * MULTIPLIER_L;
                if (a > 1023)
                    a = 1023;

                temp = 0;
                for (int i = bucketBLow; i < bucketBHigh; i++) {
                    if (amplituides[i] > SLOP)
                        temp = temp + amplituides[i];
                    else countB--;
                }
                if (countB > 0)
                    b = temp / countB;
                else
                    b = OFFSET;
                b = 2 * (float) Math.sqrt(b);
                //b = (float) (20 * Math.log10(b));
                b = b * MULTIPLIER;
                if (b > 1023)
                    b = 1023;

                temp = 0;
                for (int i = bucketCLow; i < bucketCHigh; i++) {
                    if (amplituides[i] > SLOP)
                        temp = temp + amplituides[i];
                    else countC--;
                }
                if (countC > 0)
                    c = temp / countC;
                else
                    c = OFFSET;
                c = 2* (float) Math.sqrt(c);
                //c = (float) (20 * Math.log10(c));
                c = c * MULTIPLIER;
                if (c > 1023)
                    c = 1023;
                sendValue(a, b, c);


                //System.out.println("wtf");
                /*for (int amplituide : amplituides) {
                    //    System.out.printf("%4d ", amplituide > 0 ? Math.round(10 * Math.log10(amplituide)) : 0);
                    System.out.printf("%4d ", amplituide);
                }*/
                System.out.println();
                //}).start();
            }
        }, 20000, false, true);
        visualizer.setEnabled(true);
    }

    private Pair<Integer, Pair<Integer, Integer>> generateBuckets(int range, int max) {
        int a, b, c;
        int by3 = range / 3;
        int diff3;
        int addA = 0;
        int addB = 0;
        if (!(range % 3 == 0)) {
            diff3 = range - by3 * 3;
            int add = diff3 / 2;
            addA = addB = add;
            if (!(diff3 % 2 == 0))
                addA++;
        }
        a = by3 - by3 / 2 + addA;
        b = a + by3 - by3 / 4;
        c = b + by3 + addB /*+ (int)(by3 / 2) + by3 / 2*/;
        Log.i("meh", "getBuckets: " + a + "  " + b + "  " + c + "  max:" + range);
        return new Pair<>(a, new Pair<>(b, max));
    }

    public void stop() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        visualizer.setEnabled(false);
        visualizer.release();
    }

    private void sendValue(float l, float m, float h) {
        float avgL = l / averageLength;
        float avgM = m / averageLength;
        float avgH = h / averageLength;
        Float t;
        if ((t = averagingQueueL.poll()) != null && t > -1)
            averageL -= t;
        if ((t = averagingQueueM.poll()) != null && t > -1)
            averageM -= t;
        if ((t = averagingQueueH.poll()) != null && t > -1)
            averageH -= t;
        averageL += avgL;
        averageM += avgM;
        averageH += avgH;
        averagingQueueL.offer(avgL);
        averagingQueueM.offer(avgM);
        averagingQueueH.offer(avgH);
        System.out.printf("%3d %3d %3d\n", Math.round(averageL), Math.round(averageM), Math.round(averageH));
        visListener.OnValue(Math.round(averageL), Math.round(averageM), Math.round(averageH));
    }

    interface VisListener {
        void OnValue(int r, int g, int b);
    }
}
