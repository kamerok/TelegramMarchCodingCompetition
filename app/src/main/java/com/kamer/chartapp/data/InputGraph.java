package com.kamer.chartapp.data;

import java.util.List;

public class InputGraph {

    private List<InputItem> values;
    private int color;
    private String name;

    public InputGraph(List<InputItem> values, int color, String name) {
        this.values = values;
        this.color = color;
        this.name = name;
    }

    public List<InputItem> getValues() {
        return values;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "InputGraph{" +
                "values=" + values +
                ", color=" + color +
                ", name='" + name + '\'' +
                '}';
    }
}
