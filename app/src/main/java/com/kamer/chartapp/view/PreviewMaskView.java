package com.kamer.chartapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kamer.chartapp.R;
import com.kamer.chartapp.view.data.PreviewMaskDrawData;


public class PreviewMaskView extends View {

    private static final int frameWidth = 15;
    private static final int frameHeight = 5;
    private static final int threshold = 40;

    private Paint overlayPaint;
    private Paint framePaint;

    private PreviewMaskDrawData drawData;

    private Listener listener;

    private Selection selection;
    private boolean isDragInProgress;
    private float startPoint;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                if (x >= drawData.getLeft() - threshold && x <= drawData.getLeft() + frameWidth + threshold) {
                    startPoint = x;
                    isDragInProgress = true;
                    selection = Selection.LEFT;
                } else if (x > drawData.getLeft() + (frameWidth + threshold) && x < drawData.getRight() - (frameWidth + threshold)) {
                    startPoint = x;
                    isDragInProgress = true;
                    selection = Selection.MIDDLE;
                } else if (x >= drawData.getRight() - (frameWidth + threshold) && x <= drawData.getRight() + threshold) {
                    startPoint = x;
                    isDragInProgress = true;
                    selection = Selection.RIGHT;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragInProgress = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDragInProgress && listener != null) {
                    float dx = (event.getX() - startPoint) / getWidth();
                    startPoint = event.getX();
                    switch (selection) {
                        case LEFT:
                            listener.onLeftBorderChanged(dx);
                            break;
                        case RIGHT:
                            listener.onRightBorderChanged(dx);
                            break;
                        case MIDDLE:
                            listener.onPanChanged(-dx);
                            break;
                    }
                }

        }
        return true;
    }

    public void setDrawData(PreviewMaskDrawData drawData) {
        this.drawData = drawData;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
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
        canvas.drawRect(0, 0, drawData.getLeft(), getHeight(), overlayPaint);
        canvas.drawRect(drawData.getRight(), 0, getWidth(), getHeight(), overlayPaint);
        canvas.drawRect(drawData.getLeft(), 0, drawData.getLeft() + frameWidth, getWidth(), framePaint);
        canvas.drawRect(drawData.getRight() - frameWidth, 0, drawData.getRight(), getWidth(), framePaint);
        canvas.drawRect(drawData.getLeft() + frameWidth, 0, drawData.getRight() - frameWidth, frameHeight, framePaint);
        canvas.drawRect(drawData.getLeft() + frameWidth, getHeight() - frameHeight, drawData.getRight() - frameWidth, getHeight(), framePaint);
    }

    public interface Listener {

        void onLeftBorderChanged(float dX);

        void onRightBorderChanged(float dX);

        void onPanChanged(float dX);

    }

    private enum Selection {
        LEFT, RIGHT, MIDDLE
    }

}
