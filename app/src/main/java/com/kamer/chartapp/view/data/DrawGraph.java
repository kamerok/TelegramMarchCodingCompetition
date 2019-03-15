package com.kamer.chartapp.view.data;

import java.util.Arrays;

public class DrawGraph {

    private int color;
    private float[] points;
    private int alpha;

    public DrawGraph(int color, float[] points, int alpha) {
        this.color = color;
        this.points = points;
        this.alpha = alpha;
    }

    public int getColor() {
        return color;
    }

    public float[] getPoints() {
        return points;
    }

    public int getAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        return "DrawGraph{" +
                "color=" + color +
                ", points=" + Arrays.toString(points) +
                ", alpha=" + alpha +
                '}';
    }
}
