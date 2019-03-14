package com.kamer.chartapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.SeekBar;

import com.kamer.chartapp.view.ChartView;
import com.kamer.chartapp.view.data.InputItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random random;
    private ChartView chartView;
    private SeekBar leftView;
    private SeekBar rightView;
    private SeekBar panView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random = new Random();
        chartView = findViewById(R.id.view_chart);
        leftView = findViewById(R.id.view_left_border);
        rightView = findViewById(R.id.view_right_border);
        panView = findViewById(R.id.view_pan);

        leftView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float newValue = progress / ((float) seekBar.getMax());
                    chartView.setLeftBorder(newValue);
                    syncBars();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        rightView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float newValue = progress / ((float) seekBar.getMax());
                    chartView.setRightBorder(newValue);
                    syncBars();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        panView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float newValue = progress / ((float) seekBar.getMax());
                    chartView.setPan(newValue);
                    syncBars();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        chartView.post(new Runnable() {
            @Override
            public void run() {
                reloadData();
                syncBars();
            }
        });
    }

    private void syncBars() {
        leftView.setProgress((int) (chartView.getLeftBorder() * leftView.getMax()));
        rightView.setProgress((int) (chartView.getRightBorder() * rightView.getMax()));
        panView.setProgress((int) (chartView.getPan() * panView.getMax()));
    }

    private List<InputItem> createData() {
        List<InputItem> result = new ArrayList<>();
        int length = 25;
        for (int i = 0; i < length; i++) {
            long timestamp = System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS * i;
            long value = random.nextInt(500);
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
}
