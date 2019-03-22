package com.kamer.chartapp.view.data.draw;

import android.util.Pair;

import java.util.List;

public class DrawSelectionPopup {

    private float border;
    private boolean isAlignedRight;
    private String dateText;
    private List<Pair<String, Integer>> values;

    public DrawSelectionPopup(float border, boolean isAlignedRight, String dateText, List<Pair<String, Integer>> values) {
        this.border = border;
        this.isAlignedRight = isAlignedRight;
        this.dateText = dateText;
        this.values = values;
    }

    public float getBorder() {
        return border;
    }

    public boolean isAlignedRight() {
        return isAlignedRight;
    }

    public String getDateText() {
        return dateText;
    }

    public List<Pair<String, Integer>> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "DrawSelectionPopup{" +
                "border=" + border +
                ", isAlignedRight=" + isAlignedRight +
                ", dateText='" + dateText + '\'' +
                ", values=" + values +
                '}';
    }
}
