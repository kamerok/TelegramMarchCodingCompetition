package com.kamer.chartapp.view.data.draw;

public class DrawSelectionPoint {

    private float x;
    private float y;
    private int color;

    public DrawSelectionPoint(float x, float y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "DrawSelectionPoint{" +
                "x=" + x +
                ", y=" + y +
                ", color=" + color +
                '}';
    }
}
