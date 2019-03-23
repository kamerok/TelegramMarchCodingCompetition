package com.kamer.chartapp.view.data.draw;

import android.graphics.Path;

import java.util.Objects;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrawGraph drawGraph = (DrawGraph) o;
        return color == drawGraph.color &&
                alpha == drawGraph.alpha &&
                Objects.equals(path, drawGraph.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, path, alpha);
    }
}
