package com.kamer.chartapp.data;

import java.util.List;

public class InputGraph {

    private List<InputItem> values;
    private int color;
    private String name;
    private boolean isEnabled;

    public InputGraph(List<InputItem> values, int color, String name, boolean isEnabled) {
        this.values = values;
        this.color = color;
        this.name = name;
        this.isEnabled = isEnabled;
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

    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public String toString() {
        return "InputGraph{" +
                "values=" + values +
                ", color=" + color +
                ", name='" + name + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
