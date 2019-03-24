package com.kamer.chartapp.view.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.Pair;

import com.kamer.chartapp.view.Constants;
import com.kamer.chartapp.view.data.DatePoint;
import com.kamer.chartapp.view.surface.ChartView;

import java.util.ArrayList;
import java.util.List;

public class DateAlphaAnimator {

    private ChartView chartView;

    private List<DatePoint> data = new ArrayList<>();

    private Integer firstIndex;
    private float[] xAlphas = new float[0];
    private ValueAnimator currentAnimation;

    public DateAlphaAnimator(ChartView chartView) {
        this.chartView = chartView;
    }

    public void setData(List<DatePoint> data, float minX, float maxX) {
        this.data = data;
        this.firstIndex = null;
        xAlphas = calculateTargetAlphas(calculateIndexes(minX, maxX));

        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
    }

    public float[] getXAlphas() {
        return xAlphas;
    }

    public void animate(final float minX, final float maxX) {
        if (currentAnimation != null && currentAnimation.isRunning()) return;
        float[] targetX = calculateTargetAlphas(calculateIndexes(minX, maxX));
        List<Pair<Integer, Float>> toAnimate = new ArrayList<>();
        for (int i = 0; i < targetX.length; i++) {
            if (xAlphas[i] != targetX[i]) {
                toAnimate.add(new Pair<>(i, targetX[i]));
            }
        }
        if (toAnimate.isEmpty()) return;

        PropertyValuesHolder[] properties = new PropertyValuesHolder[toAnimate.size()];
        int i = 0;
        for (Pair<Integer, Float> integerFloatPair : toAnimate) {
            properties[i] = PropertyValuesHolder.ofFloat(integerFloatPair.first + "", xAlphas[integerFloatPair.first], integerFloatPair.second);
            i++;
        }
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        ValueAnimator animator = new ValueAnimator();
        animator.setValues(properties);
        animator.setDuration(100);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                for (int i = 0; i < xAlphas.length; i++) {
                    if (valueAnimator.getAnimatedValue(i + "") != null) {
                        xAlphas[i] = (float) valueAnimator.getAnimatedValue(i + "");
                    }
                }
                chartView.setXAlphas(xAlphas);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animate(minX, maxX);
            }
        });
        currentAnimation = animator;
        animator.start();
    }

    private float[] calculateTargetAlphas(List<Integer> datePointsIndexes) {
        float[] targetX = new float[data.size()];
        for (int i = 0; i < targetX.length; i++) {
            targetX[i] = datePointsIndexes.contains(i) ? 1f : 0f;
        }
        return targetX;
    }

    private List<Integer> calculateIndexes(float minX, float maxX) {
        int lastIndex = data.size() - 1;
        int startIndex;
        if (firstIndex != null) {
            startIndex = lastIndex - firstIndex;
        } else {
            startIndex = 0;
            while (!isIndexFit(startIndex, minX, maxX)) {
                startIndex++;
            }
        }
        firstIndex = lastIndex - startIndex;
        List<Integer> indexes = new ArrayList<>();
        indexes.add(lastIndex - startIndex);
        int nextIndex;
        int i = 0;
        do {
            nextIndex = startIndex + (int) Math.pow(2, i);
            i++;
        }
        while ((!isIndexFit(nextIndex, minX, maxX) || isIndexesCollide(startIndex, nextIndex, minX, maxX)) && nextIndex < data.size());
        if (isIndexFit(nextIndex, minX, maxX)) {
            indexes.add(lastIndex - nextIndex);
            int diff = nextIndex - startIndex;
            int index = nextIndex + diff;
            while (isIndexFit(index, minX, maxX)) {
                indexes.add(lastIndex - index);
                index = index + diff;
            }
        }
        return indexes;
    }

    private boolean isIndexFit(int index, float minX, float maxX) {
        if (index >= data.size()) return false;
        float dateSize = 0.13f * (maxX - minX);
        float percent = applyXMargin(data.get(index).getPercent(), minX, maxX);
        return percent - dateSize / 2 > 0 && percent + dateSize / 2 < 1;
    }

    private boolean isIndexesCollide(int index1, int index2, float minX, float maxX) {
        float dateSize = 0.13f * (maxX - minX);
        float percent1 = applyXMargin(data.get(index1).getPercent(), minX, maxX);
        float percent2 = applyXMargin(data.get(index2).getPercent(), minX, maxX);
        return Math.abs(percent1 - percent2) < dateSize;
    }

    private float applyXMargin(float x, float minX, float maxX) {
        float marginPercent = marginPercent(minX, maxX);
        return x * (1 - marginPercent * 2) + marginPercent;
    }

    private float marginPercent(float minX, float maxX) {
        return Constants.PADDING_HORIZONTAL / (chartView.getWidth() / (maxX - minX));
    }

}
