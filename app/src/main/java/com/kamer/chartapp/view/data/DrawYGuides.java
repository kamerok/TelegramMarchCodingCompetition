package com.kamer.chartapp.view.data;

import java.util.Arrays;
import java.util.List;

public class DrawYGuides {

    private float[] yGuides;
    private List<DrawText> texts;
    private float alpha;

    public DrawYGuides(float[] yGuides, List<DrawText> texts, float alpha) {
        this.yGuides = yGuides;
        this.texts = texts;
        this.alpha = alpha;
    }

    public float[] getyGuides() {
        return yGuides;
    }

    public List<DrawText> getTexts() {
        return texts;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        return "DrawYGuides{" +
                "yGuides=" + Arrays.toString(yGuides) +
                ", texts=" + texts +
                ", alpha=" + alpha +
                '}';
    }
}
