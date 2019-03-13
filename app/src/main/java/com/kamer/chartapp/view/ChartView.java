package com.kamer.chartapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kamer.chartapp.view.data.DrawItem;
import com.kamer.chartapp.view.data.InputItem;

import java.util.ArrayList;
import java.util.List;


public class ChartView extends View {

    private Paint paint;
    private List<DrawItem> drawItems;

    //for tests
    private List<InputItem> cachedInputItems;
    private boolean zoomY;
    private boolean zoomX;
    private int panOffset;

    public ChartView(Context context) {
        super(context);
        init();
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (DrawItem drawItem : drawItems) {
            canvas.drawLine(
                    drawItem.getStartX(), drawItem.getStartY(),
                    drawItem.getStopX(), drawItem.getStopY(),
                    paint
            );
        }
    }

    public void setData(List<InputItem> data) {
        cachedInputItems = data;
        calculateDrawData(data);
        invalidate();
    }

    public void switchZoomX() {
        zoomX = !zoomX;
        calculateDrawData(cachedInputItems);
        invalidate();
    }

    public void switchZoomY() {
        zoomY = !zoomY;
        calculateDrawData(cachedInputItems);
        invalidate();
    }

    public void pan() {
        switch (panOffset) {
            case 0:
                panOffset = 1;
                break;
            case 1:
                panOffset = 2;
                break;
            case 2:
                panOffset = -1;
                break;
            default:
                panOffset = 0;
        }
        calculateDrawData(cachedInputItems);
        invalidate();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
    }

    private void calculateDrawData(List<InputItem> data1) {
        List<InputItem> data = new ArrayList<>();
        if (zoomX) {
            int size = data1.size() / 2;
            int start;
            switch (panOffset) {
                case 0:
                    start = 1;
                    break;
                case 2:
                    start = size / 2;
                    break;
                case 1:
                default:
                    start = size / 4;
            }
            int end = start + size;
            for (int i = start; i < end; i++) {
                data.add(data1.get(i));
            }
        } else {
            data.addAll(data1);
        }
        List<DrawItem> drawData = new ArrayList<>();
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
        long verticalLength = Math.abs(verticalMax - verticalMin);
        if (zoomY) {
            verticalMin -= verticalLength / 2;
            verticalMax += verticalLength / 2;
            verticalLength = Math.abs(verticalMax - verticalMin);
        }
        int xInterval = getWidth() / (data.size() - 1);
        for (int i = 1; i < data.size(); i++) {
            InputItem start = data.get(i - 1);
            InputItem end = data.get(i);
            int startX = xInterval * (i - 1);
            int startY = (int) ((start.getValue() - verticalMin) / (float) verticalLength * getHeight());
            int stopX = xInterval * i;
            int stopY = (int) ((end.getValue() - verticalMin) / (float) verticalLength * getHeight());
            drawData.add(new DrawItem(startX, startY, stopX, stopY));
        }
        drawItems = drawData;
        Log.i("tag", "calculate: " + data + "\n" + drawData);
    }
}
