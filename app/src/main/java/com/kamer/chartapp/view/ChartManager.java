package com.kamer.chartapp.view;

import android.support.annotation.FloatRange;

import com.kamer.chartapp.view.data.InputGraph;

import java.util.ArrayList;
import java.util.List;

public class ChartManager {

    private ChartView chartView;
    private PreviewView previewView;
    private UpdateListener updateListener;

    private List<InputGraph> graphs = new ArrayList<>();

    public ChartManager(ChartView chartView, PreviewView previewView, UpdateListener updateListener) {
        this.chartView = chartView;
        this.previewView = previewView;
        this.updateListener = updateListener;

        chartView.externalListener = previewView;
    }

    public void setData(final List<InputGraph> data) {
        chartView.post(new Runnable() {
            @Override
            public void run() {
                graphs = data;
                chartView.setData(data);
                sync();
            }
        });
    }

    public void setLeftBorder(@FloatRange(from = 0, to = 1) float leftBorder) {
        chartView.setLeftBorder(leftBorder);
        sync();
    }

    public void setRightBorder(@FloatRange(from = 0, to = 1) float rightBorder) {
        chartView.setRightBorder(rightBorder);
        sync();
    }

    public void setPan(@FloatRange(from = 0, to = 1) float pan) {
        chartView.setPan(pan);
        sync();
    }

    public void updateGraphEnabled(String name, boolean isEnabled) {
        for (int i = 0; i < graphs.size(); i++) {
            InputGraph graph = graphs.get(i);
            if (graph.getName().equals(name)) {
                graphs.set(i, new InputGraph(graph.getValues(), graph.getColor(), graph.getName(), isEnabled));
                chartView.setData(graphs);
                sync();
                return;
            }
        }
    }

    private void sync() {
        previewView.setData(chartView.graphs, chartView.getRightBorder(), chartView.getLeftBorder());
        updateListener.onUpdate(chartView.getLeftBorder(), chartView.getRightBorder(), chartView.getPan(), graphs);
    }

    public interface UpdateListener {

        void onUpdate(float left, float right, float pan, List<InputGraph> graphs);

    }

}
