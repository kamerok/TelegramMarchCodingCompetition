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

import com.kamer.chartapp.view.data.DrawItem;
import com.kamer.chartapp.view.data.GraphItem;

import java.util.ArrayList;
import java.util.List;


public class PreviewView extends View {

    private Paint paint;
    private Paint linePaint;
    private List<DrawItem> drawItems;

    private float leftBorder = 0f;
    private float rightBorder = 1f;

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
        for (DrawItem drawItem : drawItems) {
            canvas.drawLine(
                    drawItem.getStartX(), drawItem.getStartY(),
                    drawItem.getStopX(), drawItem.getStopY(),
                    paint
            );
        }
    }

    public void setData(List<GraphItem> data, float left, float rigth) {
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

    private void calculateDrawData(List<GraphItem> graphItems) {
        List<DrawItem> drawData = new ArrayList<>();
        int width = getWidth();
        int height = getHeight();

        for (int i = 1; i < graphItems.size(); i++) {
            GraphItem start = graphItems.get(i - 1);
            GraphItem end = graphItems.get(i);
            int startX = (int) (width * start.getX());
            int startY = (int) (height - height * start.getY());
            int stopX = (int) (width * end.getX());
            int stopY = (int) (height - height * end.getY());
            drawData.add(new DrawItem(startX, startY, stopX, stopY));
        }

        drawItems = drawData;
    }
}
