package com.kamer.chartapp.data;

import java.util.Arrays;

public class InputGraph {

    private long[] values;
    private int color;
    private String name;

    public InputGraph(long[] values, int color, String name) {
        this.values = values;
        this.color = color;
        this.name = name;
    }

    public long[] getValues() {
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
                "values=" + Arrays.toString(values) +
                ", color=" + color +
                ", name='" + name + '\'' +
                '}';
    }
}
