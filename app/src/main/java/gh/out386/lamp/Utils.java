package gh.out386.lamp;

/**
 * Created by J on 11/9/2017.
 */

public class Utils {
    static String buildUrlRgb(int red, int green, int blue, int white) {
        // Not checking max/min values, as the server does that anyway
        return "http://192.168.43.200/setrgb?r="
        + red
        + "&g=" + green
        + "&b=" + blue
        + "&w=" + white;
    }

    /**
     * Algorithm by Tanner Helland
     * Takes a temperature in Kelvin, and returns an URL with the RGB values for that temperature
     * @param temp The target temperature in Kelvin. Min 1000, max 40000
     * @param white The current white value
     * @return URL with RGB values of the target URL, and the given white value
     */
    static TempModel buildUrlHellandTemp(int temp, int white) {
        int r;
        int g;
        int b;

        temp = temp / 100;
        // For red
        if (temp <= 66)
            r = 255;
        else {
            r = (int) (329.698727446 * (Math.pow((double)(temp - 60), -0.1332047592)));
            if (r < 0)
                r = 0;
            else if (r > 255)
                r = 255;
        }

        // For green
        if (temp <= 66)
            g = (int) (99.4708025861 * Math.log(temp) - 161.1195681661);
        else
            g = (int) (288.1221695283 * Math.pow((double)(temp - 60), -0.0755148492));
        if (g < 0)
            g = 0;
        else if (g > 255)
            g = 255;

        // For blue
        if (temp >= 66)
            b = 255;
        else {
            if (temp <= 19)
                b = 0;
            else {
                b = (int) (138.5177312231 * Math.log(temp - 10) - 305.0447927307);
                if (b < 0)
                    b = 0;
                else if (b > 255)
                    b = 255;
            }
        }
        return new TempModel(r, g, b, white, buildUrlRgb(r, g, b, white));
    }
}
