package gh.out386.lamp;

import android.media.audiofx.Visualizer;
import android.util.Log;
import android.util.Pair;

/**
 * Created by J on 12/2/2017.
 */

public class ProcessMusic {
    final int SLOP = 5;
    final int OFFSET = 7;
    final int MULTIPLIER = 7;
    int dA, dB, dC;
    private Visualizer visualizer;
    private Pair<Integer, Pair<Integer, Integer>> buckets;
    private int range;

    public ProcessMusic() {
        range = Visualizer.getCaptureSizeRange()[0];
        visualizer = new Visualizer(0);
        visualizer.setCaptureSize(range);
        visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        buckets = getBuckets(range / 2 - 2);
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
                new Thread(() -> {
                    int amplituides[] = new int[range / 2];
                    System.out.print(Visualizer.getMaxCaptureRate());
                    for (int i = 1; i < fft.length / 2; i++) {
                        amplituides[i - 1] = (int) (Math.hypot(fft[i * 2], fft[i * 2 + 1]));
                    }

                    int l, m, h;
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
                        l = temp / dAT + 1;
                    else
                        l = 1;
                    l = l * MULTIPLIER;
                    if (l > 255)
                        l = 255;

                    temp = 0;
                    for (int i = buckets.first; i < buckets.second.first; i++) {
                        if (amplituides[i] > SLOP)
                            temp = temp + amplituides[i];
                        else dBT--;
                    }
                    if (dBT > 0)
                        m = temp / dBT + OFFSET;
                    else
                        m = OFFSET;
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
                        h = temp / dCT + OFFSET;
                    else
                        h = OFFSET;
                    h = h * MULTIPLIER;
                    if (h > 255)
                        h = 255;
                    Log.i("music", l + "  " + m + "  " + h);
                   /*for (int amplituide : amplituides) {
                        System.out.print("  " + amplituide);
                    }
                    System.out.println();*/
                }).start();
            }
        }, 20000, false, true);
        visualizer.setEnabled(true);
    }

    private Pair<Integer, Pair<Integer, Integer>> getBuckets(int range) {
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
        a = by3 - (int)(by3 / 1.3) + addA;
        b = a + by3 - by3 / 2;
        c = b + by3 + addB + (int)(by3 / 1.3) + by3 / 2;
        Log.i("meh", "getBuckets: " + a + "  " + b + "  " + c + "  " + range);
        return new Pair<>(a, new Pair<>(b, c));
    }
}
