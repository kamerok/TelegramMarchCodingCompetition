package com.kamer.chartapp.view.data.draw;

public class DrawText {

    private String text;
    private float x;
    private float y;
    private int alpha;

    public DrawText(String text, float x, float y, int alpha) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.alpha = alpha;
    }

    public String getText() {
        return text;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        return "DrawText{" +
                "text='" + text + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", alpha=" + alpha +
                '}';
    }
}
