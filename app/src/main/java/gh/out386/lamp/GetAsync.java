package gh.out386.lamp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.SeekBar;

import com.sdsmdg.harjot.crollerTest.Croller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetAsync extends AsyncTask<String, Void, String> {
    static final String ACTION_SERVER_FAIL = "serverFail";
    private ProgressDialog pd;
    private WeakReference<Context> contextRef;
    private MutableLiveData<Integer> red = new MutableLiveData<>();
    private MutableLiveData<Integer> green = new MutableLiveData<>();
    private MutableLiveData<Integer> blue = new MutableLiveData<>();
    private MutableLiveData<Integer> white = new MutableLiveData<>();

    public GetAsync(Context context) {
        contextRef = new WeakReference<>(context);
        pd = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context c;
        if ((c = contextRef.get()) != null) {
            pd.setMessage(c.getString(R.string.server_connecting));
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        URL url;
        HttpURLConnection urlConnection = null;
        StringBuilder o;
        try {
            url = new URL(strings[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            String s;
            o = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while ((s = br.readLine()) != null)
                o.append(s);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return o.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s == null) {
            pd.dismiss();
            Context context;
            if ((context = contextRef.get()) != null) {
                new AlertDialog.Builder(context)
                        .setMessage(R.string.server_fail)
                        .setTitle(R.string.server_fail_title)
                        .setCancelable(false)
                        .setPositiveButton(R.string.server_fail_button, (dialog, which) ->
                                LocalBroadcastManager.getInstance(context)
                                        .sendBroadcast(new Intent().setAction(ACTION_SERVER_FAIL)))
                        .show();
            }
            return;
        }
        TempModel model = Utils.parseJson(s);
        if (red != null)
            red.setValue(model.r);
        if (green != null)
            green.setValue(model.g);
        if (blue != null)
            blue.setValue(model.b);
        if (white != null)
            white.setValue(model.w);
        // As this data should only be fetched once
        red = green = blue = white = null;
        pd.dismiss();
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
}
