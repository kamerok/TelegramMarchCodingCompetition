package com.kamer.chartapp.view.data;

import java.util.List;

public class Graph {

    private int color;
    private List<GraphItem> items;
    private boolean isEnabled;
    private float alpha;

    public Graph(int color, List<GraphItem> items, boolean isEnabled, float alpha) {
        this.color = color;
        this.items = items;
        this.isEnabled = isEnabled;
        this.alpha = alpha;
    }

    public int getColor() {
        return color;
    }

    public List<GraphItem> getItems() {
        return items;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "color=" + color +
                ", items=" + items +
                ", isEnabled=" + isEnabled +
                ", alpha=" + alpha +
                '}';
    }
}
