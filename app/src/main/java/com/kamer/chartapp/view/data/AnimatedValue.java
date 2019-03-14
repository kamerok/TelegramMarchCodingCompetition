package com.kamer.chartapp.view.data;

public class AnimatedValue {

    private float minY;
    private float maxY;

    public AnimatedValue(float minY, float maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    @Override
    public String toString() {
        return "AnimatedValue{" +
                "minY=" + minY +
                ", maxY=" + maxY +
                '}';
    }
}
