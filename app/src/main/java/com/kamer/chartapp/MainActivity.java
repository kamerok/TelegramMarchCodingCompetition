package com.kamer.chartapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.kamer.chartapp.data.DataProvider;
import com.kamer.chartapp.data.InputGraph;
import com.kamer.chartapp.view.ChartManager;
import com.kamer.chartapp.view.ChartView;
import com.kamer.chartapp.view.PreviewView;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.utils.DataConverter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ChartManager.UpdateListener {

    private SeekBar leftView;
    private SeekBar rightView;
    private SeekBar panView;
    private ViewGroup buttonsLayout;


    private ChartManager chartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ChartView chartView = findViewById(R.id.view_chart);
        PreviewView previewView = findViewById(R.id.view_preview);
        leftView = findViewById(R.id.view_left_border);
        rightView = findViewById(R.id.view_right_border);
        panView = findViewById(R.id.view_pan);
        buttonsLayout = findViewById(R.id.layout_buttons);
        RadioGroup radioGroupLayout = findViewById(R.id.radio_group_layout);

        List<List<InputGraph>> data = DataProvider.getData();
        for (int i = 0; i < data.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText("" + i);
            final int index = i;
            radioGroupLayout.addView(radioButton);
            if (i == 0) {
                radioGroupLayout.check(radioGroupLayout.getChildAt(0).getId());
            }
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        reloadData(index);
                    }
                }
            });
        }

        leftView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float newValue = progress / ((float) seekBar.getMax());
                    chartManager.setLeftBorder(newValue);
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
                    chartManager.setRightBorder(newValue);
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
                    chartManager.setPan(newValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        chartManager = new ChartManager(chartView, previewView, this);
        reloadData(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "Change theme", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onUpdate(float left, float right, float pan, List<Graph> graphs) {
        leftView.setProgress((int) (left * leftView.getMax()));
        rightView.setProgress((int) (right * rightView.getMax()));
        panView.setProgress((int) (pan * panView.getMax()));

        buttonsLayout.removeAllViews();
        for (final Graph datum : graphs) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            checkBox.setText(datum.getName());
            checkBox.setChecked(datum.isEnabled());
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(datum.getColor()));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    boolean isAllUnchecked = true;
                    for (int i = 0; i < buttonsLayout.getChildCount(); i++) {
                        View child = buttonsLayout.getChildAt(i);
                        if (child instanceof Checkable && ((Checkable) child).isChecked()) {
                            isAllUnchecked = false;
                            break;
                        }
                    }

                    if (isAllUnchecked) {
                        buttonView.setChecked(true);
                    } else {
                        chartManager.updateGraphEnabled(datum.getName(), isChecked);
                    }
                }
            });
            buttonsLayout.addView(checkBox);
        }
    }

    private void reloadData(int index) {
        chartManager.setData(DataConverter.convertInput(DataProvider.getData().get(index)));
    }

}
