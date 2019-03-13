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
        long verticalLength = verticalMax - verticalMin;
        int xInterval = getWidth() / (data.size() - 1);
        for (int i = 1; i < data.size(); i++) {
            InputItem start = data.get(i - 1);
            InputItem end = data.get(i);
            int startX = xInterval * (i - 1);
            int startY = (int) ((start.getValue() - verticalMin) / (float) verticalLength * getHeight());
            int stopX = xInterval * i;
            int stopY =  (int) ((end.getValue() - verticalMin) / (float) verticalLength * getHeight());
            drawData.add(new DrawItem(startX, startY, stopX, stopY));
        }
        drawItems = drawData;
        Log.i("tag", "convert: " + data + "\n" + drawData);
        invalidate();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
    }
}
