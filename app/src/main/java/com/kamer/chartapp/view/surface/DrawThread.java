package com.kamer.chartapp.view.surface;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.kamer.chartapp.view.GraphDrawer;
import com.kamer.chartapp.view.data.draw.GraphDrawData;

public class DrawThread extends Thread{

    final private SurfaceHolder surfaceHolder;

    private boolean runFlag = false;
    private long prevTime;

    private GraphDrawData drawData;
    public GraphDrawer drawer;

    public DrawThread(GraphDrawer drawer, SurfaceHolder surfaceHolder){
        this.drawer = drawer;
        this.surfaceHolder = surfaceHolder;

        prevTime = System.currentTimeMillis();
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    public void setDrawData(GraphDrawData drawData) {
        this.drawData = drawData;
    }

    @Override
    public void run() {
        Canvas canvas;
        while (runFlag) {
            long now = System.currentTimeMillis();
            long elapsedTime = now - prevTime;
            if (elapsedTime > 16){
                prevTime = now;
            }
            canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                    if (canvas != null && drawData != null) {
                        drawer.render(canvas, drawData);
                    }
                }
            }
            finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}