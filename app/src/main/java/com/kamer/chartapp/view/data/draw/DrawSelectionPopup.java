package com.kamer.chartapp.view.data.draw;

import java.util.List;

public class DrawSelectionPopup {

    private float left;
    private float right;
    private float top;
    private float bottom;
    private DrawText date;
    private List<DrawText> value;

    public DrawSelectionPopup(float left, float right, float top, float bottom, DrawText date, List<DrawText> value) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.date = date;
        this.value = value;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getTop() {
        return top;
    }

    public float getBottom() {
        return bottom;
    }

    public DrawText getDate() {
        return date;
    }

    public List<DrawText> getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DrawSelectionPopup{" +
                "left=" + left +
                ", right=" + right +
                ", top=" + top +
                ", bottom=" + bottom +
                ", date=" + date +
                ", value=" + value +
                '}';
    }
}
