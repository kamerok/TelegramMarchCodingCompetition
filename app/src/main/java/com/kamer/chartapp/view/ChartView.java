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
import android.util.Pair;
import android.view.View;

import com.kamer.chartapp.view.data.draw.DrawSelection;
import com.kamer.chartapp.view.data.draw.DrawSelectionPoint;
import com.kamer.chartapp.view.data.draw.DrawSelectionPopup;
import com.kamer.chartapp.view.data.draw.DrawYGuides;
import com.kamer.chartapp.view.data.draw.GraphDrawData;
import com.kamer.chartapp.view.data.draw.DrawGraph;
import com.kamer.chartapp.view.data.draw.DrawText;
import com.kamer.chartapp.view.utils.UnitConverter;

import java.util.List;


public class ChartView extends View {

    private Paint paint;
    private Paint guideLinePaint;
    private Paint guideTextPaint;
    private Paint circlePaint;
    private Paint erasePaint;
    private Paint selectionPopupPaint;
    private Paint selectionPopupDatePaint;
    private Paint selectionPopupValuePaint;

    private GraphDrawData drawData;

    private float viewPadding;
    private float popupWidth;
    private float popupVerticalPadding;
    private float popupHorizontalPadding;
    private float popupCornerRadius;
    private float popupDateSize;
    private float popupValueSize;
    private float popupValueMargin;
    private float popupDateMargin;

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

    public void setColors(int popupColor, int popupTextColor, int shadowColor, int guideColor, int guideTextColor) {
        selectionPopupPaint.setColor(popupColor);
        selectionPopupPaint.setShadowLayer(1, 0, 0, shadowColor);
        selectionPopupDatePaint.setColor(popupTextColor);
        guideLinePaint.setColor(guideColor);
        guideTextPaint.setColor(guideTextColor);
    }

    private void init() {
        viewPadding = UnitConverter.dpToPx(16);
        popupWidth = UnitConverter.dpToPx(120);
        popupVerticalPadding = UnitConverter.dpToPx(10);
        popupHorizontalPadding = UnitConverter.dpToPx(16);
        popupCornerRadius = UnitConverter.dpToPx(4);
        popupDateSize = UnitConverter.dpToPx(14);
        popupValueSize = UnitConverter.dpToPx(16);
        popupValueMargin = UnitConverter.dpToPx(2);
        popupDateMargin = UnitConverter.dpToPx(4);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        guideLinePaint = new Paint();
        guideLinePaint.setColor(Color.GRAY);
        guideLinePaint.setStrokeWidth(UnitConverter.dpToPx(1));
        guideLinePaint.setStyle(Paint.Style.STROKE);

        guideTextPaint = new Paint();
        guideTextPaint.setColor(Color.GRAY);
        guideTextPaint.setAntiAlias(true);
        guideTextPaint.setTextSize(UnitConverter.dpToPx(14));

        circlePaint = new Paint();
        circlePaint.setColor(Color.GRAY);

        erasePaint = new Paint();
        erasePaint.setColor(Color.GRAY);
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        selectionPopupPaint = new Paint();
        selectionPopupPaint.setColor(Color.WHITE);
        selectionPopupPaint.setShadowLayer(1, 0, 0, Color.GRAY);

        selectionPopupDatePaint = new Paint();
        selectionPopupDatePaint.setColor(Color.BLACK);
        selectionPopupDatePaint.setTextSize(popupDateSize);
        selectionPopupDatePaint.setAntiAlias(true);
        selectionPopupDatePaint.setFakeBoldText(true);

        selectionPopupValuePaint = new Paint();
        selectionPopupValuePaint.setTextSize(popupValueSize);
        selectionPopupValuePaint.setAntiAlias(true);
        selectionPopupValuePaint.setFakeBoldText(true);
    }

    private void render(Canvas canvas, GraphDrawData drawData) {
        List<DrawYGuides> drawYGuides = drawData.getDrawYGuides();
        guideTextPaint.setTextAlign(Paint.Align.LEFT);
        for (int i = 0; i < drawYGuides.size(); i++) {
            DrawYGuides drawYGuide = drawYGuides.get(i);
            guideLinePaint.setAlpha(drawYGuide.getAlpha());
            guideTextPaint.setAlpha(drawYGuide.getAlpha());
            canvas.drawLines(drawYGuide.getyGuides(), guideLinePaint);
            List<DrawText> texts = drawYGuide.getTexts();
            for (int j = 0; j < texts.size(); j++) {
                DrawText text = texts.get(j);
                canvas.drawText(text.getText(), text.getX(), text.getY(), guideTextPaint);
            }
        }
        drawSelectionLine(canvas, drawData);
        List<DrawText> xLabels = drawData.getxLabels();
        guideTextPaint.setAlpha(255);
        guideTextPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < xLabels.size(); i++) {
            DrawText text = xLabels.get(i);
            canvas.drawText(text.getText(), text.getX(), text.getY(), guideTextPaint);
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
            drawSelection(canvas, drawData.getDrawSelection());
        }
    }

    private void drawSelectionLine(Canvas canvas, GraphDrawData drawData) {
        if (drawData.getDrawSelection() != null) {
            guideLinePaint.setAlpha(255);
            canvas.drawLine(
                    drawData.getDrawSelection().getSelection(), 0,
                    //TODO: is this safe?
                    drawData.getDrawSelection().getSelection(), drawData.getDrawYGuides().get(0).getyGuides()[3],
                    guideLinePaint
            );
        }
    }

    private void drawSelection(Canvas canvas, DrawSelection drawSelection) {
        List<DrawSelectionPoint> points = drawSelection.getPoints();
        for (int i = 0; i < points.size(); i++) {
            DrawSelectionPoint point = points.get(i);
            circlePaint.setColor(point.getColor());
            canvas.drawCircle(point.getX(), point.getY(), 15, circlePaint);
            canvas.drawCircle(point.getX(), point.getY(), 10, erasePaint);
        }

        DrawSelectionPopup popup = drawSelection.getPopup();
        List<Pair<String, Integer>> values = popup.getValues();
        float left = popup.getBorder();
        float right = popup.getBorder();
        if (popup.isAlignedRight()) {
            left = left - popupWidth;
        } else {
            right = right + popupWidth;
        }
        float top = viewPadding;
        float height = popupVerticalPadding * 2 + popupDateSize + popupValueSize * values.size() + popupValueMargin * (values.size() - 1) + popupDateMargin;
        canvas.drawRoundRect(left, top, right, height + top, popupCornerRadius, popupCornerRadius, selectionPopupPaint);
        canvas.drawText(popup.getDateText(), left + popupHorizontalPadding, top + popupDateSize + popupVerticalPadding, selectionPopupDatePaint);
        for (int i = 0; i < values.size(); i++) {
            Pair<String, Integer> drawText = values.get(i);
            selectionPopupValuePaint.setColor(drawText.second);
            canvas.drawText(drawText.first, left + popupHorizontalPadding, top + popupDateSize + popupVerticalPadding + popupDateMargin + popupValueMargin * (i) + popupValueSize * (i + 1), selectionPopupValuePaint);
        }
    }
}
