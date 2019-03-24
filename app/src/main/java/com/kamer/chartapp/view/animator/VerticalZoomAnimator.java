package com.kamer.chartapp.view.animator;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;

import com.kamer.chartapp.view.Constants;
import com.kamer.chartapp.view.PreviewView;
import com.kamer.chartapp.view.data.Data;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.GraphItem;
import com.kamer.chartapp.view.data.YGuides;
import com.kamer.chartapp.view.surface.ChartView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kamer.chartapp.view.utils.FloatUtils.isFloatEquals;

public class VerticalZoomAnimator {

    private ChartView chartView;
    private PreviewView previewView;

    private Data data;

    private Map<YGuides, Float> guideAlphas = new HashMap<>();
    private float minY;
    private float maxY = 1;
    private float totalMaxY = 1;

    private ValueAnimator currentZoomAnimation;
    private ValueAnimator currentAlphaAnimation;

    public VerticalZoomAnimator(ChartView chartView, PreviewView previewView) {
        this.chartView = chartView;
        this.previewView = previewView;
    }

    public void setData(Data data, float minX, float maxX) {
        this.data = data;

        float[] targetRange = calculateTargetRange(minX, maxX, true);
        minY = targetRange[0];
        maxY = targetRange[1];
        guideAlphas.clear();
        guideAlphas.put(new YGuides(calculateYGuides(targetRange[0], targetRange[1]), true), 1f);

        totalMaxY = calculateTargetRange(0, 1, false)[1];

        if (currentZoomAnimation != null) {
            currentZoomAnimation.cancel();
        }
        if (currentAlphaAnimation != null) {
            currentAlphaAnimation.cancel();
        }
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public float getTotalMaxY() {
        return totalMaxY;
    }

    public Map<YGuides, Float> getGuideAlphas() {
        return guideAlphas;
    }

    public void animate(float minX, float maxX) {
        float[] targetRange = calculateTargetRange(minX, maxX, false);
        float[] totalRange = calculateTargetRange(0f, 1f, false);

        float[] percents = calculateYGuides(targetRange[0], targetRange[1]);
        animateZoom(percents[0], targetRange[1], totalRange[1]);
        animateAlpha(percents);
    }

    private void animateZoom(float targetMinY, float targetMaxY, float targetTotalMax) {
        PropertyValuesHolder[] properties = new PropertyValuesHolder[3];
        properties[0] = PropertyValuesHolder.ofFloat("minY", minY, targetMinY);
        properties[1] = PropertyValuesHolder.ofFloat("maxY", maxY, targetMaxY);
        properties[2] = PropertyValuesHolder.ofFloat("totalMaxY", totalMaxY, targetTotalMax);
        if (currentZoomAnimation != null) {
            currentZoomAnimation.cancel();
        }
        ValueAnimator animator = new ValueAnimator();
        animator.setValues(properties);
        animator.setDuration(100);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float newMin = (float) valueAnimator.getAnimatedValue("minY");
                float newMax = (float) valueAnimator.getAnimatedValue("maxY");
                float newTotalMax = (float) valueAnimator.getAnimatedValue("totalMaxY");

                minY = newMin;
                maxY = newMax;
                chartView.setYRange(minY, maxY);

                if (newTotalMax != totalMaxY) {
                    totalMaxY = newTotalMax;
                    previewView.setMax(newTotalMax);
                }
            }
        });
        currentZoomAnimation = animator;
        animator.start();
    }

    private void animateAlpha(float[] percents) {
        YGuides targetGuides = new YGuides(percents, true);
        if (!guideAlphas.containsKey(targetGuides)) {
            guideAlphas.put(targetGuides, 0f);
        }

        List<YGuides> keys = new ArrayList<>(guideAlphas.keySet());
        for (YGuides guides : keys) {
            if (!targetGuides.equals(guides) && guides.isActive()) {
                float value = guideAlphas.get(guides);
                guideAlphas.remove(guides);
                guideAlphas.put(new YGuides(guides.getPercent(), false), value);
            }
        }

        PropertyValuesHolder[] properties = new PropertyValuesHolder[guideAlphas.size()];
        int i = 0;
        for (Map.Entry<YGuides, Float> yGuidesFloatEntry : guideAlphas.entrySet()) {
            properties[i] = PropertyValuesHolder.ofFloat(yGuidesFloatEntry.getKey().hashCode() + "", yGuidesFloatEntry.getValue(), yGuidesFloatEntry.getKey().isActive() ? 1f : 0f);
            i++;
        }
        if (currentAlphaAnimation != null) {
            currentAlphaAnimation.cancel();
        }
        ValueAnimator animator = new ValueAnimator();
        animator.setValues(properties);
        animator.setDuration(100);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                List<YGuides> keys = new ArrayList<>(guideAlphas.keySet());
                for (YGuides guides : keys) {
                    float value = (float) valueAnimator.getAnimatedValue(guides.hashCode() + "");
                    guideAlphas.remove(guides);
                    if (!(value == 0 && !guides.isActive())) {
                        guideAlphas.put(guides, value);
                    }
                }
                chartView.setGuideAlphas(new HashMap<>(guideAlphas));
            }
        });
        currentAlphaAnimation = animator;
        animator.start();
    }

    private float[] calculateTargetRange(float minX, float maxX, boolean withMargin) {
        float yMin = 1;
        float yMax = 0;

        int firstInclusiveIndex = findFirstInclusiveIndex(minX, withMargin, minX, maxX);
        int lastInclusiveIndex = findLastInclusiveIndex(maxX, withMargin, minX, maxX);

        for (Graph graph : data.getGraphs()) {
            if (!graph.isEnabled()) continue;
            List<GraphItem> graphItems = graph.getItems();

            if (firstInclusiveIndex > 0) {
                float startYPercentage = calcYAtXByTwoPoints(
                        minX,
                        data.getDatePoints().get(firstInclusiveIndex - 1).getPercent(),
                        graphItems.get(firstInclusiveIndex - 1).getPercent(),
                        data.getDatePoints().get(firstInclusiveIndex).getPercent(),
                        graphItems.get(firstInclusiveIndex).getPercent()
                );
                if (startYPercentage < yMin) {
                    yMin = startYPercentage;
                } else if (startYPercentage > yMax) {
                    yMax = startYPercentage;
                }
            }

            if (lastInclusiveIndex < graph.getItems().size() - 1) {
                float endYPercentage = calcYAtXByTwoPoints(
                        maxX,
                        data.getDatePoints().get(lastInclusiveIndex).getPercent(),
                        graphItems.get(lastInclusiveIndex).getPercent(),
                        data.getDatePoints().get(lastInclusiveIndex + 1).getPercent(),
                        graphItems.get(lastInclusiveIndex + 1).getPercent()
                );

                if (endYPercentage < yMin) {
                    yMin = endYPercentage;
                } else if (endYPercentage > yMax) {
                    yMax = endYPercentage;
                }
            }
            for (int i = firstInclusiveIndex; i <= lastInclusiveIndex; i++) {
                float value = graphItems.get(i).getPercent();
                if (value < yMin) {
                    yMin = value;
                } else if (value > yMax) {
                    yMax = value;
                }
            }
        }

        return new float[]{yMin, yMax};
    }

    private float[] calculateYGuides(float minY, float maxY) {
        int count = 6;
        float bottomValue = (data.getMaxValue() - data.getMinValue()) * minY + data.getMinValue();
        float top = (data.getMaxValue() - data.getMinValue()) * (maxY) + data.getMinValue();

        int roundBottom = (int) Math.floor(bottomValue);
        roundBottom = roundBottom / 10 * 10;
        float bottomPercent = (float) (roundBottom - data.getMinValue()) / (data.getMaxValue() - data.getMinValue());

        int roundTop = (int) Math.floor(top);
        roundTop = roundTop / 10 * 10;
        float topPercent = (float) (roundTop - data.getMinValue()) / (data.getMaxValue() - data.getMinValue());

        float segment = Math.abs(topPercent - bottomPercent) / 5;

        float[] guides = new float[count];
        guides[0] = bottomPercent;
        for (int i = 1; i < count; i++) {
            guides[i] = guides[i - 1] + segment;
        }
        return guides;
    }

    private int findFirstInclusiveIndex(float startXPercentage, boolean respectMargin, float minX, float maxX) {
        for (int i = 0; i < data.getDatePoints().size(); i++) {
            float value = data.getDatePoints().get(i).getPercent();
            if (respectMargin) {
                value = applyXMargin(value, minX, maxX);
            }
            if (value > startXPercentage || isFloatEquals(value, startXPercentage)) {
                return i;
            }
        }
        return -1;
    }

    private int findLastInclusiveIndex(float endXPercentage, boolean respectMargins, float minX, float maxX) {
        for (int i = data.getDatePoints().size() - 1; i >= 0; i--) {
            float value = data.getDatePoints().get(i).getPercent();
            if (respectMargins) {
                value = applyXMargin(value, minX, maxX);
            }
            if (value < endXPercentage || isFloatEquals(value, endXPercentage)) {
                return i;
            }
        }
        return -1;
    }

    private float applyXMargin(float x, float minX, float maxX) {
        float marginPercent = marginPercent(minX, maxX);
        return x * (1 - marginPercent * 2) + marginPercent;
    }

    private float marginPercent(float minX, float maxX) {
        return Constants.PADDING_HORIZONTAL / (chartView.getWidth() / (maxX - minX));
    }

    private float calcYAtXByTwoPoints(float x, float x1, float y1, float x2, float y2) {
        return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
    }
}
