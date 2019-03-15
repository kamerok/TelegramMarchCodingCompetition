package com.kamer.chartapp.view.data;

import java.util.List;

public class Graph {

    private String name;
    private int color;
    private List<GraphItem> items;
    private boolean isEnabled;

    public Graph(String name, int color, List<GraphItem> items, boolean isEnabled) {
        this.name = name;
        this.color = color;
        this.items = items;
        this.isEnabled = isEnabled;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "Graph{" +
                "name='" + name + '\'' +
                ", color=" + color +
                ", items=" + items +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
