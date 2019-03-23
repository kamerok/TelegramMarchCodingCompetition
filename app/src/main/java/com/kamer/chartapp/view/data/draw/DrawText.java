package com.kamer.chartapp.view.data.draw;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrawText drawText = (DrawText) o;
        return Float.compare(drawText.x, x) == 0 &&
                Float.compare(drawText.y, y) == 0 &&
                alpha == drawText.alpha &&
                Objects.equals(text, drawText.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, x, y, alpha);
    }
}
