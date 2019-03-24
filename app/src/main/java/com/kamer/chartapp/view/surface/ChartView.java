package com.kamer.chartapp.view.surface;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kamer.chartapp.view.GraphDrawer;
import com.kamer.chartapp.view.data.Data;
import com.kamer.chartapp.view.data.YGuides;
import com.kamer.chartapp.view.data.draw.DrawSelection;

import java.util.HashMap;
import java.util.Map;

public class ChartView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;
    private GraphDrawer drawer;

    private Data data;
    private float minY;
    private float maxY;
    private float minX;
    private float maxX;
    private float[] xAlphas;
    private Map<YGuides, Float> guideAlphas;

    public ChartView(Context context) {
        super(context);
        init();
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(drawer, getHolder());
        drawThread.setData(data, minY, maxY, minX, maxX, xAlphas, guideAlphas);
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void setColors(int popupColor, int popupTextColor, int shadowColor, int guideColor, int guideTextColor, int backgroundColor) {
        drawer.setColors(popupColor, popupTextColor, shadowColor, guideColor, guideTextColor, backgroundColor);
        if (drawThread != null) {
            drawThread.setDirty();
        }
    }

    public void setData(Data data, float minY, float maxY, float minX, float maxX, float[] xAlphas, Map<YGuides, Float> guideAlphas) {
        this.data = data;
        this.minY = minY;
        this.maxY = maxY;
        this.minX = minX;
        this.maxX = maxX;
        this.xAlphas = xAlphas;
        this.guideAlphas = guideAlphas;
        if (drawThread != null) {
            drawThread.setData(data, minY, maxY, minX, maxX, xAlphas, guideAlphas);
        }
    }

    public void setGraphAlphas(HashMap<String, Float> graphAlphas) {
        if (drawThread != null) {
            drawThread.setGraphAlphas(graphAlphas);
        }
    }

    public void setMinX(float minX) {
        this.minX = minX;
        if (drawThread != null) {
            drawThread.setMinX(minX);
        }
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
        if (drawThread != null) {
            drawThread.setMaxX(maxX);
        }
    }

    public void setXAlphas(float[] xAlphas) {
        this.xAlphas = xAlphas;
        if (drawThread != null) {
            drawThread.setXAlphas(xAlphas);
        }
    }

    public void setSelection(DrawSelection drawSelection) {
        if (drawThread != null) {
            drawThread.setSelection(drawSelection);
        }
    }

    public void clearSelection() {
        if (drawThread != null) {
            drawThread.setSelection(null);
        }
    }

    public void set(
            float minY,
            float maxY,
            Map<YGuides, Float> guideAlphas
    ) {
        if (drawThread != null) {
            drawThread.set(minY, maxY, guideAlphas);
        }
    }

    private void init() {
        drawer = new GraphDrawer();
        getHolder().addCallback(this);
    }
}