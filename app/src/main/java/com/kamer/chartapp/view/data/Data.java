package com.kamer.chartapp.view.data;

import java.util.ArrayList;
import java.util.List;

public class Data {

    private List<Graph> graphs = new ArrayList<>();
    private long minValue;
    private long maxValue;

    public Data(List<Graph> graphs, long minValue, long maxValue) {
        this.graphs = graphs;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public List<Graph> getGraphs() {
        return graphs;
    }

    public long getMinValue() {
        return minValue;
    }

    public long getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return "Data{" +
                "graphs=" + graphs +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }
}
