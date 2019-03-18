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

import com.kamer.chartapp.view.data.GraphDrawData;
import com.kamer.chartapp.view.data.DrawGraph;
import com.kamer.chartapp.view.data.DrawText;

import java.util.List;


public class ChartView extends View {

    private Paint paint;
    private Paint guideLinePaint;
    private Paint textPaint;

    private GraphDrawData drawData;

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
        if (drawData != null) {
            render(canvas, drawData);
        }
    }

    public void setDrawData(GraphDrawData drawData) {
        this.drawData = drawData;
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        guideLinePaint = new Paint();
        guideLinePaint.setColor(Color.GRAY);
        guideLinePaint.setStrokeWidth(4);
        guideLinePaint.setAntiAlias(true);
        guideLinePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(40);
    }

    private void render(Canvas canvas, GraphDrawData drawData) {
        canvas.drawLines(drawData.getYGuides(), guideLinePaint);
        List<DrawText> texts = drawData.getTexts();
        for (int i = 0; i < texts.size(); i++) {
            DrawText text = texts.get(i);
            canvas.drawText(text.getText(), text.getX(), text.getY(), textPaint);
        }
        for (int i = 0; i < drawData.getDrawGraphs().size(); i++) {
            DrawGraph graph = drawData.getDrawGraphs().get(i);
            paint.setColor(graph.getColor());
            paint.setAlpha(graph.getAlpha());
            canvas.drawPath(
                    graph.getPath(),
                    paint
            );
        }
    }
}
