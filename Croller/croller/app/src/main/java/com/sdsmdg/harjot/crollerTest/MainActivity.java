package com.sdsmdg.harjot.crollerTest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Croller croller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        croller = (Croller) findViewById(R.id.croller);
//        croller.setIndicatorWidth(10);
//        croller.setBackCircleColor(Color.parseColor("#EDEDED"));
//        croller.setMainCircleColor(Color.WHITE);
//        croller.setMax(50);
//        croller.setStartOffset(45);
//        croller.setIsContinuous(false);
//        croller.setLabelColor(Color.BLACK);
//        croller.setProgressPrimaryColor(Color.parseColor("#0B3C49"));
//        croller.setIndicatorColor(Color.parseColor("#0B3C49"));
//        croller.setProgressSecondaryColor(Color.parseColor("#EEEEEE"));
//        croller.setProgressRadius(380);
//        croller.setBackCircleRadius(300);

        croller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {

            }

            @Override
            public void onStartTrackingTouch(Croller croller) {
                Toast.makeText(MainActivity.this, "Start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(Croller croller) {
                Toast.makeText(MainActivity.this, "Stop", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
