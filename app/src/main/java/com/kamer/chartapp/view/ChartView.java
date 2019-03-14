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
        calculateGraphItems(data);
        calculateDrawData();
        animateZoom();
        //invalidate();
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
        float startXPercentage = 1 - (visiblePartSize() + pan);
        int firstInclusiveIndex = 0;
        float startYPercentBeforeIndex = 0;
        float startXPercentBeforeIndex = 0;
        float startYPercentAfterIndex = 0;
        float startXPercentAfterIndex = 0;
        for (int i = 0; i < graphItems.size() - 1; i++) {
            GraphItem current = graphItems.get(i);
            GraphItem next = graphItems.get(i + 1);
            if (current.getX() == startXPercentage) {
                firstInclusiveIndex = i + 1;
                startYPercentBeforeIndex = current.getY();
                startXPercentBeforeIndex = current.getX();
                startYPercentAfterIndex = current.getY();
                startXPercentAfterIndex = current.getX();
                break;
            }
            if (next.getX() == startXPercentage) {
                firstInclusiveIndex = i + 2;
                startYPercentBeforeIndex = next.getY();
                startXPercentBeforeIndex = next.getX();
                startYPercentAfterIndex = next.getY();
                startXPercentAfterIndex = next.getX();
                break;
            }
            if (current.getX() < startXPercentage && next.getX() > startXPercentage) {
                firstInclusiveIndex = i + 1;
                startYPercentBeforeIndex = current.getY();
                startXPercentBeforeIndex = current.getX();
                startYPercentAfterIndex = next.getY();
                startXPercentAfterIndex = next.getX();
                break;
            }
        }
        float startYPercentage;
        if (startYPercentBeforeIndex == startYPercentAfterIndex) {
            startYPercentage = startYPercentAfterIndex;
        } else {
            startYPercentage = startYPercentBeforeIndex
                    + (startXPercentage - startXPercentBeforeIndex)
                    * (startYPercentAfterIndex - startYPercentBeforeIndex)
                    / (startXPercentAfterIndex - startXPercentBeforeIndex);
        }


        float endXPercentage = startXPercentage + visiblePartSize();
        int lastInclusiveIndex = 0;
        float lastYPercentBeforeIndex = 0;
        float lastXPercentBeforeIndex = 0;
        float lastYPercentAfterIndex = 0;
        float lastXPercentAfterIndex = 0;
        for (int i = graphItems.size() - 1; i >= 1; i--) {
            GraphItem current = graphItems.get(i);
            GraphItem previous = graphItems.get(i - 1);
            if (current.getX() == endXPercentage) {
                lastInclusiveIndex = i - 1;
                lastYPercentBeforeIndex = current.getY();
                lastXPercentBeforeIndex = current.getX();
                lastYPercentAfterIndex = current.getY();
                lastXPercentAfterIndex = current.getX();
                break;
            }
            if (previous.getX() == endXPercentage) {
                lastInclusiveIndex = i - 2;
                lastYPercentBeforeIndex = previous.getY();
                lastXPercentBeforeIndex = previous.getX();
                lastYPercentAfterIndex = previous.getY();
                lastXPercentAfterIndex = previous.getX();
                break;
            }
            if (current.getX() > endXPercentage && previous.getX() < endXPercentage) {
                lastInclusiveIndex = i - 1;
                lastYPercentBeforeIndex = previous.getY();
                lastXPercentBeforeIndex = previous.getX();
                lastYPercentAfterIndex = current.getY();
                lastXPercentAfterIndex = current.getX();
                break;
            }
        }
        float endYPercentage;
        if (lastYPercentBeforeIndex == lastYPercentAfterIndex) {
            endYPercentage = lastYPercentAfterIndex;
        } else {
            endYPercentage = lastYPercentBeforeIndex
                    + (endXPercentage - lastXPercentBeforeIndex)
                    * (lastYPercentAfterIndex - lastYPercentBeforeIndex)
                    / (lastXPercentAfterIndex - lastXPercentBeforeIndex);
        }

        List<DrawItem> drawData = new ArrayList<>();
        int width = getWidth();
        int height = getHeight();

        float yMin = animatedValue.getMinY();
        float yMax = animatedValue.getMaxY();

        GraphItem first = graphItems.get(firstInclusiveIndex);
        float newXPercent = calcPercent(first.getX(), startXPercentage, endXPercentage);
        drawData.add(new DrawItem(0, (int) (height - calcPercent(startYPercentage, yMin, yMax) * height), (int) (newXPercent * width), (int) (height - calcPercent(first.getY(), yMin, yMax) * height)));

        for (int i = firstInclusiveIndex + 1; i <= lastInclusiveIndex; i++) {
            GraphItem start = graphItems.get(i - 1);
            GraphItem end = graphItems.get(i);
            int startX = (int) (width * calcPercent(start.getX(), startXPercentage, endXPercentage));
            //int startY = (int) (height - height * start.getY());
            int startY = (int) (height - height * calcPercent(start.getY(), yMin, yMax));
            int stopX = (int) (width * calcPercent(end.getX(), startXPercentage, endXPercentage));
            //int stopY = (int) (height - height * end.getY());
            int stopY = (int) (height - height * calcPercent(end.getY(), yMin, yMax));
            drawData.add(new DrawItem(startX, startY, stopX, stopY));
        }

        GraphItem last = graphItems.get(lastInclusiveIndex);
        newXPercent = calcPercent(last.getX(), startXPercentage, endXPercentage);
        drawData.add(new DrawItem((int) (newXPercent * width), (int) (height - calcPercent(last.getY(), yMin, yMax) * height), width, (int) (height - calcPercent(endYPercentage, yMin, yMax) * height)));

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
        int firstInclusiveIndex = 0;
        float startYPercentBeforeIndex = 0;
        float startXPercentBeforeIndex = 0;
        float startYPercentAfterIndex = 0;
        float startXPercentAfterIndex = 0;
        for (int i = 0; i < graphItems.size() - 1; i++) {
            GraphItem current = graphItems.get(i);
            GraphItem next = graphItems.get(i + 1);
            if (current.getX() == startXPercentage) {
                firstInclusiveIndex = i + 1;
                startYPercentBeforeIndex = current.getY();
                startXPercentBeforeIndex = current.getX();
                startYPercentAfterIndex = current.getY();
                startXPercentAfterIndex = current.getX();
                break;
            }
            if (next.getX() == startXPercentage) {
                firstInclusiveIndex = i + 2;
                startYPercentBeforeIndex = next.getY();
                startXPercentBeforeIndex = next.getX();
                startYPercentAfterIndex = next.getY();
                startXPercentAfterIndex = next.getX();
                break;
            }
            if (current.getX() < startXPercentage && next.getX() > startXPercentage) {
                firstInclusiveIndex = i + 1;
                startYPercentBeforeIndex = current.getY();
                startXPercentBeforeIndex = current.getX();
                startYPercentAfterIndex = next.getY();
                startXPercentAfterIndex = next.getX();
                break;
            }
        }
        float startYPercentage;
        if (startYPercentBeforeIndex == startYPercentAfterIndex) {
            startYPercentage = startYPercentAfterIndex;
        } else {
            startYPercentage = startYPercentBeforeIndex
                    + (startXPercentage - startXPercentBeforeIndex)
                    * (startYPercentAfterIndex - startYPercentBeforeIndex)
                    / (startXPercentAfterIndex - startXPercentBeforeIndex);
        }


        float endXPercentage = startXPercentage + visiblePartSize();
        int lastInclusiveIndex = 0;
        float lastYPercentBeforeIndex = 0;
        float lastXPercentBeforeIndex = 0;
        float lastYPercentAfterIndex = 0;
        float lastXPercentAfterIndex = 0;
        for (int i = graphItems.size() - 1; i >= 1; i--) {
            GraphItem current = graphItems.get(i);
            GraphItem previous = graphItems.get(i - 1);
            if (current.getX() == endXPercentage) {
                lastInclusiveIndex = i - 1;
                lastYPercentBeforeIndex = current.getY();
                lastXPercentBeforeIndex = current.getX();
                lastYPercentAfterIndex = current.getY();
                lastXPercentAfterIndex = current.getX();
                break;
            }
            if (previous.getX() == endXPercentage) {
                lastInclusiveIndex = i - 2;
                lastYPercentBeforeIndex = previous.getY();
                lastXPercentBeforeIndex = previous.getX();
                lastYPercentAfterIndex = previous.getY();
                lastXPercentAfterIndex = previous.getX();
                break;
            }
            if (current.getX() > endXPercentage && previous.getX() < endXPercentage) {
                lastInclusiveIndex = i - 1;
                lastYPercentBeforeIndex = previous.getY();
                lastXPercentBeforeIndex = previous.getX();
                lastYPercentAfterIndex = current.getY();
                lastXPercentAfterIndex = current.getX();
                break;
            }
        }
        float endYPercentage;
        if (lastYPercentBeforeIndex == lastYPercentAfterIndex) {
            endYPercentage = lastYPercentAfterIndex;
        } else {
            endYPercentage = lastYPercentBeforeIndex
                    + (endXPercentage - lastXPercentBeforeIndex)
                    * (lastYPercentAfterIndex - lastYPercentBeforeIndex)
                    / (lastXPercentAfterIndex - lastXPercentBeforeIndex);
        }

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
}
