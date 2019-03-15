package com.kamer.chartapp.view.data;

import java.util.List;

public class Graph {

    private int color;
    private List<GraphItem> items;

    public Graph(int color, List<GraphItem> items) {
        this.color = color;
        this.items = items;
    }

    public int getColor() {
        return color;
    }

    public List<GraphItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "color=" + color +
                ", items=" + items +
                '}';
    }
}
