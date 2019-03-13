package com.kamer.chartapp.view.data;

/**
 * Percent position relative to complete graph
 */
public class GraphItem {

    private float x;
    private float y;

    public GraphItem(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "GraphItem{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
