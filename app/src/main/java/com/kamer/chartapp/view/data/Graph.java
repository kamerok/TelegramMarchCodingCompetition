package com.kamer.chartapp.view.data;

import android.graphics.Path;

import java.util.List;

public class Graph {

    private String name;
    private int color;
    private List<GraphItem> items;
    private Path path;
    private boolean isEnabled;

    public Graph(String name, int color, List<GraphItem> items, Path path, boolean isEnabled) {
        this.name = name;
        this.color = color;
        this.items = items;
        this.path = path;
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

    public Path getPath() {
        return path;
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
                ", path=" + path +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
