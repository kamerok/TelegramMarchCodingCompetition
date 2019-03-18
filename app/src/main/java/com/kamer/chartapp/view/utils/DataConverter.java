package com.kamer.chartapp.view.utils;

import com.kamer.chartapp.data.InputGraph;
import com.kamer.chartapp.data.InputItem;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.GraphItem;

import java.util.ArrayList;
import java.util.List;

public class DataConverter {

    public static List<Graph> convertInput(List<InputGraph> inputGraphs) {
        List<Graph> result = new ArrayList<>();

        List<InputItem> allItems = new ArrayList<>();
        for (InputGraph inputGraph : inputGraphs) {
            allItems.addAll(inputGraph.getValues());
        }
        if (allItems.isEmpty()) return result;
        long[] range = calculateYRange(allItems);
        long min = range[0];
        long max = range[1];
        long verticalLength = Math.abs(min - max);

        for (InputGraph inputGraph : inputGraphs) {
            List<GraphItem> items = new ArrayList<>();
            List<InputItem> data = inputGraph.getValues();
            for (int i = 0; i < data.size(); i++) {
                float x = (float) i / (data.size() - 1);
                float y = Math.abs(min - data.get(i).getValue()) / (float) verticalLength;
                items.add(new GraphItem(x, y));
            }
            result.add(new Graph(inputGraph.getName(), inputGraph.getColor(), items, true));
        }
        return result;
    }

    private static long[] calculateYRange(List<InputItem> data) {
        long verticalMin = data.get(0).getValue();
        long verticalMax = data.get(0).getValue();
        for (int i = 1; i < data.size(); i++) {
            long value = data.get(i).getValue();
            if (value > verticalMax) {
                verticalMax = value;
            } else if (value < verticalMin) {
                verticalMin = value;
            }
        }
        return new long[]{verticalMin, verticalMax};
    }

}
