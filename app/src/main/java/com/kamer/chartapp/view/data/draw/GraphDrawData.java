package com.kamer.chartapp.view.data.draw;

import com.kamer.chartapp.view.data.draw.DrawGraph;
import com.kamer.chartapp.view.data.draw.DrawText;
import com.kamer.chartapp.view.data.draw.DrawYGuides;

import java.util.List;

public class GraphDrawData {

    private List<DrawGraph> drawGraphs;
    private List<DrawYGuides> drawYGuides;
    private List<DrawText> xLabels;

    public GraphDrawData(List<DrawGraph> drawGraphs, List<DrawYGuides> drawYGuides, List<DrawText> xLabels) {
        this.drawGraphs = drawGraphs;
        this.drawYGuides = drawYGuides;
        this.xLabels = xLabels;
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

    @Override
    public String toString() {
        return "GraphDrawData{" +
                "drawGraphs=" + drawGraphs +
                ", drawYGuides=" + drawYGuides +
                ", xLabels=" + xLabels +
                '}';
    }
}
