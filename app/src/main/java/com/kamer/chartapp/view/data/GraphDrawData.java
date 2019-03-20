package com.kamer.chartapp.view.data;

import java.util.List;

public class GraphDrawData {

    private List<DrawGraph> drawGraphs;
    private List<DrawYGuides> drawYGuides;

    public GraphDrawData(List<DrawGraph> drawGraphs, List<DrawYGuides> drawYGuides) {
        this.drawGraphs = drawGraphs;
        this.drawYGuides = drawYGuides;
    }

    public List<DrawGraph> getDrawGraphs() {
        return drawGraphs;
    }

    public List<DrawYGuides> getDrawYGuides() {
        return drawYGuides;
    }

    @Override
    public String toString() {
        return "GraphDrawData{" +
                "drawGraphs=" + drawGraphs +
                ", drawYGuides=" + drawYGuides +
                '}';
    }
}
