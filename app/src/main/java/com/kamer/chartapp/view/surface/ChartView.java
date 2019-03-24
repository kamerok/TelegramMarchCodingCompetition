package com.kamer.chartapp.view.surface;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kamer.chartapp.view.GraphDrawer;
import com.kamer.chartapp.view.data.draw.GraphDrawData;

public class ChartView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;
    private GraphDrawer drawer;

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

    public void setDrawData(GraphDrawData drawData) {
        if (drawThread != null) {
            drawThread.setDrawData(drawData);
        }
    }

    public void setColors(int popupColor, int popupTextColor, int shadowColor, int guideColor, int guideTextColor, int backgroundColor) {
        drawer.setColors(popupColor, popupTextColor, shadowColor, guideColor, guideTextColor, backgroundColor);
        if (drawThread != null) {
            drawThread.setDirty();
        }
    }

    private void init() {
        drawer = new GraphDrawer();
        getHolder().addCallback(this);
    }
}