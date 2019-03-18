package com.kamer.chartapp.view.data;

import java.util.List;

public class PreviewDrawData {

    private List<DrawGraph> drawGraphs;
    private float leftBorder;
    private float rightBorder;

    public PreviewDrawData(List<DrawGraph> drawGraphs, float leftBorder, float rightBorder) {
        this.drawGraphs = drawGraphs;
        this.leftBorder = leftBorder;
        this.rightBorder = rightBorder;
    }

    public List<DrawGraph> getDrawGraphs() {
        return drawGraphs;
    }

    public float getLeftBorder() {
        return leftBorder;
    }

    public float getRightBorder() {
        return rightBorder;
    }

    @Override
    public String toString() {
        return "PreviewDrawData{" +
                "drawGraphs=" + drawGraphs +
                ", leftBorder=" + leftBorder +
                ", rightBorder=" + rightBorder +
                '}';
    }
}
