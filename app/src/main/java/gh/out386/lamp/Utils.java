package gh.out386.lamp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by J on 11/9/2017.
 */

public class Utils {
    private final static String BASE_URL = "http://192.168.43.200";
    final static String GET_URL = BASE_URL + "/get";
    private static final String RGB_STRING_FORMAT = "rgb:r:%d,g:%d,b:%d,w:%d";

    static String buildRgbMessage(int red, int green, int blue, int white) {
        // Not checking max/min values, as the server does that anyway
        return String.format(Locale.ENGLISH,
                RGB_STRING_FORMAT, red, green, blue, white);
    }

    static TempModel parseJson(String json) {
        int r = 0;
        int g = 0;
        int b = 0;
        int w = 0;
        JSONObject object = null;
        try {
            object = new JSONObject(json);
            r = object.getInt("r");
            g = object.getInt("g");
            b = object.getInt("b");
            w = object.getInt("w");
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return new TempModel(r, g, b, w, null);
    }
}
