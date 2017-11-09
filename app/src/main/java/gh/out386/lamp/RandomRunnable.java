package gh.out386.lamp;

/**
 * Kanged from https://gist.github.com/jamesotron/766994
 */

public class RandomRunnable implements Runnable {
    private MainActivity.RandomRgb listener;
    private boolean started = true;
    private int [] rgbColour = new int [3];

    RandomRunnable(MainActivity.RandomRgb listener) {
        this.listener = listener;
        rgbColour[0] = 255;
        rgbColour[1] = 0;
        rgbColour[2] = 0;
    }

    @Override
    public void run() {
        outer: while (started) {
            // Choose the colours to increment and decrement.
            for (int decColour = 0; decColour < 3; decColour += 1) {
                int incColour = decColour == 2 ? 0 : decColour + 1;

                // cross-fade the two colours.
                for (int i = 0; i < 255; i += 1) {
                    if (! started)
                        break outer;
                    rgbColour[decColour] -= 1;
                    rgbColour[incColour] += 1;

                    listener.setRgb(rgbColour[0], rgbColour[1], rgbColour[2]);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    void stop() {
        started = false;
    }

    void start() {
        started = true;
    }
}
