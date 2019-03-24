package com.kamer.chartapp.view;

import android.annotation.SuppressLint;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.kamer.chartapp.view.animator.DateAlphaAnimator;
import com.kamer.chartapp.view.animator.GraphAlphaAnimator;
import com.kamer.chartapp.view.animator.VerticalZoomAnimator;
import com.kamer.chartapp.view.data.Data;
import com.kamer.chartapp.view.data.DatePoint;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.GraphItem;
import com.kamer.chartapp.view.data.draw.DrawSelection;
import com.kamer.chartapp.view.data.draw.DrawSelectionPoint;
import com.kamer.chartapp.view.data.draw.DrawSelectionPopup;
import com.kamer.chartapp.view.surface.ChartView;

import java.util.ArrayList;
import java.util.List;

import static com.kamer.chartapp.view.utils.FloatUtils.isFloatEquals;

public class ChartManager {

    private static final float MIN_VISIBLE_PART = 0.2f;

    private ChartView chartView;
    private PreviewView previewView;
    private PreviewMaskView previewMaskView;
    private UpdateListener updateListener;

    private Data data;

    private float leftBorder = 0.7f;
    private float rightBorder = 1f;
    private float pan = 0f;

    private GraphAlphaAnimator graphAlphaAnimator;
    private DateAlphaAnimator dateAlphaAnimator;
    private VerticalZoomAnimator verticalZoomAnimator;

    @SuppressLint("ClickableViewAccessibility")
    public ChartManager(ChartView chartView, PreviewView previewView, PreviewMaskView previewMaskView, UpdateListener updateListener) {
        this.chartView = chartView;
        this.previewView = previewView;
        this.previewMaskView = previewMaskView;
        this.updateListener = updateListener;

        this.graphAlphaAnimator = new GraphAlphaAnimator(previewView, chartView);
        this.dateAlphaAnimator = new DateAlphaAnimator(chartView);
        this.verticalZoomAnimator = new VerticalZoomAnimator(chartView, previewView);

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
                leftBorder = 0.7f;
                rightBorder = 1f;
                pan = 0f;

                graphAlphaAnimator.setData(data.getGraphs());
                dateAlphaAnimator.setData(data.getDatePoints(), leftBorder, rightBorder);
                verticalZoomAnimator.setData(data, leftBorder, rightBorder);

                chartView.setData(
                        data,
                        verticalZoomAnimator.getMinY(),
                        verticalZoomAnimator.getMaxY(),
                        leftBorder,
                        rightBorder,
                        dateAlphaAnimator.getXAlphas(),
                        verticalZoomAnimator.getGuideAlphas()
                );
                previewMaskView.setBorders(leftBorder, rightBorder);
                previewView.setData(data.getGraphs(), verticalZoomAnimator.getTotalMaxY());
                syncGraphEnabledStatus();
            }
        });
    }

    public void updateGraphEnabled(String name, boolean isEnabled) {
        for (int i = 0; i < data.getGraphs().size(); i++) {
            Graph graph = data.getGraphs().get(i);
            if (graph.getName().equals(name)) {
                data.getGraphs().set(i, new Graph(graph.getName(), graph.getColor(), graph.getItems(), graph.getPath(), isEnabled));
                chartView.clearSelection();
                graphAlphaAnimator.animate();
                verticalZoomAnimator.animate(leftBorder, rightBorder);
                syncGraphEnabledStatus();
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
            chartView.clearSelection();
            onUpdateMinX();
            onUpdateZoomX();
            onUpdateZoomY();
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
            chartView.clearSelection();
            onUpdateMaxX();
            onUpdateZoomX();
            onUpdateZoomY();
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
            chartView.clearSelection();
            onUpdateMinX();
            onUpdateMaxX();
            onUpdateZoomY();
        }
    }

    private void updateSelection(float x) {
        float localPercent = x / chartView.getWidth();
        float percent = visiblePartSize() * localPercent + leftBorder;

        int selectedIndex = 0;
        int lastInclusiveIndex = findLastInclusiveIndex(rightBorder);
        for (int i = findFirstInclusiveIndex(leftBorder); i < lastInclusiveIndex; i++) {
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
            float realY = calculateYFromPercent(chartView.getHeight(), graphItem.getPercent(), verticalZoomAnimator.getMinY(), verticalZoomAnimator.getMaxY());
            points.add(new DrawSelectionPoint(realX, realY, graph.getColor()));
            texts.add(new Pair<>(graphItem.getValue() + "", graph.getColor()));
        }

        DrawSelectionPopup popup = new DrawSelectionPopup(
                border,
                isAlignedRight,
                datePoint.getTextExtended(),
                texts
        );
        chartView.setSelection(new DrawSelection(realX, points, popup));
    }

    private void onUpdateMinX() {
        chartView.setMinX(leftBorder);
        previewMaskView.setBorders(leftBorder, rightBorder);
    }

    private void onUpdateMaxX() {
        chartView.setMaxX(rightBorder);
        previewMaskView.setBorders(leftBorder, rightBorder);
    }

    private void onUpdateZoomX() {
        dateAlphaAnimator.animate(leftBorder, rightBorder);
    }

    private void onUpdateZoomY() {
        verticalZoomAnimator.animate(leftBorder, rightBorder);
    }

    private void syncGraphEnabledStatus() {
        updateListener.onUpdate(data.getGraphs());
    }

    private int calculateYFromPercent(int height, float y, float minYPercent, float maxYPercent) {
        int heightWithPadding = height - (int) Constants.PADDING_VERTICAL * 2;
        return ((int) (heightWithPadding - heightWithPadding * calcPercent(y, minYPercent, maxYPercent))) + (int) Constants.PADDING_VERTICAL;
    }

    private float visiblePartSize() {
        return rightBorder - leftBorder;
    }

    private float calcPercent(float value, float start, float end) {
        return (value - start) / (end - start);
    }

    private int findFirstInclusiveIndex(float startXPercentage) {
        for (int i = 0; i < data.getDatePoints().size(); i++) {
            float value = applyXMargin(data.getDatePoints().get(i).getPercent());
            if (value > startXPercentage || isFloatEquals(value, startXPercentage)) {
                return i;
            }
        }
        return -1;
    }

    private int findLastInclusiveIndex(float endXPercentage) {
        for (int i = data.getDatePoints().size() - 1; i >= 0; i--) {
            float value = applyXMargin(data.getDatePoints().get(i).getPercent());
            if (value < endXPercentage || isFloatEquals(value, endXPercentage)) {
                return i;
            }
        }
        return -1;
    }

    private float applyXMargin(float x) {
        float xMarginPercent = marginPercent(leftBorder, rightBorder);
        return x * (1 - xMarginPercent * 2) + xMarginPercent;
    }

    private float marginPercent(float minX, float maxX) {
        return Constants.PADDING_HORIZONTAL / (chartView.getWidth() / (maxX - minX));
    }

    public interface UpdateListener {

        void onUpdate(List<Graph> graphs);

    }

}
