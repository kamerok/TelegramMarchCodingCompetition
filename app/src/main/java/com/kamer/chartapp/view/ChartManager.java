package com.kamer.chartapp.view;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.support.annotation.FloatRange;

import com.kamer.chartapp.view.data.GraphDrawData;
import com.kamer.chartapp.view.data.DrawGraph;
import com.kamer.chartapp.view.data.DrawItem;
import com.kamer.chartapp.view.data.DrawText;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.GraphItem;
import com.kamer.chartapp.view.data.InputGraph;
import com.kamer.chartapp.view.data.InputItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartManager {

    private static final float MIN_VISIBLE_PART = 0.1f;
    private static final int PADDING_VERTICAL = 50;

    private ChartView chartView;
    private PreviewView previewView;
    private UpdateListener updateListener;

    private List<InputGraph> inputGraphs = new ArrayList<>();
    private List<Graph> graphs = new ArrayList<>();

    private float leftBorder = 0f;
    private float rightBorder = 1f;
    private float pan = 0f;

    private Map<String, Float> alphas = new HashMap<>();
    private float minY;
    private float maxY = 1;
    private float totalMinY;
    private float totalMaxY = 1;

    private ValueAnimator currentAnimation;

    public ChartManager(ChartView chartView, PreviewView previewView, UpdateListener updateListener) {
        this.chartView = chartView;
        this.previewView = previewView;
        this.updateListener = updateListener;
    }

    public void setData(final List<InputGraph> data) {
        chartView.post(new Runnable() {
            @Override
            public void run() {
                inputGraphs = data;

                graphs = calculateGraphs(data);
                calculateDrawData();
                animateZoom();

                sync();
            }
        });
    }

    public void setLeftBorder(@FloatRange(from = 0, to = 1) float leftBorder) {
        float newLeft = leftBorder;
        float newVisiblePart = rightBorder - leftBorder;
        if (newVisiblePart + pan > 1) {
            newLeft = 0;
        } else if (newVisiblePart < MIN_VISIBLE_PART) {
            newLeft = rightBorder - MIN_VISIBLE_PART;
        }
        if (this.leftBorder != newLeft) {
            this.leftBorder = newLeft;
            calculateDrawData();
            animateZoom();
        }

        sync();
    }

    public void setRightBorder(@FloatRange(from = 0, to = 1) float rightBorder) {
        float newRight;
        float newPan = pan;
        if (rightBorder > 1) {
            newRight = 1;
            pan = 0;
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
            calculateDrawData();
            animateZoom();
        }

        sync();
    }

    public void setPan(@FloatRange(from = 0, to = 1) float pan) {
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
            calculateDrawData();
            animateZoom();
        }

        sync();
    }

    public void updateGraphEnabled(String name, boolean isEnabled) {
        for (int i = 0; i < inputGraphs.size(); i++) {
            InputGraph graph = inputGraphs.get(i);
            if (graph.getName().equals(name)) {
                inputGraphs.set(i, new InputGraph(graph.getValues(), graph.getColor(), graph.getName(), isEnabled));
                setData(inputGraphs);
                sync();
                return;
            }
        }
    }

    private void sync() {
        previewView.setData(graphs, leftBorder, rightBorder);
        updateListener.onUpdate(leftBorder, rightBorder, pan, inputGraphs);
    }

    private List<Graph> calculateGraphs(List<InputGraph> inputGraphs) {
        List<Graph> result = new ArrayList<>();

        List<InputItem> allItems = new ArrayList<>();
        for (InputGraph inputGraph : inputGraphs) {
            allItems.addAll(inputGraph.getValues());
        }
        if (allItems.isEmpty()) return result;
        long[] range = calculateYRange(allItems);
        long min = range[0];
        long max = range[1];
        long verticalLength = Math.abs(min - max);

        for (InputGraph inputGraph : inputGraphs) {
            List<GraphItem> items = new ArrayList<>();
            List<InputItem> data = inputGraph.getValues();
            for (int i = 0; i < data.size(); i++) {
                float x = (float) i / (data.size() - 1);
                float y = Math.abs(min - data.get(i).getValue()) / (float) verticalLength;
                items.add(new GraphItem(x, y));
            }
            result.add(new Graph(inputGraph.getName(), inputGraph.getColor(), items, inputGraph.isEnabled()));
        }
        return result;
    }

    private int findFirstIndexAfterPercent(float percent, List<GraphItem> items) {
        for (int i = 0; i < items.size() - 2; i++) {
            GraphItem current = items.get(i);
            GraphItem next = items.get(i + 1);
            if ((isFloatEquals(current.getX(), percent) || current.getX() < percent) && next.getX() > percent) {
                return i + 1;
            }
        }
        return -1;
    }

    private int findLastIndexBeforePercent(float percent, List<GraphItem> items) {
        for (int i = items.size() - 1; i >= 1; i--) {
            GraphItem current = items.get(i);
            GraphItem previous = items.get(i - 1);
            if ((current.getX() > percent || isFloatEquals(current.getX(), percent)) && previous.getX() < percent) {
                return i - 1;
            }
        }
        return -1;
    }

    private float calcYAtXByTwoPoints(float x, float x1, float y1, float x2, float y2) {
        return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
    }

    private void calculateDrawData() {
        ArrayList<DrawGraph> result = new ArrayList<>();

        for (Graph graph : graphs) {
            List<GraphItem> graphItems = graph.getItems();
            float startXPercentage = 1 - (visiblePartSize() + pan);
            int firstInclusiveIndex = findFirstIndexAfterPercent(startXPercentage, graphItems);
            float startYPercentage = calcYAtXByTwoPoints(
                    startXPercentage,
                    graphItems.get(firstInclusiveIndex - 1).getX(),
                    graphItems.get(firstInclusiveIndex - 1).getY(),
                    graphItems.get(firstInclusiveIndex).getX(),
                    graphItems.get(firstInclusiveIndex).getY()
            );

            float endXPercentage = startXPercentage + visiblePartSize();
            int lastInclusiveIndex = findLastIndexBeforePercent(endXPercentage, graphItems);
            float endYPercentage = calcYAtXByTwoPoints(
                    endXPercentage,
                    graphItems.get(lastInclusiveIndex).getX(),
                    graphItems.get(lastInclusiveIndex).getY(),
                    graphItems.get(lastInclusiveIndex + 1).getX(),
                    graphItems.get(lastInclusiveIndex + 1).getY()
            );

            List<DrawItem> drawData = new ArrayList<>();
            int width = chartView.getWidth();
            int height = chartView.getHeight();

            GraphItem first = graphItems.get(firstInclusiveIndex);
            float newXPercent = calcPercent(first.getX(), startXPercentage, endXPercentage);
            drawData.add(new DrawItem(
                    0, calculateYFromPercent(height, startYPercentage, minY, maxY),
                    (int) (newXPercent * width), calculateYFromPercent(height, first.getY(), minY, maxY)
            ));

            for (int i = firstInclusiveIndex + 1; i <= lastInclusiveIndex; i++) {
                GraphItem start = graphItems.get(i - 1);
                GraphItem end = graphItems.get(i);
                int startX = (int) (width * calcPercent(start.getX(), startXPercentage, endXPercentage));
                int startY = calculateYFromPercent(height, start.getY(), minY, maxY);
                int stopX = (int) (width * calcPercent(end.getX(), startXPercentage, endXPercentage));
                int stopY = calculateYFromPercent(height, end.getY(), minY, maxY);
                drawData.add(new DrawItem(startX, startY, stopX, stopY));
            }

            GraphItem last = graphItems.get(lastInclusiveIndex);
            newXPercent = calcPercent(last.getX(), startXPercentage, endXPercentage);
            drawData.add(new DrawItem(
                    (int) (newXPercent * width), calculateYFromPercent(height, last.getY(), minY, maxY),
                    width, calculateYFromPercent(height, endYPercentage, minY, maxY)
            ));

            Path path = new Path();
            for (DrawItem item : drawData) {
                if (path.isEmpty()) {
                    path.moveTo(item.getStartX(), item.getStartY());
                }
                path.lineTo(item.getStopX(), item.getStopY());
            }
            float alpha = getAlpha(graph.getName());
            result.add(new DrawGraph(graph.getColor(), path, (int) (alpha * 255)));
        }

        float[] yLines = new float[6 * 4];
        List<DrawText> drawTexts = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            float segment = Math.abs(maxY - minY) / 6;
            float percent = 1 - calcPercent(((segment) * i + minY), minY, maxY);
            float y = chartView.getHeight() * percent;
            yLines[i * 4] = 0;
            yLines[i * 4 + 1] = y;
            yLines[i * 4 + 2] = chartView.getWidth();
            yLines[i * 4 + 3] = y;

            float realPercent = 1 - percent;
            drawTexts.add(new DrawText(realPercent + "", 0, y));
        }

        chartView.setDrawData(new GraphDrawData(result, yLines, drawTexts));
    }

    private int calculateYFromPercent(int height, float y, float minYPercent, float maxYPercent) {
        int heightWithPadding = height - PADDING_VERTICAL * 2;
        return ((int) (heightWithPadding - heightWithPadding * calcPercent(y, minYPercent, maxYPercent))) + PADDING_VERTICAL;
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


        PropertyValuesHolder[] properties = new PropertyValuesHolder[graphs.size() + 4];
        properties[0] = PropertyValuesHolder.ofFloat("minY", minY, targetRange[0]);
        properties[1] = PropertyValuesHolder.ofFloat("maxY", maxY, targetRange[1]);
        properties[2] = PropertyValuesHolder.ofFloat("totalMinY", totalMinY, totalRange[0]);
        properties[3] = PropertyValuesHolder.ofFloat("totalMaxY", totalMaxY, totalRange[1]);
        for (int i = 0; i < graphs.size(); i++) {
            Graph graph = graphs.get(i);
            String name = graph.getName();
            properties[i + 4] = PropertyValuesHolder.ofFloat(name, getAlpha(name), graph.isEnabled() ? 1f : 0f);
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
                for (Graph graph : graphs) {
                    Object animatedValue = valueAnimator.getAnimatedValue(graph.getName());
                    float alpha = animatedValue != null ? (float) animatedValue : 1f;
                    newAlphas.put(graph.getName(), alpha);
                }
                minY = newMin;
                maxY = newMax;
                totalMinY = newTotalMin;
                totalMaxY = newTotalMax;

                alphas = newAlphas;
                calculateDrawData();
                chartView.invalidate();

                previewView.onValuesUpdated(newTotalMin, newTotalMax, newAlphas);
            }
        });
        currentAnimation = animator;
        animator.start();
    }

    private float[] calculateTargetRange(float startXPercentage, float endXPercentage) {
        float yMin = 1;
        float yMax = 0;

        for (Graph graph : graphs) {
            if (!graph.isEnabled()) continue;
            List<GraphItem> graphItems = graph.getItems();
            int firstInclusiveIndex = findFirstIndexAfterPercent(startXPercentage, graphItems);
            float startYPercentage = calcYAtXByTwoPoints(
                    startXPercentage,
                    graphItems.get(firstInclusiveIndex - 1).getX(),
                    graphItems.get(firstInclusiveIndex - 1).getY(),
                    graphItems.get(firstInclusiveIndex).getX(),
                    graphItems.get(firstInclusiveIndex).getY()
            );


            int lastInclusiveIndex = findLastIndexBeforePercent(endXPercentage, graphItems);
            float endYPercentage = calcYAtXByTwoPoints(
                    endXPercentage,
                    graphItems.get(lastInclusiveIndex).getX(),
                    graphItems.get(lastInclusiveIndex).getY(),
                    graphItems.get(lastInclusiveIndex + 1).getX(),
                    graphItems.get(lastInclusiveIndex + 1).getY()
            );

            if (startYPercentage < yMin) {
                yMin = startYPercentage;
            } else if (startYPercentage > yMax) {
                yMax = startYPercentage;
            }

            if (endYPercentage < yMin) {
                yMin = endYPercentage;
            } else if (endYPercentage > yMax) {
                yMax = endYPercentage;
            }
            for (int i = firstInclusiveIndex; i <= lastInclusiveIndex; i++) {
                float value = graphItems.get(i).getY();
                if (value < yMin) {
                    yMin = value;
                } else if (value > yMax) {
                    yMax = value;
                }
            }
        }

        return new float[]{yMin, yMax};
    }

    private boolean isFloatEquals(float f1, float f2) {
        return Math.abs(f1 - f2) < 0.00001f;
    }

    private long[] calculateYRange(List<InputItem> data) {
        long verticalMin = data.get(0).getValue();
        long verticalMax = data.get(0).getValue();
        for (int i = 1; i < data.size(); i++) {
            long value = data.get(i).getValue();
            if (value > verticalMax) {
                verticalMax = value;
            } else if (value < verticalMin) {
                verticalMin = value;
            }
        }
        return new long[]{verticalMin, verticalMax};
    }

    private float getAlpha(String name) {
        Float animatedAlpha = alphas.get(name);
        return animatedAlpha != null ? animatedAlpha : 1f;
    }

    public interface UpdateListener {

        void onUpdate(float left, float right, float pan, List<InputGraph> graphs);

    }

}
