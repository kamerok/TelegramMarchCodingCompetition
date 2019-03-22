package com.kamer.chartapp.view.data.draw;

import java.util.List;

public class DrawSelection {

    private float selection;
    private List<DrawSelectionPoint> points;
    private DrawSelectionPopup popup;

    public DrawSelection(float selection, List<DrawSelectionPoint> points, DrawSelectionPopup popup) {
        this.selection = selection;
        this.points = points;
        this.popup = popup;
    }

    public float getSelection() {
        return selection;
    }

    public List<DrawSelectionPoint> getPoints() {
        return points;
    }

    public DrawSelectionPopup getPopup() {
        return popup;
    }

    @Override
    public String toString() {
        return "DrawSelection{" +
                "selection=" + selection +
                ", points=" + points +
                ", popup=" + popup +
                '}';
    }
}
