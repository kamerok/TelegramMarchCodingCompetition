package com.kamer.chartapp.view.surface;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import com.kamer.chartapp.view.GraphDrawer;
import com.kamer.chartapp.view.data.draw.GraphDrawData;

public class DrawThread extends Thread {

    final private SurfaceHolder surfaceHolder;

    private boolean runFlag = false;
    private boolean isDirty = true;

    private GraphDrawData drawData;
    private GraphDrawer drawer;

    public DrawThread(GraphDrawer drawer, SurfaceHolder surfaceHolder) {
        this.drawer = drawer;
        this.surfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    public void setDrawData(GraphDrawData drawData) {
        if (!drawData.equals(this.drawData)) {
            this.drawData = drawData;
            isDirty = true;
        }
    }

    public void setDirty() {
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
                        if (canvas != null && drawData != null) {
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
}