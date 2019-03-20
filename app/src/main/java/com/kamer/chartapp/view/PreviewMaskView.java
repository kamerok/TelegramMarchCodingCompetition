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

import com.kamer.chartapp.R;
import com.kamer.chartapp.view.data.PreviewMaskDrawData;


public class PreviewMaskView extends View {

    private Paint overlayPaint;
    private Paint framePaint;

    private PreviewMaskDrawData drawData;

    public PreviewMaskView(Context context) {
        super(context);
        init();
    }

    public PreviewMaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PreviewMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawData != null) {
            render(canvas, drawData);
        }
    }

    public void setDrawData(PreviewMaskDrawData drawData) {
        this.drawData = drawData;
    }

    private void init() {
        overlayPaint = new Paint();
        overlayPaint.setColor(getResources().getColor(R.color.colorOverlay));
        overlayPaint.setStyle(Paint.Style.FILL);

        framePaint = new Paint();
        framePaint.setColor(getResources().getColor(R.color.colorFrame));
        framePaint.setStyle(Paint.Style.FILL);
    }

    private void render(Canvas canvas, PreviewMaskDrawData drawData) {
        int frameWidth = 15;
        int frameHeight = 5;
        canvas.drawRect(0, 0, drawData.getLeft(), getHeight(), overlayPaint);
        canvas.drawRect(drawData.getRight(), 0, getWidth(), getHeight(), overlayPaint);
        canvas.drawRect(drawData.getLeft(), 0, drawData.getLeft() + frameWidth, getWidth(), framePaint);
        canvas.drawRect(drawData.getRight() - frameWidth, 0, drawData.getRight(), getWidth(), framePaint);
        canvas.drawRect(drawData.getLeft() + frameWidth, 0, drawData.getRight() - frameWidth, frameHeight, framePaint);
        canvas.drawRect(drawData.getLeft() + frameWidth, getHeight() - frameHeight, drawData.getRight() - frameWidth, getHeight(), framePaint);
    }

}
