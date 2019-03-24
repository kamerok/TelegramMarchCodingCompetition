package com.kamer.chartapp.view.utils;

import android.graphics.Path;

import com.kamer.chartapp.data.InputData;
import com.kamer.chartapp.data.InputGraph;
import com.kamer.chartapp.view.data.Data;
import com.kamer.chartapp.view.data.DatePoint;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.data.GraphItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataConverter {

    private static final SimpleDateFormat format = new SimpleDateFormat("MMM d", Locale.ENGLISH);
    private static final SimpleDateFormat extendedFormat = new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH);

    public static Data convertInput(InputData inputData) {

        long minX = inputData.getTimestamps()[0];
        long maxX = inputData.getTimestamps()[inputData.getTimestamps().length - 1];
        long dateLength = Math.abs(minX - maxX);
        List<DatePoint> datePoints = new ArrayList<>();
        for (long timestamp : inputData.getTimestamps()) {
            float percent = (timestamp - minX) / (float) dateLength;
            datePoints.add(new DatePoint(percent, timestamp, format.format(timestamp), extendedFormat.format(timestamp)));
        }

        long minY = Long.MAX_VALUE;
        long maxY = Long.MIN_VALUE;
        for (int i = 0; i < inputData.getGraphs().size(); i++) {
            InputGraph inputGraph = inputData.getGraphs().get(i);
            for (long value : inputGraph.getValues()) {
                if (value < minY) minY = value;
                if (value > maxY) maxY = value;
            }
        }
        long verticalLength = Math.abs(minY - maxY);


        List<Graph> graphs = new ArrayList<>();
        for (InputGraph inputGraph : inputData.getGraphs()) {
            long[] data = inputGraph.getValues();
            List<GraphItem> items = new ArrayList<>();
            Path path = new Path();

            for (int i = 0; i < datePoints.size(); i++) {
                float x = datePoints.get(i).getPercent();
                float y = Math.abs(minY - data[i]) / (float) verticalLength;
                items.add(new GraphItem(y, data[i]));
                if (path.isEmpty()) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }

            graphs.add(new Graph(inputGraph.getName(), inputGraph.getColor(), items, path, true));
        }
        return new Data(graphs, datePoints, minY, maxY);
    }

}
