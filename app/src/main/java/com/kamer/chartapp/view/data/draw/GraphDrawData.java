package com.kamer.chartapp.view.data.draw;

import java.util.List;

public class GraphDrawData {

    private List<DrawGraph> drawGraphs;
    private List<DrawYGuides> drawYGuides;
    private List<DrawText> xLabels;
    private DrawSelection drawSelection;

    public GraphDrawData(List<DrawGraph> drawGraphs, List<DrawYGuides> drawYGuides, List<DrawText> xLabels, DrawSelection drawSelection) {
        this.drawGraphs = drawGraphs;
        this.drawYGuides = drawYGuides;
        this.xLabels = xLabels;
        this.drawSelection = drawSelection;
    }

    public List<DrawGraph> getDrawGraphs() {
        return drawGraphs;
    }

    public List<DrawYGuides> getDrawYGuides() {
        return drawYGuides;
    }

    public List<DrawText> getxLabels() {
        return xLabels;
    }

    public DrawSelection getDrawSelection() {
        return drawSelection;
    }

    @Override
    public String toString() {
        return "GraphDrawData{" +
                "drawGraphs=" + drawGraphs +
                ", drawYGuides=" + drawYGuides +
                ", xLabels=" + xLabels +
                ", drawSelection=" + drawSelection +
                '}';
    }
}
