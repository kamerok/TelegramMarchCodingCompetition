package com.kamer.chartapp.view.data;

import java.util.Arrays;

public class DrawGraph {

    private int color;
    private float[] points;

    public DrawGraph(int color, float[] points) {
        this.color = color;
        this.points = points;
    }

    public int getColor() {
        return color;
    }

    public float[] getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "DrawGraph{" +
                "color=" + color +
                ", points=" + Arrays.toString(points) +
                '}';
    }
}
