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

import com.kamer.chartapp.view.data.DrawGraph;
import com.kamer.chartapp.view.data.PreviewDrawData;


public class PreviewView extends View {

    private Paint paint;

    private PreviewDrawData drawData;

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
        if (drawData != null) {
            render(canvas, drawData);
        }
    }

    public void setDrawData(PreviewDrawData drawData) {
        this.drawData = drawData;
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
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
