package com.kamer.chartapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;

import com.kamer.chartapp.view.ChartView;
import com.kamer.chartapp.view.data.InputItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random random;
    private ChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random = new Random();
        chartView = findViewById(R.id.view_chart);

        chartView.post(new Runnable() {
            @Override
            public void run() {
                reloadData();
            }
        });
    }

    private List<InputItem> createData() {
        List<InputItem> result = new ArrayList<>();
        int length = 25;
        for (int i = 0; i < length; i++) {
            long timestamp = System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS * i;
            long value = random.nextInt(50);
            result.add(new InputItem(timestamp, value));
        }
        return result;
    }

    private void reloadData() {
        chartView.setData(createData());
    }

    public void onReloadClick(View view) {
        reloadData();
    }

    public void onZoomXClick(View view) {

    }

    public void onPanClick(View view) {

    }

    public void onZoomYClick(View view) {
        chartView.switchZoomY();
    }
}
