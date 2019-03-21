package com.kamer.chartapp.view.data;

import java.util.List;

public class Data {

    private List<Graph> graphs;
    private List<DatePoint> datePoints;
    private long minValue;
    private long maxValue;

    public Data(List<Graph> graphs, List<DatePoint> datePoints, long minValue, long maxValue) {
        this.graphs = graphs;
        this.datePoints = datePoints;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public List<Graph> getGraphs() {
        return graphs;
    }

    public List<DatePoint> getDatePoints() {
        return datePoints;
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
                ", datePoints=" + datePoints +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }
}
