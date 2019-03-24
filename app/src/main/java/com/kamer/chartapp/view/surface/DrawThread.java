package com.kamer.chartapp.view.surface;

import android.graphics.Canvas;
import android.graphics.Path;
import android.view.SurfaceHolder;

import com.kamer.chartapp.view.GraphDrawer;
import com.kamer.chartapp.view.data.Data;
import com.kamer.chartapp.view.data.DatePoint;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.YGuides;
import com.kamer.chartapp.view.data.draw.DrawGraph;
import com.kamer.chartapp.view.data.draw.DrawSelection;
import com.kamer.chartapp.view.data.draw.DrawText;
import com.kamer.chartapp.view.data.draw.DrawYGuides;
import com.kamer.chartapp.view.data.draw.GraphDrawData;
import com.kamer.chartapp.view.utils.DrawUtils;
import com.kamer.chartapp.view.utils.UnitConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawThread extends Thread {

    private static final float PADDING_VERTICAL = UnitConverter.dpToPx(32);
    private static final float PADDING_HORIZONTAL = UnitConverter.dpToPx(16);
    private static final float PADDING_TEXT_BOTTOM = UnitConverter.dpToPx(8);

    final private SurfaceHolder surfaceHolder;

    private boolean runFlag = false;
    private boolean isDirty = true;

    private GraphDrawer drawer;

    private Data data;

    private float minY;
    private float maxY;
    private float minX;
    private float maxX;
    private Map<String, Float> alphas = new HashMap<>();
    private Map<YGuides, Float> guideAlphas = new HashMap<>();
    private float[] xAlphas = new float[0];
    private float xMarginPercent;
    private DrawSelection drawSelection;

    public DrawThread(GraphDrawer drawer, SurfaceHolder surfaceHolder) {
        this.drawer = drawer;
        this.surfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    public void setDirty() {
        isDirty = true;
    }

    public void setData(Data data) {
        this.data = data;
        isDirty = true;
    }

    public void set(
            float minY,
            float maxY,
            float minX,
            float maxX,
            Map<String, Float> alphas,
            Map<YGuides, Float> guideAlphas,
            float[] xAlphas,
            float xMarginPercent,
            DrawSelection drawSelection
    ) {
        this.minY = minY;
        this.maxY = maxY;
        this.minX = minX;
        this.maxX = maxX;
        this.alphas = alphas;
        this.guideAlphas = guideAlphas;
        this.xAlphas = xAlphas;
        this.xMarginPercent = xMarginPercent;
        this.drawSelection = drawSelection;
        isDirty = true;
    }

    @Override
    public void run() {
        Canvas canvas;
        while (runFlag) {
            synchronized (surfaceHolder) {
                if (isDirty) {
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas(null);
                        if (canvas != null) {
                            GraphDrawData drawData = calculateDrawData(canvas.getWidth(), canvas.getHeight());
                            drawer.render(canvas, drawData);
                            isDirty = false;
                        }
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }
        }
    }

    private GraphDrawData calculateDrawData(int width, int height) {
        ArrayList<DrawGraph> result = new ArrayList<>();

        for (Graph graph : data.getGraphs()) {
            Path path = DrawUtils.scalePath(width, height, graph.getPath(), minY, maxY, minX, maxX, PADDING_VERTICAL, PADDING_HORIZONTAL);

            float alpha = getAlpha(graph.getName());
            result.add(new DrawGraph(graph.getColor(), path, (int) (alpha * 255)));
        }


        //TODO: move it somewhere
        List<DrawYGuides> drawYGuides = new ArrayList<>();
        for (Map.Entry<YGuides, Float> yGuidesFloatEntry : guideAlphas.entrySet()) {
            YGuides guide = yGuidesFloatEntry.getKey();
            float[] yLines = new float[guide.getPercent().length * 4];
            List<DrawText> drawTexts = new ArrayList<>();
            int alpha = (int) (yGuidesFloatEntry.getValue() * 255);
            for (int i = 0; i < guide.getPercent().length; i++) {
                float y = calculateYFromPercent(height, guide.getPercent()[i], minY, maxY, PADDING_VERTICAL);
                yLines[i * 4] = UnitConverter.dpToPx(16);
                yLines[i * 4 + 1] = y;
                yLines[i * 4 + 2] = width - UnitConverter.dpToPx(16);
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
                int x = (int) (width * calcPercent(applyXMargin(xPercent), minX, maxX));
                xLabels.add(new DrawText(datePoint.getText(), x, height - PADDING_TEXT_BOTTOM, (int) (xAlphas[i] * 255)));
            }
        }

        return new GraphDrawData(result, drawYGuides, xLabels, drawSelection);
    }

    private float getAlpha(String name) {
        Float animatedAlpha = alphas.get(name);
        return animatedAlpha != null ? animatedAlpha : 1f;
    }

    private int calculateYFromPercent(int height, float y, float minYPercent, float maxYPercent, float padding) {
        int heightWithPadding = height - (int) padding * 2;
        return ((int) (heightWithPadding - heightWithPadding * calcPercent(y, minYPercent, maxYPercent))) + (int) padding;
    }

    private float calcPercent(float value, float start, float end) {
        return (value - start) / (end - start);
    }

    private float applyXMargin(float x) {
        return x * (1 - xMarginPercent * 2) + xMarginPercent;
    }
}