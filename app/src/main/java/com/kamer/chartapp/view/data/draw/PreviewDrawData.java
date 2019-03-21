package com.kamer.chartapp.view.data.draw;

import com.kamer.chartapp.view.data.draw.DrawGraph;

import java.util.List;

public class PreviewDrawData {

    private List<DrawGraph> drawGraphs;

    public PreviewDrawData(List<DrawGraph> drawGraphs) {
        this.drawGraphs = drawGraphs;
    }

    public List<DrawGraph> getDrawGraphs() {
        return drawGraphs;
    }

    @Override
    public String toString() {
        return "PreviewDrawData{" +
                "drawGraphs=" + drawGraphs +
                '}';
    }
}
