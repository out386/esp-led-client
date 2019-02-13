package gh.out386.lamp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import gh.out386.lamp.network.DiscoverSsdp;

//TODO: Make good use of this activity and turn it into a device picker
public class ScanActivity extends AppCompatActivity {
    static final String IP_ADDR = "ESP_IP_ADDR";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        DiscoverSsdp ssdp = new DiscoverSsdp();
        DiscoverSsdp.OnDeviceListener listener = new DiscoverSsdp.OnDeviceListener() {
            @Override
            public void onDevice(String data) {
                Log.i("listener", "onDevice: " + data);
                ssdp.stop();
                launchMain(data);
            }

            @Override
            public void onFailure(Exception e) {
                Log.i("listener", "onFailure: " + e.getMessage());
            }
        };
        ssdp.scan(listener);
    }
    private void launchMain(String ipAddress) {
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(IP_ADDR, ipAddress)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
