package com.kamer.chartapp.view.data;

import java.util.List;

public class InputGraph {

    private List<InputItem> values;
    private int color;

    public InputGraph(List<InputItem> values, int color) {
        this.values = values;
        this.color = color;
    }

    public List<InputItem> getValues() {
        return values;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "InputGraph{" +
                "values=" + values +
                ", color=" + color +
                '}';
    }
}
