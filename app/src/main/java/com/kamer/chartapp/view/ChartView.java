package com.kamer.chartapp.view;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
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

import com.kamer.chartapp.view.data.AnimatedValue;
import com.kamer.chartapp.view.data.DrawItem;
import com.kamer.chartapp.view.data.GraphItem;
import com.kamer.chartapp.view.data.InputItem;

import java.util.ArrayList;
import java.util.List;


public class ChartView extends View {

    private static final float MIN_VISIBLE_PART = 0.1f;

    private Paint paint;
    public List<GraphItem> graphItems;
    private float[] drawItems;

    private float leftBorder = 0f;
    private float rightBorder = 1f;
    private float pan = 0f;

    private AnimatedValue animatedValue = new AnimatedValue(0, 1);

    private ValueAnimator currentAnimation;

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
        if (drawItems != null) {
            canvas.drawLines(
                    drawItems,
                    paint
            );
        }
    }

    public void setData(List<InputItem> data) {
        graphItems = calculateGraphItems(data);
        calculateDrawData();
        animateZoom();
    }

    public float getLeftBorder() {
        return leftBorder;
    }

    public void setLeftBorder(@FloatRange(from = 0, to = 1) float leftBorder) {
        float newLeft = leftBorder;
        float newVisiblePart = rightBorder - leftBorder;
        if (newVisiblePart + pan > 1) {
            newLeft = 0;
        } else if (newVisiblePart < MIN_VISIBLE_PART) {
            newLeft = rightBorder - MIN_VISIBLE_PART;
        }
        if (this.leftBorder != newLeft) {
            this.leftBorder = newLeft;
            calculateDrawData();
            animateZoom();
        }
    }

    public float getRightBorder() {
        return rightBorder;
    }

    public void setRightBorder(@FloatRange(from = 0, to = 1) float rightBorder) {
        float newRight;
        float newPan = pan;
        if (rightBorder > 1) {
            newRight = 1;
            pan = 0;
        } else if (rightBorder - leftBorder < MIN_VISIBLE_PART) {
            newRight = leftBorder + MIN_VISIBLE_PART;
            newPan = 1 - newRight;
        } else {
            newRight = rightBorder;
            newPan = 1 - rightBorder;
        }
        if (this.rightBorder != newRight || pan != newPan) {
            this.rightBorder = newRight;
            this.pan = newPan;
            calculateDrawData();
            animateZoom();
        }
    }

    public float getPan() {
        return pan;
    }

    /**
     * @param pan Percentage offset from right border.
     */
    public void setPan(@FloatRange(from = 0, to = 1) float pan) {
        float newPan = pan;
        if (visiblePartSize() + newPan > 1) {
            newPan = 1 - visiblePartSize();
        } else if (newPan < 0) {
            newPan = 0;
        }
        if (this.pan != newPan) {
            float diff = this.pan - newPan;
            this.pan = newPan;
            this.leftBorder += diff;
            this.rightBorder += diff;
            calculateDrawData();
            animateZoom();
        }
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
    }

    private List<GraphItem> calculateGraphItems(List<InputItem> data) {
        List<GraphItem> result = new ArrayList<>();
        long[] range = calculateYRange(data);

        long verticalLength = Math.abs(range[0] - range[1]);
        for (int i = 0; i < data.size(); i++) {
            float x = (float) i / (data.size() - 1);
            float y = Math.abs(range[0] - data.get(i).getValue()) / (float) verticalLength;
            result.add(new GraphItem(x, y));
        }
        return result;
    }

    private int findFirstIndexAfterPercent(float percent, List<GraphItem> items) {
        for (int i = 0; i < items.size() - 2; i++) {
            GraphItem current = items.get(i);
            GraphItem next = items.get(i + 1);
            if ((isFloatEquals(current.getX(), percent) || current.getX() < percent) && next.getX() > percent) {
                return i + 1;
            }
        }
        return -1;
    }

    private int findLastIndexBeforePercent(float percent, List<GraphItem> items) {
        for (int i = items.size() - 1; i >= 1; i--) {
            GraphItem current = items.get(i);
            GraphItem previous = items.get(i - 1);
            if ((current.getX() > percent || isFloatEquals(current.getX(), percent)) && previous.getX() < percent) {
                return i - 1;
            }
        }
        return -1;
    }

    private float calcYAtXByTwoPoints(float x, float x1, float y1, float x2, float y2) {
        return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
    }

    private void calculateDrawData() {
        float startXPercentage = 1 - (visiblePartSize() + pan);
        int firstInclusiveIndex = findFirstIndexAfterPercent(startXPercentage, graphItems);
        float startYPercentage = calcYAtXByTwoPoints(
                startXPercentage,
                graphItems.get(firstInclusiveIndex - 1).getX(),
                graphItems.get(firstInclusiveIndex - 1).getY(),
                graphItems.get(firstInclusiveIndex).getX(),
                graphItems.get(firstInclusiveIndex).getY()
        );

        float endXPercentage = startXPercentage + visiblePartSize();
        int lastInclusiveIndex = findLastIndexBeforePercent(endXPercentage, graphItems);
        float endYPercentage = calcYAtXByTwoPoints(
                endXPercentage,
                graphItems.get(lastInclusiveIndex).getX(),
                graphItems.get(lastInclusiveIndex).getY(),
                graphItems.get(lastInclusiveIndex + 1).getX(),
                graphItems.get(lastInclusiveIndex + 1).getY()
        );

        List<DrawItem> drawData = new ArrayList<>();
        int width = getWidth();
        int height = getHeight();

        float yMin = animatedValue.getMinY();
        float yMax = animatedValue.getMaxY();

        GraphItem first = graphItems.get(firstInclusiveIndex);
        float newXPercent = calcPercent(first.getX(), startXPercentage, endXPercentage);
        drawData.add(new DrawItem(
                0, (int) (height - calcPercent(startYPercentage, yMin, yMax) * height),
                (int) (newXPercent * width), (int) (height - calcPercent(first.getY(), yMin, yMax) * height)
        ));

        for (int i = firstInclusiveIndex + 1; i <= lastInclusiveIndex; i++) {
            GraphItem start = graphItems.get(i - 1);
            GraphItem end = graphItems.get(i);
            int startX = (int) (width * calcPercent(start.getX(), startXPercentage, endXPercentage));
            int startY = (int) (height - height * calcPercent(start.getY(), yMin, yMax));
            int stopX = (int) (width * calcPercent(end.getX(), startXPercentage, endXPercentage));
            int stopY = (int) (height - height * calcPercent(end.getY(), yMin, yMax));
            drawData.add(new DrawItem(startX, startY, stopX, stopY));
        }

        GraphItem last = graphItems.get(lastInclusiveIndex);
        newXPercent = calcPercent(last.getX(), startXPercentage, endXPercentage);
        drawData.add(new DrawItem(
                (int) (newXPercent * width), (int) (height - calcPercent(last.getY(), yMin, yMax) * height),
                width, (int) (height - calcPercent(endYPercentage, yMin, yMax) * height)
        ));

        float[] points = new float[drawData.size() * 4];
        for (int i = 0; i < drawData.size(); i++) {
            DrawItem drawItem = drawData.get(i);
            points[i * 4] = drawItem.getStartX();
            points[i * 4 + 1] = drawItem.getStartY();
            points[i * 4 + 2] = drawItem.getStopX();
            points[i * 4 + 3] = drawItem.getStopY();
        }
        drawItems = points;
    }

    private float visiblePartSize() {
        return rightBorder - leftBorder;
    }

    private float calcPercent(float value, float start, float end) {
        return (value - start) / (end - start);
    }

    private void animateZoom() {
        AnimatedValue targetValue = calculateTargetValue();
        PropertyValuesHolder propertyMin = PropertyValuesHolder.ofFloat("minY", animatedValue.getMinY(), targetValue.getMinY());
        PropertyValuesHolder propertyMax = PropertyValuesHolder.ofFloat("maxY", animatedValue.getMaxY(), targetValue.getMaxY());
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        ValueAnimator animator = new ValueAnimator();
        animator.setValues(propertyMin, propertyMax);
        animator.setDuration(100);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float newMin = (float) valueAnimator.getAnimatedValue("minY");
                float newMax = (float) valueAnimator.getAnimatedValue("maxY");
                if (newMin != animatedValue.getMinY() || newMax != animatedValue.getMaxY()) {
                    animatedValue = new AnimatedValue(
                            newMin,
                            newMax
                    );
                    calculateDrawData();
                }
                invalidate();
            }
        });
        currentAnimation = animator;
        animator.start();
    }

    private AnimatedValue calculateTargetValue() {
        float startXPercentage = 1 - (visiblePartSize() + pan);
        int firstInclusiveIndex = findFirstIndexAfterPercent(startXPercentage, graphItems);
        float startYPercentage = calcYAtXByTwoPoints(
                startXPercentage,
                graphItems.get(firstInclusiveIndex - 1).getX(),
                graphItems.get(firstInclusiveIndex - 1).getY(),
                graphItems.get(firstInclusiveIndex).getX(),
                graphItems.get(firstInclusiveIndex).getY()
        );


        float endXPercentage = startXPercentage + visiblePartSize();
        int lastInclusiveIndex = findLastIndexBeforePercent(endXPercentage, graphItems);
        float endYPercentage = calcYAtXByTwoPoints(
                endXPercentage,
                graphItems.get(lastInclusiveIndex).getX(),
                graphItems.get(lastInclusiveIndex).getY(),
                graphItems.get(lastInclusiveIndex + 1).getX(),
                graphItems.get(lastInclusiveIndex + 1).getY()
        );

        float yMin = startYPercentage;
        float yMax = startYPercentage;
        if (endYPercentage < yMin) {
            yMin = endYPercentage;
        } else if (endYPercentage > yMax) {
            yMax = endYPercentage;
        }
        for (int i = firstInclusiveIndex; i <= lastInclusiveIndex; i++) {
            float value = graphItems.get(i).getY();
            if (value < yMin) {
                yMin = value;
            } else if (value > yMax) {
                yMax = value;
            }
        }

        return new AnimatedValue(yMin, yMax);
    }

    private boolean isFloatEquals(float f1, float f2) {
        return Math.abs(f1 - f2) < 0.00001f;
    }

    private long[] calculateYRange(List<InputItem> data) {
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
        return new long[]{verticalMin, verticalMax};
    }
}
