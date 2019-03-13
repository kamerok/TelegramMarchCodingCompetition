package com.kamer.chartapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.kamer.chartapp.view.data.DrawItem;
import com.kamer.chartapp.view.data.GraphItem;
import com.kamer.chartapp.view.data.InputItem;

import java.util.ArrayList;
import java.util.List;


public class ChartView extends View {

    private Paint paint;
    private List<GraphItem> graphItems;
    private List<DrawItem> drawItems;

    private float zoom = 1f;
    private float pan = 0f;

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
        //find drawing start
        //find drawing end
        //scale all values in between
        for (DrawItem drawItem : drawItems) {
            canvas.drawLine(
                    drawItem.getStartX(), drawItem.getStartY(),
                    drawItem.getStopX(), drawItem.getStopY(),
                    paint
            );
        }
    }

    public void setData(List<InputItem> data) {
        calculateGraphItems(data);
        calculateDrawData();
        invalidate();
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(@FloatRange(from = 0.1, to = 1) float zoom) {
        float newZoom = zoom;
        if (newZoom > 1) {
            newZoom = 1;
        } else if (newZoom < 0.1f) {
            newZoom = 0.1f;
        }
        this.zoom = newZoom;
        calculateDrawData();
        invalidate();
    }

    public float getPan() {
        return pan;
    }

    /**
     * @param pan Percentage offset from right border.
     */
    public void setPan(@FloatRange(from = 0, to = 1) float pan) {
        float newPan = pan;
        if (zoom + newPan > 1) {
            newPan = 1 - zoom;
        } else if (newPan < 0) {
            newPan = 0;
        }
        this.pan = newPan;
        calculateDrawData();
        invalidate();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
    }

    private void calculateGraphItems(List<InputItem> data) {
        List<GraphItem> graphData = new ArrayList<>();
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
        for (int i = 0; i < data.size(); i++) {
            float x = (float) i / (data.size() - 1);
            float y = Math.abs(verticalMin - data.get(i).getValue()) / (float) verticalLength;
            graphData.add(new GraphItem(x, y));
        }
        graphItems = graphData;
    }

    private void calculateDrawData() {
        List<DrawItem> drawData = new ArrayList<>();
        for (int i = 1; i < graphItems.size(); i++) {
            GraphItem start = graphItems.get(i - 1);
            GraphItem end = graphItems.get(i);
            int startX = (int) (getWidth() * start.getX());
            int startY = (int) (getHeight() - getHeight() * start.getY());
            int stopX = (int) (getWidth() * end.getX());
            int stopY = (int) (getHeight() - getHeight() * end.getY());
            drawData.add(new DrawItem(startX, startY, stopX, stopY));
        }
        drawItems = drawData;
    }
}
