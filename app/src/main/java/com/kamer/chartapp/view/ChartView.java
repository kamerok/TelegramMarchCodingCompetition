package com.kamer.chartapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.kamer.chartapp.view.data.draw.DrawSelectionPoint;
import com.kamer.chartapp.view.data.draw.DrawYGuides;
import com.kamer.chartapp.view.data.draw.GraphDrawData;
import com.kamer.chartapp.view.data.draw.DrawGraph;
import com.kamer.chartapp.view.data.draw.DrawText;

import java.util.List;


public class ChartView extends View {

    private Paint paint;
    private Paint guideLinePaint;
    private Paint textPaint;
    private Paint xTextPaint;
    private Paint circlePaint;
    private Paint erasePaint;

    private GraphDrawData drawData;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

        xTextPaint = new Paint();
        xTextPaint.setColor(Color.GRAY);
        xTextPaint.setTextSize(40);
        xTextPaint.setTextAlign(Paint.Align.CENTER);

        circlePaint = new Paint();
        circlePaint.setColor(Color.GRAY);

        erasePaint = new Paint();
        erasePaint.setColor(Color.GRAY);
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

    }

    private void render(Canvas canvas, GraphDrawData drawData) {
        List<DrawYGuides> drawYGuides = drawData.getDrawYGuides();
        for (int i = 0; i < drawYGuides.size(); i++) {
            DrawYGuides drawYGuide = drawYGuides.get(i);
            guideLinePaint.setAlpha(drawYGuide.getAlpha());
            textPaint.setAlpha(drawYGuide.getAlpha());
            canvas.drawLines(drawYGuide.getyGuides(), guideLinePaint);
            List<DrawText> texts = drawYGuide.getTexts();
            for (int j = 0; j < texts.size(); j++) {
                DrawText text = texts.get(j);
                canvas.drawText(text.getText(), text.getX(), text.getY(), textPaint);
            }
        }
        if (drawData.getDrawSelection() != null) {
            guideLinePaint.setAlpha(255);
            canvas.drawLine(
                    drawData.getDrawSelection().getSelection(), 0,
                    drawData.getDrawSelection().getSelection(), getHeight(),
                    guideLinePaint
            );
        }
        List<DrawText> xLabels = drawData.getxLabels();
        for (int i = 0; i < xLabels.size(); i++) {
            DrawText text = xLabels.get(i);
            canvas.drawText(text.getText(), text.getX(), text.getY(), xTextPaint);
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
        if (drawData.getDrawSelection() != null) {
            List<DrawSelectionPoint> points = drawData.getDrawSelection().getPoints();
            for (int i = 0; i < points.size(); i++) {
                DrawSelectionPoint point = points.get(i);
                circlePaint.setColor(point.getColor());
                canvas.drawCircle(point.getX(), point.getY(), 15, circlePaint);
                canvas.drawCircle(point.getX(), point.getY(), 10, erasePaint);
            }
        }
    }
}
