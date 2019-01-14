package gh.out386.lamp;

import gh.out386.lamp.interfaces.RandomRgb;

/**
 * Source: https://gist.github.com/jamesotron/766994
 */

public class RandomRunnable implements Runnable {
    private RandomRgb listener;
    private boolean started = true;
    private int[] rgbColour = new int[3];
    private int decColour = 0;
    private int incColour;
    private int innerLoop = 0;

    public RandomRunnable(RandomRgb listener) {
        this.listener = listener;
        rgbColour[0] = 255;
        rgbColour[1] = 0;
        rgbColour[2] = 0;
    }

    @Override
    public void run() {
        outer:
        while (true) {
            // Choose the colours to increment and decrement.
            for (; decColour < 3; decColour++) {
                incColour = decColour == 2 ? 0 : decColour + 1;

                // cross-fade the two colours.
                for (; innerLoop < 255; innerLoop++) {
                    if (!started) {
                        innerLoop--;
                        break outer;
                    }
                    rgbColour[decColour]--;
                    rgbColour[incColour]++;

                    listener.setRgb(rgbColour[0], rgbColour[1], rgbColour[2]);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {
                    }
                }
                innerLoop = 0;
            }
            decColour = 0;
        }
    }

    public void stop() {
        started = false;
    }

    public void start() {
        started = true;
    }
}
