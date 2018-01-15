package gh.out386.lamp;

import android.media.audiofx.Visualizer;
import android.util.Log;
import android.util.Pair;

/**
 * Created by J on 12/2/2017.
 */

public class ProcessMusic {
    final int SLOP = 5;
    final int OFFSET = 5;
    final int MULTIPLIER = 10;
    final int MULTIPLIER_H = 3;
    int dA, dB, dC;
    private Visualizer visualizer;
    private Pair<Integer, Pair<Integer, Integer>> buckets;
    private int range;
    private int maxRange;
    private VisListener visListener;

    public ProcessMusic(VisListener visListener) {
        this.visListener = visListener;
        range = 64;//Visualizer.getCaptureSizeRange()[0];
        maxRange = 200;//Visualizer.getCaptureSizeRange()[1];
        visualizer = new Visualizer(0);
        visualizer.setCaptureSize(maxRange);
        visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        buckets = getBuckets(range / 2 - 2, maxRange / 2 - 2);
        dA = buckets.first;
        dB = buckets.second.first - buckets.first;
        dC = buckets.second.second - buckets.second.first;
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
                Log.i("music", l + "  " + m + "  "+ h);*//*
                int s = 0;
                for (byte t : waveform) {
                    s += t + 128;
                }
                System.out.println(s/128);*/
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                //System.out.print(samplingRate + "ss   " + Arrays.toString(Visualizer.getCaptureSizeRange()) + "bbb ");

                /*System.out.print(fft[0] + " ");
                for (int i = 2; i < fft.length - 1; i+=2) {
                    System.out.print(fft[i] + " ");
                }
                System.out.print(" " + fft[1]);
                System.out.println();*/
                //new Thread(() -> {
                    int amplituides[] = new int[maxRange / 2];
                    System.out.print(Visualizer.getCaptureSizeRange()[1] + " ");
                    for (int i = 1; i < maxRange / 2; i++) {
                        //amplituides[i - 1] = (int) (Math.hypot(fft[i * 2], fft[i * 2 + 1]));
                        amplituides[i - 1] = fft[i * 2] * fft[i * 2] + fft[i * 2 + 1] * fft[i * 2 + 1];
                    }

                    float l, m, h;
                    int dAT = dA;
                    int dBT = dB;
                    int dCT = dC;

                    int temp = 0;
                    for (int i = 0; i < buckets.first; i++) {
                        if (amplituides[i] > SLOP)
                            temp = temp + amplituides[i];
                        else
                            dAT--;
                    }
                    if (dAT > 0)
                        l = temp / dAT;
                    else
                        l = OFFSET;
                    l = (float) Math.sqrt(l);//(50 * Math.log10(l));
                    l = l * MULTIPLIER_H;
                    if (l > 255)
                        l = 255;

                    temp = 0;
                    for (int i = buckets.first; i < buckets.second.first; i++) {
                        if (amplituides[i] > SLOP)
                            temp = temp + amplituides[i];
                        else dBT--;
                    }
                    if (dBT > 0)
                        m = temp / dBT;
                    else
                        m = OFFSET;
                    m = (float) Math.sqrt(m);//(50 * Math.log10(m));
                    m = m * MULTIPLIER;
                    if (m > 255)
                        m = 255;

                    temp = 0;
                    for (int i = buckets.second.first; i < buckets.second.second; i++) {
                        if (amplituides[i] > SLOP)
                            temp = temp + amplituides[i];
                        else dCT--;
                    }
                    if (dCT > 0)
                        h = temp / dCT;
                    else
                        h = OFFSET;
                    h = (float) Math.sqrt(h);//(50 * Math.log10(h));
                    h = h * MULTIPLIER;
                    if (h > 255)
                        h = 255;
                    Log.i("music", (int) l + "  " + (int) m + "  " + (int) h);
                    visListener.OnValue((int) l, (int) m, (int) h);
                   /*for (int amplituide : amplituides) {
                        System.out.print("  " + amplituide);
                    }
                    System.out.println();*/
                //}).start();
            }
        }, 20000, false, true);
        visualizer.setEnabled(true);
    }

    private Pair<Integer, Pair<Integer, Integer>> getBuckets(int range, int max) {
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
        a = by3 /*- (int)(by3 / 2)*/ + addA;
        b = a + by3 /*- by3 / 2*/;
        c = b + by3 + addB /*+ (int)(by3 / 2) + by3 / 2*/;
        Log.i("meh", "getBuckets: " + a + "  " + b + "  " + c + "  " + max);
        return new Pair<>(a, new Pair<>(b, max));
    }

    public void stop() {
        visualizer.setEnabled(false);
        visualizer.release();
    }

    interface VisListener {
        void OnValue(int r, int g, int b);
    }
}