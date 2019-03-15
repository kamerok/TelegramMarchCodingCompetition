package com.kamer.chartapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.kamer.chartapp.view.ChartView;
import com.kamer.chartapp.view.PreviewView;
import com.kamer.chartapp.view.data.InputGraph;
import com.kamer.chartapp.view.data.InputItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ChartView chartView;
    private PreviewView previewView;
    private SeekBar leftView;
    private SeekBar rightView;
    private SeekBar panView;
    private ViewGroup buttonsLayout;

    private Random random;

    private List<InputGraph> graphs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random = new Random();
        chartView = findViewById(R.id.view_chart);
        previewView = findViewById(R.id.view_preview);
        leftView = findViewById(R.id.view_left_border);
        rightView = findViewById(R.id.view_right_border);
        panView = findViewById(R.id.view_pan);
        buttonsLayout = findViewById(R.id.layout_buttons);

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
        chartView.externalListener = previewView;
    }

    public void onReloadClick(View view) {
        reloadData();
        syncBars();
    }

    private void syncBars() {
        leftView.setProgress((int) (chartView.getLeftBorder() * leftView.getMax()));
        rightView.setProgress((int) (chartView.getRightBorder() * rightView.getMax()));
        panView.setProgress((int) (chartView.getPan() * panView.getMax()));

        previewView.setData(chartView.graphs, chartView.getRightBorder(), chartView.getLeftBorder());
    }

    private List<InputGraph> createData() {
        List<InputGraph> result = new ArrayList<>();

        List<InputItem> values1 = new ArrayList<>();
        int length = 25;
        for (int i = 0; i < length; i++) {
            long timestamp = System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS * i;
            long value = random.nextInt(500);
            values1.add(new InputItem(timestamp, value));
        }
        result.add(new InputGraph(values1, Color.parseColor("#3DC23F"), "First", true));

        List<InputItem> values2 = new ArrayList<>();
        for (int i = 0; i < values1.size(); i++) {
            long value = random.nextInt(1500);
            values2.add(new InputItem(values1.get(i).getTimestamp(), value));
        }
        result.add(new InputGraph(values2, Color.parseColor("#F34C44"), "Second", true));

        /*List<InputItem> values3 = new ArrayList<>();
        for (int i = 0; i < values1.size(); i++) {
            long value = random.nextInt(1000);
            values3.add(new InputItem(values1.get(i).getTimestamp(), value));
        }
        result.add(new InputGraph(values3, Color.BLUE, "kopa", true));*/

        return result;
    }

    private void reloadData() {
        List<InputGraph> data = createData();
        buttonsLayout.removeViews(1, buttonsLayout.getChildCount() - 1);
        for (final InputGraph datum : data) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            checkBox.setText(datum.getName());
            checkBox.setChecked(true);
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(datum.getColor()));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateGraphEnabled(datum.getName(), isChecked);
                }
            });
            buttonsLayout.addView(checkBox);
        }
        graphs = data;
        chartView.setData(data);
    }

    private void updateGraphEnabled(String name, boolean isEnabled) {
        for (int i = 0; i < graphs.size(); i++) {
            InputGraph graph = graphs.get(i);
            if (graph.getName().equals(name)) {
                graphs.set(i, new InputGraph(graph.getValues(), graph.getColor(), graph.getName(), isEnabled));
                //TODO: update?
                chartView.setData(graphs);
                syncBars();
                return;
            }
        }
    }
}
