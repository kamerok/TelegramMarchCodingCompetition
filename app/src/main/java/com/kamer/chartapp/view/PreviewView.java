package com.kamer.chartapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.kamer.chartapp.view.data.Data;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.draw.DrawGraph;
import com.kamer.chartapp.view.data.draw.PreviewDrawData;
import com.kamer.chartapp.view.utils.DrawUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PreviewView extends View {

    private static final int PADDING_VERTICAL = 15;

    private Paint paint;

    private PreviewDrawData drawData;

    private float max;
    private Map<String, Float> alphas = new HashMap<>();
    private List<Graph> graphs = new ArrayList<>();
    private float percentDiff;

    public PreviewView(Context context) {
        super(context);
        init();
    }

    public PreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawData != null) {
            render(canvas, drawData);
        }
    }

    public void setData(Data data, float max) {
        this.graphs = data.getGraphs();
        this.max = max;
        this.alphas = new HashMap<>();
        this.percentDiff = (data.getMaxValue() - (data.getMaxValue() - data.getMinValue())) / (float) data.getMaxValue();
        drawData = calculatePreviewDrawData();
        invalidate();
    }

    public void setMax(float max) {
        this.max = max;
        drawData = calculatePreviewDrawData();
        invalidate();
    }

    public void setAlphas(Map<String, Float> alphas) {
        this.alphas = alphas;
        drawData = calculatePreviewDrawData();
        invalidate();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    private PreviewDrawData calculatePreviewDrawData() {
        List<DrawGraph> result = new ArrayList<>();
        int width = getWidth();
        int height = getHeight();

        for (Graph graph : graphs) {
            Path path = DrawUtils.scalePath(width, height, graph.getPath(), -percentDiff, max, 0, 1, PADDING_VERTICAL, 0);

            float alpha = getAlpha(graph.getName());
            result.add(new DrawGraph(graph.getColor(), path, ((int) (255 * alpha))));
        }

        return new PreviewDrawData(result);
    }

    private float getAlpha(String name) {
        Float animatedAlpha = alphas.get(name);
        return animatedAlpha != null ? animatedAlpha : 1f;
    }

    private void render(Canvas canvas, PreviewDrawData drawData) {
        for (DrawGraph drawGraph : drawData.getDrawGraphs()) {
            paint.setColor(drawGraph.getColor());
            paint.setAlpha(drawGraph.getAlpha());
            canvas.drawPath(
                    drawGraph.getPath(),
                    paint
            );
        }
    }

}
