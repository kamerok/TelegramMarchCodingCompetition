package com.kamer.chartapp.view;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Path;
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
import com.kamer.chartapp.view.data.draw.DrawText;
import com.kamer.chartapp.view.data.draw.DrawYGuides;
import com.kamer.chartapp.view.data.draw.GraphDrawData;
import com.kamer.chartapp.view.data.draw.PreviewDrawData;
import com.kamer.chartapp.view.data.draw.PreviewMaskDrawData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartManager {

    private static final float MIN_VISIBLE_PART = 0.2f;
    private static final int PADDING_VERTICAL = 50;
    private static final int PADDING_PREVIEW_VERTICAL = 15;

    private ChartView chartView;
    private PreviewView previewView;
    private PreviewMaskView previewMaskView;
    private UpdateListener updateListener;

    private Data data;

    private float leftBorder = 0.7f;
    private float rightBorder = 1f;
    private float pan = 0f;

    private Map<String, Float> alphas = new HashMap<>();
    private Map<YGuides, Float> guideAlphas = new HashMap<>();
    private float minY;
    private float maxY = 1;
    private float totalMinY;
    private float totalMaxY = 1;
    private DrawSelection drawSelection;

    private List<Integer> datePointsIndexes = new ArrayList<>();

    private ValueAnimator currentAnimation;

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
                float[] targetRange = calculateTargetRange(1 - (visiblePartSize() + pan), 1 - (visiblePartSize() + pan) + visiblePartSize());
                guideAlphas.put(new YGuides(yGuides(targetRange[0], targetRange[1]), true), 1f);

                datePointsIndexes.clear();
                recalculateDates();

                animateZoom();

                sync();
            }
        });
    }

    public void updateGraphEnabled(String name, boolean isEnabled) {
        for (int i = 0; i < data.getGraphs().size(); i++) {
            Graph graph = data.getGraphs().get(i);
            if (graph.getName().equals(name)) {
                data.getGraphs().set(i, new Graph(graph.getName(), graph.getColor(), graph.getItems(), isEnabled));
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
            recalculateDates();
            animateZoom();
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
            recalculateDates();
            animateZoom();
        }
    }

    private void updateSelection(float x) {
        float localPercent = x / chartView.getWidth();
        float percent = visiblePartSize() * localPercent + leftBorder;

        int selectedIndex = 0;
        int lastInclusiveIndex = findLastInclusiveIndex(rightBorder);
        for (int i = findFirstInclusiveIndex(leftBorder); i < lastInclusiveIndex; i++) {
            float current = data.getDatePoints().get(i).getPercent();
            float next = data.getDatePoints().get(i + 1).getPercent();
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

        float selectedPercent = data.getDatePoints().get(selectedIndex).getPercent();
        ArrayList<DrawSelectionPoint> points = new ArrayList<>();
        List<Graph> graphs = data.getGraphs();
        float realX = chartView.getWidth() * calcPercent(selectedPercent, leftBorder, rightBorder);
        for (int i = 0; i < graphs.size(); i++) {
            Graph graph = graphs.get(i);
            float realY = calculateYFromPercent(chartView.getHeight(), graph.getItems().get(selectedIndex).getPercent(), minY, maxY, PADDING_VERTICAL);
            points.add(new DrawSelectionPoint(realX, realY, graph.getColor()));
        }
        drawSelection = new DrawSelection(realX, points);

        calculateDrawData();
        chartView.invalidate();
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
        }
    }

    private boolean isIndexFit(int index) {
        if (index >= data.getDatePoints().size()) return false;
        float dateSize = (rightBorder - leftBorder) / 4;
        float percent = data.getDatePoints().get(index).getPercent();
        return percent - dateSize / 2 > 0 && percent + dateSize / 2 < 1;
    }

    private void recalculateDates() {
        int startIndex = 0;
        while (!isIndexFit(startIndex)) {
            startIndex = startIndex * 2 + 1;
        }
        List<Integer> indexes = new ArrayList<>();
        indexes.add(startIndex);
        int nextIndex = startIndex * 2 + 1;
        if (isIndexFit(nextIndex)) {
            indexes.add(nextIndex);
            int diff = nextIndex - startIndex;
            int index = nextIndex + diff;
            while (isIndexFit(index)) {
                indexes.add(index);
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
        int firstInclusiveIndex = findFirstInclusiveIndex(leftBorder);
        int lastInclusiveIndex = findLastInclusiveIndex(rightBorder);

        for (Graph graph : data.getGraphs()) {
            List<GraphItem> graphItems = graph.getItems();

            Pair<Float, Float> start = null, end = null;

            if (firstInclusiveIndex > 0) {
                float startYPercentage = calcYAtXByTwoPoints(
                        leftBorder,
                        data.getDatePoints().get(firstInclusiveIndex - 1).getPercent(),
                        graphItems.get(firstInclusiveIndex - 1).getPercent(),
                        data.getDatePoints().get(firstInclusiveIndex).getPercent(),
                        graphItems.get(firstInclusiveIndex).getPercent()
                );
                start = new Pair<>(leftBorder, startYPercentage);
            }

            if (lastInclusiveIndex < graphItems.size() - 1) {
                float endYPercentage = calcYAtXByTwoPoints(
                        rightBorder,
                        data.getDatePoints().get(lastInclusiveIndex).getPercent(),
                        graphItems.get(lastInclusiveIndex).getPercent(),
                        data.getDatePoints().get(lastInclusiveIndex + 1).getPercent(),
                        graphItems.get(lastInclusiveIndex + 1).getPercent()
                );
                end = new Pair<>(rightBorder, endYPercentage);
            }

            List<Pair<Float, Float>> items = new ArrayList<>();
            if (start != null) items.add(start);
            for (int i = firstInclusiveIndex; i <= lastInclusiveIndex; i++) {
                items.add(new Pair<>(
                        data.getDatePoints().get(i).getPercent(),
                        graphItems.get(i).getPercent()
                ));
            }
            if (end != null) items.add(end);

            Path path = getPathForGraphItems(items, width, height, minY, maxY, leftBorder, rightBorder, PADDING_VERTICAL);
            float alpha = getAlpha(graph.getName());

            result.add(new DrawGraph(graph.getColor(), path, (int) (alpha * 255)));
        }

        List<DrawYGuides> drawYGuides = new ArrayList<>();
        for (Map.Entry<YGuides, Float> yGuidesFloatEntry : guideAlphas.entrySet()) {
            YGuides guide = yGuidesFloatEntry.getKey();
            float[] yLines = new float[guide.getPercent().length * 4];
            List<DrawText> drawTexts = new ArrayList<>();
            for (int i = 0; i < guide.getPercent().length; i++) {
                float y = chartView.getHeight() - chartView.getHeight() * calcPercent(guide.getPercent()[i], minY, maxY);
                yLines[i * 4] = 0;
                yLines[i * 4 + 1] = y;
                yLines[i * 4 + 2] = chartView.getWidth();
                yLines[i * 4 + 3] = y;

                String text = ((data.getMaxValue() - data.getMinValue()) * guide.getPercent()[i] + data.getMinValue()) + "";
                drawTexts.add(new DrawText(text, 0, y));
            }
            drawYGuides.add(new DrawYGuides(yLines, drawTexts, ((int) (yGuidesFloatEntry.getValue() * 255))));
        }

        ArrayList<DrawText> xLabels = new ArrayList<>();
        for (Integer datePointIndex : datePointsIndexes) {
            DatePoint datePoint = data.getDatePoints().get(datePointIndex);
            float xPercent = datePoint.getPercent();
            int x = (int) (width * calcPercent(xPercent, leftBorder, rightBorder));
            xLabels.add(new DrawText(datePoint.getText(), x, height));
        }

        chartView.setDrawData(new GraphDrawData(result, drawYGuides, xLabels, drawSelection));
    }

    private float[] yGuides(float minY, float maxY) {
        int count = 6;
        float[] guides = new float[count];
        float segment = Math.abs(maxY - minY) / count;
        for (int i = 0; i < count; i++) {
            guides[i] = segment * i + minY + segment / 2;
        }
        return guides;
    }

    private void calculatePreviewDrawData(List<Graph> graphs) {
        List<DrawGraph> result = new ArrayList<>();
        int width = previewView.getWidth();
        int height = previewView.getHeight();

        for (Graph graph : graphs) {
            List<Pair<Float, Float>> graphItems = new ArrayList<>();
            for (int i = 0; i < graph.getItems().size(); i++) {
                graphItems.add(new Pair<>(data.getDatePoints().get(i).getPercent(), graph.getItems().get(i).getPercent()));
            }

            Path path = getPathForGraphItems(graphItems, width, height, totalMinY, totalMaxY, 0, 1, PADDING_PREVIEW_VERTICAL);

            float alpha = getAlpha(graph.getName());
            result.add(new DrawGraph(graph.getColor(), path, ((int) (255 * alpha))));
        }

        previewView.setDrawData(new PreviewDrawData(result));
        previewMaskView.setDrawData(new PreviewMaskDrawData(
                previewMaskView.getWidth() * leftBorder,
                previewMaskView.getWidth() * rightBorder
        ));
    }

    private Path getPathForGraphItems(
            List<Pair<Float, Float>> items,
            int width, int height,
            float minY, float maxY,
            float minX, float maxX,
            int verticalPadding
    ) {
        Path path = new Path();
        for (int i = 1; i < items.size(); i++) {
            Pair<Float, Float> start = items.get(i - 1);
            Pair<Float, Float> end = items.get(i);
            int startX = (int) (width * calcPercent(start.first, minX, maxX));
            int startY = calculateYFromPercent(height, start.second, minY, maxY, verticalPadding);
            int stopX = (int) (width * calcPercent(end.first, minX, maxX));
            int stopY = calculateYFromPercent(height, end.second, minY, maxY, verticalPadding);

            if (path.isEmpty()) {
                path.moveTo(startX, startY);
            }
            path.lineTo(stopX, stopY);
        }
        return path;
    }

    private int calculateYFromPercent(int height, float y, float minYPercent, float maxYPercent, int padding) {
        int heightWithPadding = height - padding * 2;
        return ((int) (heightWithPadding - heightWithPadding * calcPercent(y, minYPercent, maxYPercent))) + padding;
    }

    private float visiblePartSize() {
        return rightBorder - leftBorder;
    }

    private float calcPercent(float value, float start, float end) {
        return (value - start) / (end - start);
    }

    private void animateZoom() {
        float[] targetRange = calculateTargetRange(1 - (visiblePartSize() + pan), 1 - (visiblePartSize() + pan) + visiblePartSize());
        float[] totalRange = calculateTargetRange(0f, 1f);
        List<Graph> graphs = data.getGraphs();
        YGuides targetGuides = new YGuides(yGuides(targetRange[0], targetRange[1]), true);
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

        PropertyValuesHolder[] properties = new PropertyValuesHolder[graphs.size() + 4 + guideAlphas.size()];
        properties[0] = PropertyValuesHolder.ofFloat("minY", minY, targetRange[0]);
        properties[1] = PropertyValuesHolder.ofFloat("maxY", maxY, targetRange[1]);
        properties[2] = PropertyValuesHolder.ofFloat("totalMinY", totalMinY, totalRange[0]);
        properties[3] = PropertyValuesHolder.ofFloat("totalMaxY", totalMaxY, totalRange[1]);
        for (int i = 0; i < graphs.size(); i++) {
            Graph graph = graphs.get(i);
            String name = graph.getName();
            properties[i + 4] = PropertyValuesHolder.ofFloat(name, getAlpha(name), graph.isEnabled() ? 1f : 0f);
        }
        int i = 0;
        for (Map.Entry<YGuides, Float> yGuidesFloatEntry : guideAlphas.entrySet()) {
            properties[i + 4 + graphs.size()] = PropertyValuesHolder.ofFloat(yGuidesFloatEntry.getKey().hashCode() + "", yGuidesFloatEntry.getValue(), yGuidesFloatEntry.getKey().isActive() ? 1f : 0f);
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
                float newTotalMin = (float) valueAnimator.getAnimatedValue("totalMinY");
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
                totalMinY = newTotalMin;
                totalMaxY = newTotalMax;

                alphas = newAlphas;
                calculateDrawData();
                chartView.invalidate();

                calculatePreviewDrawData(data.getGraphs());
                previewView.invalidate();
                previewMaskView.invalidate();
            }
        });
        currentAnimation = animator;
        animator.start();
    }

    private float[] calculateTargetRange(float startXPercentage, float endXPercentage) {
        float yMin = 1;
        float yMax = 0;

        int firstInclusiveIndex = findFirstInclusiveIndex(startXPercentage);
        int lastInclusiveIndex = findLastInclusiveIndex(endXPercentage);

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

    private int findFirstInclusiveIndex(float startXPercentage) {
        for (int i = 0; i < data.getDatePoints().size(); i++) {
            float value = data.getDatePoints().get(i).getPercent();
            if (value > startXPercentage || isFloatEquals(value, startXPercentage)) {
                return i;
            }
        }
        return -1;
    }

    private int findLastInclusiveIndex(float endXPercentage) {
        for (int i = data.getDatePoints().size() - 1; i >= 0; i--) {
            float value = data.getDatePoints().get(i).getPercent();
            if (value < endXPercentage || isFloatEquals(value, endXPercentage)) {
                return i;
            }
        }
        return -1;
    }

    private float calcYAtXByTwoPoints(float x, float x1, float y1, float x2, float y2) {
        return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
    }

    private boolean isFloatEquals(float f1, float f2) {
        return Math.abs(f1 - f2) < 0.0001;
    }

    public interface UpdateListener {

        void onUpdate(List<Graph> graphs);

    }

}
