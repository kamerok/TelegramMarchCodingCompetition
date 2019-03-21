package com.kamer.chartapp.view.data;

/**
 * Percent position relative to complete graph
 */
public class GraphItem {

    private float percent;
    private long value;

    public GraphItem(float percent, long value) {
        this.percent = percent;
        this.value = value;
    }

    public float getPercent() {
        return percent;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "GraphItem{" +
                "percent=" + percent +
                ", value=" + value +
                '}';
    }
}
