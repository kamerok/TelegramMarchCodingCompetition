package com.kamer.chartapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.kamer.chartapp.view.data.Data;
import com.kamer.chartapp.view.data.DatePoint;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.GraphItem;
import com.kamer.chartapp.view.data.YGuides;
import com.kamer.chartapp.view.data.draw.DrawGraph;
import com.kamer.chartapp.view.data.draw.DrawSelection;
import com.kamer.chartapp.view.data.draw.DrawSelectionPoint;
import com.kamer.chartapp.view.data.draw.DrawSelectionPopup;
import com.kamer.chartapp.view.data.draw.DrawText;
import com.kamer.chartapp.view.data.draw.DrawYGuides;
import com.kamer.chartapp.view.data.draw.GraphDrawData;
import com.kamer.chartapp.view.data.draw.PreviewDrawData;
import com.kamer.chartapp.view.data.draw.PreviewMaskDrawData;
import com.kamer.chartapp.view.surface.ChartView;
import com.kamer.chartapp.view.utils.UnitConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartManager {

    private static final float MIN_VISIBLE_PART = 0.2f;
    private static final float PADDING_VERTICAL = UnitConverter.dpToPx(32);
    private static final int PADDING_TEXT_BOTTOM = (int) UnitConverter.dpToPx(8);
    private static final int PADDING_PREVIEW_VERTICAL = 15;

    private ChartView chartView;
    private PreviewView previewView;
    private PreviewMaskView previewMaskView;
    private UpdateListener updateListener;

    private Data data;

    private float leftBorder = 0.7f;
    private float rightBorder = 1f;
    private float pan = 0f;

    private float[] xAlphas = new float[0];
    private Map<String, Float> alphas = new HashMap<>();
    private Map<YGuides, Float> guideAlphas = new HashMap<>();
    private float minY;
    private float maxY = 1;
    private float totalMaxY = 1;
    private DrawSelection drawSelection;

    private List<Integer> datePointsIndexes = new ArrayList<>();

    private ValueAnimator currentAnimation;
    private ValueAnimator datesAnimation;

    private float xMarginPercent;
    private float xMarginPx = UnitConverter.dpToPx(16);

    @SuppressLint("ClickableViewAccessibility")
    public ChartManager(ChartView chartView, PreviewView previewView, PreviewMaskView previewMaskView, UpdateListener updateListener) {
        this.chartView = chartView;
        this.previewView = previewView;
        this.previewMaskView = previewMaskView;
        this.updateListener = updateListener;

        previewMaskView.setListener(new PreviewMaskView.Listener() {
            @Override
            public void onLeftBorderChanged(float dX) {
                setLeftBorder(leftBorder + dX);
            }

            @Override
            public void onRightBorderChanged(float dX) {
                setRightBorder(rightBorder + dX);
            }

            @Override
            public void onPanChanged(float dX) {
                setPan(pan + dX);
            }
        });
        chartView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    updateSelection(event.getX());
                }
                return true;
            }
        });
    }

    public void setData(final Data data) {
        chartView.post(new Runnable() {
            @Override
            public void run() {
                ChartManager.this.data = data;
                float[] targetRange = calculateTargetRange(leftBorder, rightBorder, true);
                minY = targetRange[0];
                maxY = targetRange[1];
                alphas.clear();
                guideAlphas.clear();
                guideAlphas.put(new YGuides(calculateYGuides(targetRange[0], targetRange[1]), true), 1f);

                xAlphas = new float[data.getDatePoints().size()];

                drawSelection = null;
                datePointsIndexes.clear();
                for (Integer datePointsIndex : datePointsIndexes) {
                    xAlphas[datePointsIndex] = 1;
                }
                recalculateXMargin();
//                updateHorizontalPosition();
                animateZoom();

                recalculateDates();
                animateDates();

                sync();
            }
        });
    }

    public void updateGraphEnabled(String name, boolean isEnabled) {
        for (int i = 0; i < data.getGraphs().size(); i++) {
            Graph graph = data.getGraphs().get(i);
            if (graph.getName().equals(name)) {
                data.getGraphs().set(i, new Graph(graph.getName(), graph.getColor(), graph.getItems(), graph.getPath(), isEnabled));
                drawSelection = null;
                animateZoom();
                sync();
                return;
            }
        }
    }

    private void setLeftBorder(float leftBorder) {
        float newLeft = leftBorder;
        float newVisiblePart = rightBorder - leftBorder;
        if (newVisiblePart + pan > 1) {
            newLeft = 0;
        } else if (newVisiblePart < MIN_VISIBLE_PART) {
            newLeft = rightBorder - MIN_VISIBLE_PART;
        }
        if (this.leftBorder != newLeft) {
            this.leftBorder = newLeft;
            drawSelection = null;
            recalculateXMargin();
            animateZoom();
//            updateHorizontalPosition();

            recalculateDates();
            animateDates();
        }
    }

    private void setRightBorder(float rightBorder) {
        float newRight;
        float newPan;
        if (rightBorder > 1) {
            newRight = 1;
            newPan = 0;
        } else if (rightBorder - leftBorder < MIN_VISIBLE_PART) {
            newRight = leftBorder + MIN_VISIBLE_PART;
            newPan = 1 - newRight;
        } else {
            newRight = rightBorder;
            newPan = 1 - rightBorder;
        }
        if (this.rightBorder != newRight || pan != newPan) {
            this.rightBorder = newRight;
            this.pan = newPan;
            drawSelection = null;
            recalculateXMargin();
            animateZoom();
//            updateHorizontalPosition();

            recalculateDates();
            animateDates();
        }
    }

    private void setPan(float pan) {
        float newPan = pan;
        if (visiblePartSize() + newPan > 1) {
            newPan = 1 - visiblePartSize();
        } else if (newPan < 0) {
            newPan = 0;
        }
        if (this.pan != newPan) {
            float diff = this.pan - newPan;
            this.pan = newPan;
            this.leftBorder += diff;
            this.rightBorder += diff;
            drawSelection = null;
            animateZoom();
//            updateHorizontalPosition();
        }
    }

    private void updateHorizontalPosition() {
        calculateDrawData();
        calculatePreviewDrawData(data.getGraphs());
        previewMaskView.invalidate();
        previewView.invalidate();
    }

    private void updateSelection(float x) {
        float localPercent = x / chartView.getWidth();
        float percent = visiblePartSize() * localPercent + leftBorder;

        int selectedIndex = 0;
        int lastInclusiveIndex = findLastInclusiveIndex(rightBorder, true);
        for (int i = findFirstInclusiveIndex(leftBorder, true); i < lastInclusiveIndex; i++) {
            float current = applyXMargin(data.getDatePoints().get(i).getPercent());
            float next = applyXMargin(data.getDatePoints().get(i + 1).getPercent());
            if (percent < current) {
                selectedIndex = i;
                break;
            } else if (percent > current && percent < next) {
                if (Math.abs(percent - current) < Math.abs(percent - next)) {
                    selectedIndex = i;
                } else {
                    selectedIndex = i + 1;
                }
                break;
            } else if (percent > next && i + 1 == lastInclusiveIndex) {
                selectedIndex = i + 1;
                break;
            }
        }

        DatePoint datePoint = data.getDatePoints().get(selectedIndex);
        float selectedPercent = applyXMargin(datePoint.getPercent());
        float realX = chartView.getWidth() * calcPercent(selectedPercent, leftBorder, rightBorder);

        int border;
        boolean isAlignedRight;
        if (localPercent > 0.5) {
            border = (int) (chartView.getWidth() * calcPercent(selectedPercent, leftBorder, rightBorder) - 50);
            isAlignedRight = true;
        } else {
            border = (int) (chartView.getWidth() * calcPercent(selectedPercent, leftBorder, rightBorder) + 50);
            isAlignedRight = false;
        }

        List<DrawSelectionPoint> points = new ArrayList<>();
        List<Pair<String, Integer>> texts = new ArrayList<>();
        List<Graph> graphs = data.getGraphs();
        for (int i = 0; i < graphs.size(); i++) {
            Graph graph = graphs.get(i);
            GraphItem graphItem = graph.getItems().get(selectedIndex);
            float realY = calculateYFromPercent(chartView.getHeight(), graphItem.getPercent(), minY, maxY, PADDING_VERTICAL);
            points.add(new DrawSelectionPoint(realX, realY, graph.getColor()));
            texts.add(new Pair<>(graphItem.getValue() + "", graph.getColor()));
        }

        DrawSelectionPopup popup = new DrawSelectionPopup(
                border,
                isAlignedRight,
                datePoint.getTextExtended(),
                texts
        );
        drawSelection = new DrawSelection(realX, points, popup);

        calculateDrawData();
    }

    private boolean isIndexFit(int index) {
        if (index >= data.getDatePoints().size()) return false;
        float dateSize = 0.13f * visiblePartSize();
        float percent = applyXMargin(data.getDatePoints().get(index).getPercent());
        return percent - dateSize / 2 > 0 && percent + dateSize / 2 < 1;
    }

    private boolean isIndexesCollide(int index1, int index2) {
        float dateSize = 0.13f * visiblePartSize();
        float percent1 = applyXMargin(data.getDatePoints().get(index1).getPercent());
        float percent2 = applyXMargin(data.getDatePoints().get(index2).getPercent());
        return Math.abs(percent1 - percent2) < dateSize;
    }

    private void recalculateDates() {
        int lastIndex = data.getDatePoints().size() - 1;
        int startIndex;
        if (!datePointsIndexes.isEmpty()) {
            startIndex = lastIndex - datePointsIndexes.get(0);
        } else {
            startIndex = 0;
            while (!isIndexFit(startIndex)) {
                startIndex++;
            }
        }
        List<Integer> indexes = new ArrayList<>();
        indexes.add(lastIndex - startIndex);
        int nextIndex;
        int i = 0;
        do {
            nextIndex = startIndex + (int) Math.pow(2, i);
            i++;
        }
        while ((!isIndexFit(nextIndex) || isIndexesCollide(startIndex, nextIndex)) && nextIndex < data.getDatePoints().size());
        if (isIndexFit(nextIndex)) {
            indexes.add(lastIndex - nextIndex);
            int diff = nextIndex - startIndex;
            int index = nextIndex + diff;
            while (isIndexFit(index)) {
                indexes.add(lastIndex - index);
                index = index + diff;
            }
        }
        datePointsIndexes = indexes;
    }

    private void sync() {
        updateListener.onUpdate(data.getGraphs());
    }

    private void calculateDrawData() {
        ArrayList<DrawGraph> result = new ArrayList<>();


        int width = chartView.getWidth();
        int height = chartView.getHeight();

        for (Graph graph : data.getGraphs()) {
            Path path = scalePath(width, height, graph.getPath(), minY, maxY, leftBorder, rightBorder, PADDING_VERTICAL, xMarginPx);

            float alpha1 = getAlpha(graph.getName());
            result.add(new DrawGraph(graph.getColor(), path, (int) (alpha1 * 255)));
        }


        //TODO: move it somewhere
        List<DrawYGuides> drawYGuides = new ArrayList<>();
        for (Map.Entry<YGuides, Float> yGuidesFloatEntry : guideAlphas.entrySet()) {
            YGuides guide = yGuidesFloatEntry.getKey();
            float[] yLines = new float[guide.getPercent().length * 4];
            List<DrawText> drawTexts = new ArrayList<>();
            int alpha = (int) (yGuidesFloatEntry.getValue() * 255);
            for (int i = 0; i < guide.getPercent().length; i++) {
                float y = calculateYFromPercent(chartView.getHeight(), guide.getPercent()[i], minY, maxY, PADDING_VERTICAL);
                yLines[i * 4] = UnitConverter.dpToPx(16);
                yLines[i * 4 + 1] = y;
                yLines[i * 4 + 2] = chartView.getWidth() - UnitConverter.dpToPx(16);
                yLines[i * 4 + 3] = y;

                int value = Math.round((data.getMaxValue() - data.getMinValue()) * guide.getPercent()[i] + data.getMinValue());
                String text = value + "";
                drawTexts.add(new DrawText(text, UnitConverter.dpToPx(16), y - UnitConverter.dpToPx(8), alpha));
            }
            drawYGuides.add(new DrawYGuides(yLines, drawTexts, alpha));
        }

        ArrayList<DrawText> xLabels = new ArrayList<>();
        for (int i = 0; i < xAlphas.length; i++) {
            if (xAlphas[i] > 0) {
                DatePoint datePoint = data.getDatePoints().get(i);
                float xPercent = datePoint.getPercent();
                int x = (int) (width * calcPercent(applyXMargin(xPercent), leftBorder, rightBorder));
                xLabels.add(new DrawText(datePoint.getText(), x, height - PADDING_TEXT_BOTTOM, (int) (xAlphas[i] * 255)));
            }
        }

        chartView.setDrawData(new GraphDrawData(result, drawYGuides, xLabels, drawSelection));
    }

    private void calculatePreviewDrawData(List<Graph> graphs) {
        List<DrawGraph> result = new ArrayList<>();
        int width = previewView.getWidth();
        int height = previewView.getHeight();

        for (Graph graph : graphs) {
            Path path = scalePath(width, height, graph.getPath(), 0, totalMaxY, 0, 1, PADDING_PREVIEW_VERTICAL, 0);

            float alpha = getAlpha(graph.getName());
            result.add(new DrawGraph(graph.getColor(), path, ((int) (255 * alpha))));
        }

        previewView.setDrawData(new PreviewDrawData(result));
        previewMaskView.setDrawData(new PreviewMaskDrawData(
                previewMaskView.getWidth() * leftBorder,
                previewMaskView.getWidth() * rightBorder
        ));
    }

    private Path scalePath(int width, int height, Path path, float minY, float maxY, float minX, float maxX, float paddingVertical, float paddingHorizontal) {

        float scaledWidth = width * 1f / (maxX - minX);
        float scaledHeight = (height - paddingVertical * 2) * (1f / (maxY - minY));

        Path result = new Path(path);

        //scale
        Matrix scale = new Matrix();
        scale.setScale(scaledWidth, -scaledHeight);
        result.transform(scale);

        //move
        float moveX = -minX * scaledWidth;
        float moveY = scaledHeight - (1 - maxY) * scaledHeight + paddingVertical;
        result.offset(moveX, moveY);

        //x insets
        Matrix inset = new Matrix();
        RectF bounds = new RectF();
        result.computeBounds(bounds, true);
        RectF boundsWithInsets = new RectF(bounds);
        boundsWithInsets.inset(paddingHorizontal, 0);
        inset.setRectToRect(bounds, boundsWithInsets, Matrix.ScaleToFit.FILL);
        result.transform(inset);

        return result;
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

    private int calculateYFromPercent(int height, float y, float minYPercent, float maxYPercent, float padding) {
        int heightWithPadding = height - (int) padding * 2;
        return ((int) (heightWithPadding - heightWithPadding * calcPercent(y, minYPercent, maxYPercent))) + (int) padding;
    }

    private float visiblePartSize() {
        return rightBorder - leftBorder;
    }

    private float calcPercent(float value, float start, float end) {
        return (value - start) / (end - start);
    }

    private void animateDates() {
        if (datesAnimation != null && datesAnimation.isRunning()) return;
        float[] targetX = new float[xAlphas.length];
        for (int i = 0; i < xAlphas.length; i++) {
            targetX[i] = datePointsIndexes.contains(i) ? 1f : 0f;
        }
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
        if (datesAnimation != null) {
            datesAnimation.cancel();
        }
        ValueAnimator animator = new ValueAnimator();
        animator.setValues(properties);
        animator.setDuration(100);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                for (int i1 = 0; i1 < xAlphas.length; i1++) {
                    if (valueAnimator.getAnimatedValue(i1 + "") != null) {
                        xAlphas[i1] = (float) valueAnimator.getAnimatedValue(i1 + "");
                    }
                    calculateDrawData();
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animateDates();
            }
        });
        datesAnimation = animator;
        animator.start();
    }

    private void animateZoom() {
        float[] targetRange = calculateTargetRange(leftBorder, rightBorder, false);
        float[] totalRange = calculateTargetRange(0f, 1f, false);
        List<Graph> graphs = data.getGraphs();
        float[] percents = calculateYGuides(targetRange[0], targetRange[1]);
        YGuides targetGuides = new YGuides(percents, true);
        targetRange = new float[]{percents[0], targetRange[1]};
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

        PropertyValuesHolder[] properties = new PropertyValuesHolder[graphs.size() + 3 + guideAlphas.size()];
        properties[0] = PropertyValuesHolder.ofFloat("minY", minY, targetRange[0]);
        properties[1] = PropertyValuesHolder.ofFloat("maxY", maxY, targetRange[1]);
        properties[2] = PropertyValuesHolder.ofFloat("totalMaxY", totalMaxY, totalRange[1]);
        for (int i = 0; i < graphs.size(); i++) {
            Graph graph = graphs.get(i);
            String name = graph.getName();
            properties[i + 3] = PropertyValuesHolder.ofFloat(name, getAlpha(name), graph.isEnabled() ? 1f : 0f);
        }
        int i = 0;
        for (Map.Entry<YGuides, Float> yGuidesFloatEntry : guideAlphas.entrySet()) {
            properties[i + 3 + graphs.size()] = PropertyValuesHolder.ofFloat(yGuidesFloatEntry.getKey().hashCode() + "", yGuidesFloatEntry.getValue(), yGuidesFloatEntry.getKey().isActive() ? 1f : 0f);
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
                float newMin = (float) valueAnimator.getAnimatedValue("minY");
                float newMax = (float) valueAnimator.getAnimatedValue("maxY");
                float newTotalMax = (float) valueAnimator.getAnimatedValue("totalMaxY");
                HashMap<String, Float> newAlphas = new HashMap<>();
                for (Graph graph : data.getGraphs()) {
                    Object animatedValue = valueAnimator.getAnimatedValue(graph.getName());
                    float alpha = animatedValue != null ? (float) animatedValue : 1f;
                    newAlphas.put(graph.getName(), alpha);
                }
                List<YGuides> keys = new ArrayList<>(guideAlphas.keySet());
                for (YGuides guides : keys) {
                    float value = (float) valueAnimator.getAnimatedValue(guides.hashCode() + "");
                    guideAlphas.remove(guides);
                    if (!(value == 0 && !guides.isActive())) {
                        guideAlphas.put(guides, value);
                    }
                }
                minY = newMin;
                maxY = newMax;
                totalMaxY = newTotalMax;

                alphas = newAlphas;
                calculateDrawData();

                calculatePreviewDrawData(data.getGraphs());
                previewView.invalidate();
                previewMaskView.invalidate();
            }
        });
        currentAnimation = animator;
        animator.start();
    }

    private float[] calculateTargetRange(float startXPercentage, float endXPercentage, boolean withMargin) {
        float yMin = 1;
        float yMax = 0;

        int firstInclusiveIndex = findFirstInclusiveIndex(startXPercentage, withMargin);
        int lastInclusiveIndex = findLastInclusiveIndex(endXPercentage, withMargin);

        for (Graph graph : data.getGraphs()) {
            if (!graph.isEnabled()) continue;
            List<GraphItem> graphItems = graph.getItems();

            if (firstInclusiveIndex > 0) {
                float startYPercentage = calcYAtXByTwoPoints(
                        startXPercentage,
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
                        endXPercentage,
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

    private float getAlpha(String name) {
        Float animatedAlpha = alphas.get(name);
        return animatedAlpha != null ? animatedAlpha : 1f;
    }

    private int findFirstInclusiveIndex(float startXPercentage, boolean respectMargin) {
        for (int i = 0; i < data.getDatePoints().size(); i++) {
            float value = data.getDatePoints().get(i).getPercent();
            if (respectMargin) {
                value = applyXMargin(value);
            }
            if (value > startXPercentage || isFloatEquals(value, startXPercentage)) {
                return i;
            }
        }
        return -1;
    }

    private int findLastInclusiveIndex(float endXPercentage, boolean respectMargins) {
        for (int i = data.getDatePoints().size() - 1; i >= 0; i--) {
            float value = data.getDatePoints().get(i).getPercent();
            if (respectMargins) {
                value = applyXMargin(value);
            }
            if (value < endXPercentage || isFloatEquals(value, endXPercentage)) {
                return i;
            }
        }
        return -1;
    }

    private float applyXMargin(float x) {
        return x * (1 - xMarginPercent * 2) + xMarginPercent;
    }

    private float calcYAtXByTwoPoints(float x, float x1, float y1, float x2, float y2) {
        return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
    }

    private boolean isFloatEquals(float f1, float f2) {
        return Math.abs(f1 - f2) < 0.0001;
    }

    private void recalculateXMargin() {
        xMarginPercent = xMarginPx / (chartView.getWidth() / visiblePartSize());
    }

    public interface UpdateListener {

        void onUpdate(List<Graph> graphs);

    }

}
