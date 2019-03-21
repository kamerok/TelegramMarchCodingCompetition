package com.kamer.chartapp.view.data.draw;

import android.graphics.Path;


public class DrawGraph {

    private int color;
    private Path path;
    private int alpha;

    public DrawGraph(int color, Path path, int alpha) {
        this.color = color;
        this.path = path;
        this.alpha = alpha;
    }

    public int getColor() {
        return color;
    }

    public Path getPath() {
        return path;
    }

    public int getAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        return "DrawGraph{" +
                "color=" + color +
                ", path=" + path +
                ", alpha=" + alpha +
                '}';
    }
}
