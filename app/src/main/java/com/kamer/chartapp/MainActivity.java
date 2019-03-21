package com.kamer.chartapp;

import android.animation.ArgbEvaluator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.kamer.chartapp.data.DataProvider;
import com.kamer.chartapp.data.InputData;
import com.kamer.chartapp.view.ChartManager;
import com.kamer.chartapp.view.ChartView;
import com.kamer.chartapp.view.PreviewMaskView;
import com.kamer.chartapp.view.PreviewView;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.utils.DataConverter;

import java.util.List;

public class MainActivity extends Activity implements ChartManager.UpdateListener {

    private boolean isDarkTheme = true;

    private ViewGroup buttonsLayout;
    private RadioGroup radioGroupLayout;

    private ChartManager chartManager;
    private PreviewMaskView previewMaskView;

    private Menu menu;

    private ValueAnimator themeAnimator;
    private int primaryColor;
    private int darkColor;
    private int backgroundColor;
    private int textColor;
    private int overlayColor;
    private int frameColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ChartView chartView = findViewById(R.id.view_chart);
        PreviewView previewView = findViewById(R.id.view_preview);
        previewMaskView = findViewById(R.id.view_preview_mask);
        buttonsLayout = findViewById(R.id.layout_buttons);
        radioGroupLayout = findViewById(R.id.radio_group_layout);

        primaryColor = getResources().getColor(R.color.colorDarkPrimary);
        darkColor = getResources().getColor(R.color.colorDarkPrimaryDark);
        backgroundColor = getResources().getColor(R.color.colorDarkBackground);
        textColor = Color.WHITE;
        overlayColor = getResources().getColor(R.color.colorDarkOverlay);
        frameColor = getResources().getColor(R.color.colorDarkFrame);

        setColors(primaryColor, darkColor, backgroundColor, textColor);

        List<InputData> data = DataProvider.getData();
        for (int i = 0; i < data.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText("" + i);
            radioButton.setTextColor(isDarkTheme ? Color.WHITE : Color.BLACK);
            radioButton.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
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

        chartManager = new ChartManager(chartView, previewView, previewMaskView, this);
        reloadData(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        isDarkTheme = !isDarkTheme;
        updateTheme();
        return true;
    }

    private void updateTheme() {
        int targetPrimaryColor;
        int targetDarkColor;
        int targetBackgroundColor;
        int targetTextColor;
        int targetOverlayColor;
        int targetFrameColor;
        if (isDarkTheme) {
            menu.getItem(0).setIcon(R.drawable.ic_brightness_7_black_24dp);
            targetPrimaryColor = getResources().getColor(R.color.colorDarkPrimary);
            targetDarkColor = getResources().getColor(R.color.colorDarkPrimaryDark);
            targetBackgroundColor = getResources().getColor(R.color.colorDarkBackground);
            targetTextColor = Color.WHITE;
            targetOverlayColor = getResources().getColor(R.color.colorDarkOverlay);
            targetFrameColor = getResources().getColor(R.color.colorDarkFrame);
        } else {
            menu.getItem(0).setIcon(R.drawable.moon);
            targetPrimaryColor = getResources().getColor(R.color.colorPrimary);
            targetDarkColor = getResources().getColor(R.color.colorPrimaryDark);
            targetBackgroundColor = getResources().getColor(R.color.colorBackground);
            targetTextColor = Color.BLACK;
            targetOverlayColor = getResources().getColor(R.color.colorOverlay);
            targetFrameColor = getResources().getColor(R.color.colorFrame);
        }


        PropertyValuesHolder[] properties = new PropertyValuesHolder[6];
        properties[0] = PropertyValuesHolder.ofObject("primary", new ArgbEvaluator(), primaryColor, targetPrimaryColor);
        properties[1] = PropertyValuesHolder.ofObject("primaryDark", new ArgbEvaluator(), darkColor, targetDarkColor);
        properties[2] = PropertyValuesHolder.ofObject("background", new ArgbEvaluator(), backgroundColor, targetBackgroundColor);
        properties[3] = PropertyValuesHolder.ofObject("text", new ArgbEvaluator(), textColor, targetTextColor);
        properties[4] = PropertyValuesHolder.ofObject("overlay", new ArgbEvaluator(), overlayColor, targetOverlayColor);
        properties[5] = PropertyValuesHolder.ofObject("frame", new ArgbEvaluator(), frameColor, targetFrameColor);
        if (themeAnimator != null) {
            themeAnimator.cancel();
        }
        ValueAnimator animator = new ValueAnimator();
        animator.setValues(properties);
        animator.setDuration(100);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int newPrimaryColor = (int) valueAnimator.getAnimatedValue("primary");
                int newDarkColor = (int) valueAnimator.getAnimatedValue("primaryDark");
                int newBackgroundColor = (int) valueAnimator.getAnimatedValue("background");
                int newTextColor = (int) valueAnimator.getAnimatedValue("text");
                int newOverlayColor = (int) valueAnimator.getAnimatedValue("overlay");
                int newFrameColor = (int) valueAnimator.getAnimatedValue("frame");
                setColors(
                        newPrimaryColor,
                        newDarkColor,
                        newBackgroundColor,
                        newTextColor
                );
                previewMaskView.setColors(newOverlayColor, newFrameColor);
                primaryColor = newPrimaryColor;
                darkColor = newDarkColor;
                backgroundColor = newBackgroundColor;
                textColor = newTextColor;
                overlayColor = newOverlayColor;
                frameColor = newFrameColor;
            }
        });
        themeAnimator = animator;
        animator.start();
    }

    private void setColors(int primaryColor, int darkColor, int backgroundColor, int textColor) {
        getActionBar().setBackgroundDrawable(new ColorDrawable(primaryColor));
        getWindow().setNavigationBarColor(primaryColor);
        getWindow().setStatusBarColor(darkColor);
        getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
        for (int i = 0; i < buttonsLayout.getChildCount(); i++) {
            ((CheckBox) buttonsLayout.getChildAt(i)).setTextColor(textColor);
        }
        for (int i = 0; i < radioGroupLayout.getChildCount(); i++) {
            ((RadioButton) radioGroupLayout.getChildAt(i)).setTextColor(textColor);
        }
    }

    @Override
    public void onUpdate(List<Graph> graphs) {
        buttonsLayout.removeAllViews();
        for (final Graph datum : graphs) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            checkBox.setText(datum.getName());
            checkBox.setChecked(datum.isEnabled());
            checkBox.setTextColor(isDarkTheme ? Color.WHITE : Color.BLACK);
            checkBox.setButtonTintList(ColorStateList.valueOf(datum.getColor()));
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
