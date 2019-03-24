package com.kamer.chartapp.view.animator;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;

import com.kamer.chartapp.view.PreviewView;
import com.kamer.chartapp.view.data.Graph;
import com.kamer.chartapp.view.surface.ChartView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphAlphaAnimator {

    private PreviewView previewView;
    private ChartView chartView;

    private List<Graph> graphs;

    private Map<String, Float> graphAlphas = new HashMap<>();
    private ValueAnimator graphAlphaAnimation;

    public GraphAlphaAnimator(PreviewView previewView, ChartView chartView) {
        this.previewView = previewView;
        this.chartView = chartView;
    }

    public void setData(List<Graph> data) {
        this.graphs = data;
        graphAlphas = new HashMap<>();
        if (graphAlphaAnimation != null) {
            graphAlphaAnimation.cancel();
        }
    }

    public void animateGraphAlphas() {
        PropertyValuesHolder[] properties = new PropertyValuesHolder[graphs.size()];
        for (int i = 0; i < graphs.size(); i++) {
            Graph graph = graphs.get(i);
            String name = graph.getName();
            properties[i] = PropertyValuesHolder.ofFloat(name, getGraphAlpha(name), graph.isEnabled() ? 1f : 0f);
        }
        if (graphAlphaAnimation != null) {
            graphAlphaAnimation.cancel();
        }
        ValueAnimator animator = new ValueAnimator();
        animator.setValues(properties);
        animator.setDuration(100);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                HashMap<String, Float> newAlphas = new HashMap<>();
                for (Graph graph : graphs) {
                    Object animatedValue = valueAnimator.getAnimatedValue(graph.getName());
                    float alpha = animatedValue != null ? (float) animatedValue : 1f;
                    newAlphas.put(graph.getName(), alpha);
                }
                graphAlphas = newAlphas;
                previewView.setAlphas(newAlphas);
                chartView.setGraphAlphas(newAlphas);
            }
        });
        graphAlphaAnimation = animator;
        animator.start();
    }

    private float getGraphAlpha(String name) {
        Float animatedAlpha = graphAlphas.get(name);
        return animatedAlpha != null ? animatedAlpha : 1f;
    }

}
