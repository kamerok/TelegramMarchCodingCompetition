package com.kamer.chartapp.view.data;

public class DrawItem {

    private int startX;
    private int startY;

    private int stopX;
    private int stopY;

    public DrawItem(int startX, int startY, int stopX, int stopY) {
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getStopX() {
        return stopX;
    }

    public int getStopY() {
        return stopY;
    }

    @Override
    public String toString() {
        return "DrawItem{" +
                "startX=" + startX +
                ", startY=" + startY +
                ", stopX=" + stopX +
                ", stopY=" + stopY +
                '}';
    }
}
