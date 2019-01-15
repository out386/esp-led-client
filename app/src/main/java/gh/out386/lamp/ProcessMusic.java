package gh.out386.lamp;

import android.media.audiofx.Visualizer;
import android.util.Log;
import android.util.Pair;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by J on 12/2/2017.
 */

public class ProcessMusic {
    private final int SLOP = 5;
    private final int OFFSET = 5;
    private final int MULTIPLIER = 6;
    private final int MULTIPLIER_L = 5;
    private int averageLength;
    int dA, dB, dC;
    private Visualizer visualizer;
    private Pair<Integer, Pair<Integer, Integer>> buckets;
    private int range;
    private int maxRange;
    private VisListener visListener;
    private ArrayBlockingQueue<Float> averagingQueueL;
    private ArrayBlockingQueue<Float> averagingQueueM;
    private ArrayBlockingQueue<Float> averagingQueueH;
    private float averageL = 0f;
    private float averageM = 0f;
    private float averageH = 0f;

    public ProcessMusic(VisListener visListener, int averageLength) {
        this.visListener = visListener;
        this.averageLength = averageLength;
        range = 24;//Visualizer.getCaptureSizeRange()[0];
        maxRange = 1024;//Visualizer.getCaptureSizeRange()[1];
        visualizer = new Visualizer(0);
        visualizer.setCaptureSize(maxRange);
        visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        buckets = getBuckets(range, maxRange / 2 - 2);
        dA = buckets.first;
        dB = buckets.second.first - buckets.first;
        dC = buckets.second.second - buckets.second.first;

        averagingQueueL = new ArrayBlockingQueue<>(averageLength, true);
        averagingQueueM = new ArrayBlockingQueue<>(averageLength, true);
        averagingQueueH = new ArrayBlockingQueue<>(averageLength, true);
        for (int i = 0; i < averageLength; i++) {
            averagingQueueL.offer(-1f);
            averagingQueueM.offer(-1f);
            averagingQueueH.offer(-1f);
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



                int amplituides[] = new int[maxRange / 2];
                    //System.out.print(Visualizer.getCaptureSizeRange()[1] + " ");
                    for (int i = 1; i < maxRange / 2; i++) {
                        byte rfk = fft[i * 2];
                        byte ifk = fft[i * 2 + 1];
                        //amplituides[i - 1] = (int) (Math.hypot(rfk, ifk));
                        amplituides[i - 1] = rfk * rfk + ifk * ifk;
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
                    //l = (float) Math.sqrt(l);
                    //l = (float) (50 * Math.log10(l));
                    //l = l * MULTIPLIER_L;
                    if (l > 1023)
                        l = 1023;

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
                    //m = (float) Math.sqrt(m);
                    //m = (float) (50 * Math.log10(m));
                    //m = m * MULTIPLIER;
                    if (m > 1023)
                        m = 1023;

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
                    //h = (float) Math.sqrt(h);
                    //h = (float) (50 * Math.log10(h));
                    //h = h * MULTIPLIER;
                    if (h > 1023)
                        h = 1023;
                    sendValue(l, m, h);


                    //System.out.println("wtf");
                    for (int amplituide : amplituides) {
                    //    System.out.printf("%4d ", amplituide > 0 ? Math.round(10 * Math.log10(amplituide)) : 0);
                        System.out.printf("%4d ", amplituide);
                    }
                    System.out.println();
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
        a = by3 - by3 / 2 + addA;
        b = a + by3 - by3 / 4;
        c = b + by3 + addB /*+ (int)(by3 / 2) + by3 / 2*/;
        Log.i("meh", "getBuckets: " + a + "  " + b + "  " + c + "  max:" + range);
        return new Pair<>(a, new Pair<>(b, max));
    }

    public void stop() {
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
        //System.out.printf("%3d %3d %3d\n", Math.round(averageL), Math.round(averageM), Math.round(averageH));
        visListener.OnValue(Math.round(averageL), Math.round(averageM), Math.round(averageH));
    }

    interface VisListener {
        void OnValue(int r, int g, int b);
    }
}
