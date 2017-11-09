package gh.out386.lamp;

import android.arch.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by J on 11/9/2017.
 */

public class Utils {
    private static final String BASE_URL = "http://192.168.43.200";
    static final String GET_URL = BASE_URL + "/get";
    public static final int RED = 0;
    public static final int GREEN = 1;
    public static final int BLUE = 2;
    public static final int WHITE = 3;
    public static final int BRIGHTNESS = 4;
    public static final int TEMP = 5;

    static String buildUrlRgb(int red, int green, int blue, int white) {
        // Not checking max/min values, as the server does that anyway
        return BASE_URL + "/setrgb?r=" + red
                + "&g=" + green
                + "&b=" + blue
                + "&w=" + white;
    }

    /**
     * Algorithm by Tanner Helland
     * Takes a temperature in Kelvin, and returns an URL with the RGB values for that temperature
     * Source: http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code
     *
     * @param temp  The target temperature in Kelvin. Min 1000, max 40000
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
            r = (int) (329.698727446 * (Math.pow((double) (temp - 60), -0.1332047592)));
            if (r < 0)
                r = 0;
            else if (r > 255)
                r = 255;
        }

        // For green
        if (temp <= 66)
            g = (int) (99.4708025861 * Math.log(temp) - 161.1195681661);
        else
            g = (int) (288.1221695283 * Math.pow((double) (temp - 60), -0.0755148492));
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

    static TempModel parseJson(String json) {
        int r = 0;
        int g = 0;
        int b = 0;
        int w = 0;
        JSONObject object;
        try {
            object = new JSONObject(json);
            r = object.getInt("r");
            g = object.getInt("g");
            b = object.getInt("b");
            w = object.getInt("w");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new TempModel(r, g, b, w, null);
    }

    static int getInt(MutableLiveData<Integer> i) {
        if (i.getValue() == null)
            return 0;
        return i.getValue();
    }
}
