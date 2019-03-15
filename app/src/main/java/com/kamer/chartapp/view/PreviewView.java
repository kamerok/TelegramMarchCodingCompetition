package com.kamer.chartapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.kamer.chartapp.view.data.DrawGraph;
import com.kamer.chartapp.view.data.DrawItem;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.GraphItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PreviewView extends View implements AnimationListener {

    private Paint paint;
    private Paint linePaint;
    private List<DrawGraph> drawGraphs = new ArrayList<>();

    private float leftBorder = 0f;
    private float rightBorder = 1f;

    private Map<String, Float> alphas = new HashMap<>();
    private float minY;
    private float maxY = 1f;

    private List<Graph> graphs = new ArrayList<>();

    public PreviewView(Context context) {
        super(context);
        init();
    }

    public PreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawGraphs.isEmpty()) return;
        for (DrawGraph drawGraph : drawGraphs) {
            paint.setColor(drawGraph.getColor());
            paint.setAlpha(drawGraph.getAlpha());
            canvas.drawLines(
                    drawGraph.getPoints(),
                    paint
            );
        }
        canvas.drawLine(
                getWidth() * leftBorder, 0,
                getWidth() * leftBorder, getHeight(),
                linePaint
        );
        canvas.drawLine(
                getWidth() * rightBorder, 0,
                getWidth() * rightBorder, getHeight(),
                linePaint
        );
    }

    @Override
    public void onValuesUpdated(float totalMinY, float totalMaxY, Map<String, Float> alphas) {
        this.alphas = alphas;
        minY = totalMinY;
        maxY = totalMaxY;
        calculateDrawData(graphs);
        invalidate();
    }

    public void setData(List<Graph> data, float left, float rigth) {
        graphs = data;
        rightBorder = rigth;
        leftBorder = left;
        calculateDrawData(data);
        invalidate();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(4);
        linePaint.setAntiAlias(true);
    }

    private void calculateDrawData(List<Graph> graphs) {
        List<DrawGraph> result = new ArrayList<>();
        int width = getWidth();
        int height = getHeight();

        for (Graph graph : graphs) {
            List<GraphItem> graphItems = graph.getItems();
            List<DrawItem> drawItems = new ArrayList<>();

            for (int i = 1; i < graphItems.size(); i++) {
                GraphItem start = graphItems.get(i - 1);
                GraphItem end = graphItems.get(i);
                int startX = (int) (width * start.getX());
                int startY = (int) (height - height * calcPercent(start.getY(), minY, maxY));
                int stopX = (int) (width * end.getX());
                int stopY = (int) (height - height * calcPercent(end.getY(), minY, maxY));
                drawItems.add(new DrawItem(startX, startY, stopX, stopY));
            }

            float[] points = new float[drawItems.size() * 4];
            for (int i = 0; i < drawItems.size(); i++) {
                DrawItem drawItem = drawItems.get(i);
                points[i * 4] = drawItem.getStartX();
                points[i * 4 + 1] = drawItem.getStartY();
                points[i * 4 + 2] = drawItem.getStopX();
                points[i * 4 + 3] = drawItem.getStopY();
            }
            float alpha = getAlpha(graph.getName());

            result.add(new DrawGraph(graph.getColor(), points, ((int) (255 * alpha))));
        }

        drawGraphs = result;
    }

    public float getAlpha(String name) {
        Float animatedAlpha = alphas.get(name);
        return animatedAlpha != null ? animatedAlpha : 1f;
    }

    private float calcPercent(float value, float start, float end) {
        return (value - start) / (end - start);
    }
}
