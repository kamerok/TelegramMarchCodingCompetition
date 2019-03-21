package com.kamer.chartapp.view.data.draw;

import java.util.List;

public class DrawSelection {

    private float selection;
    private List<DrawSelectionPoint> points;

    public DrawSelection(float selection, List<DrawSelectionPoint> points) {
        this.selection = selection;
        this.points = points;
    }

    public float getSelection() {
        return selection;
    }

    public List<DrawSelectionPoint> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "DrawSelection{" +
                "selection=" + selection +
                ", points=" + points +
                '}';
    }
}
