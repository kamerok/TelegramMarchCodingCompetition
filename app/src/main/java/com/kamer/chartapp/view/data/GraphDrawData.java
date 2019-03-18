package com.kamer.chartapp.view.data;

import java.util.Arrays;
import java.util.List;

public class GraphDrawData {

    private List<DrawGraph> drawGraphs;
    private float[] yGuides;
    private List<DrawText> texts;

    public GraphDrawData(List<DrawGraph> drawGraphs, float[] yGuides, List<DrawText> texts) {
        this.drawGraphs = drawGraphs;
        this.yGuides = yGuides;
        this.texts = texts;
    }

    public List<DrawGraph> getDrawGraphs() {
        return drawGraphs;
    }

    public float[] getYGuides() {
        return yGuides;
    }

    public List<DrawText> getTexts() {
        return texts;
    }

    @Override
    public String toString() {
        return "DrawData{" +
                "drawGraphs=" + drawGraphs +
                ", yGuides=" + Arrays.toString(yGuides) +
                ", texts=" + texts +
                '}';
    }
}
