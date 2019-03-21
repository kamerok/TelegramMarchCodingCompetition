package com.kamer.chartapp.data;

import java.util.Arrays;
import java.util.List;

public class InputData {

    private List<InputGraph> graphs;
    private long[] timestamps;

    public InputData(List<InputGraph> graphs, long[] timestamps) {
        this.graphs = graphs;
        this.timestamps = timestamps;
    }

    public List<InputGraph> getGraphs() {
        return graphs;
    }

    public long[] getTimestamps() {
        return timestamps;
    }

    @Override
    public String toString() {
        return "InputData{" +
                "graphs=" + graphs +
                ", timestamps=" + Arrays.toString(timestamps) +
                '}';
    }
}
