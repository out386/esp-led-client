package gh.out386.lamp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.sdsmdg.harjot.crollerTest.Croller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by J on 11/9/2017.
 */

public class GetAsync extends AsyncTask<String, Void, String> {
    static final String ACTION_SERVER_FAIL = "serverFail";
    private ProgressDialog pd;
    private WeakReference<Context> contextRef;
    private WeakReference<Croller> whiteSeekRef;

    public GetAsync(Context context, Croller white) {
        contextRef = new WeakReference<>(context);
        whiteSeekRef = new WeakReference<>(white);
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

        Croller white;
        TempModel model = Utils.parseJson(s);
        if ((white = whiteSeekRef.get()) != null)
            white.setProgress(model.w);
        pd.dismiss();
    }
}
