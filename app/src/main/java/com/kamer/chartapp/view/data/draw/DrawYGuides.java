package com.kamer.chartapp.view.data.draw;

import java.util.Arrays;
import java.util.List;

public class DrawYGuides {

    private float[] yGuides;
    private List<DrawText> texts;
    private int alpha;

    public DrawYGuides(float[] yGuides, List<DrawText> texts, int alpha) {
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

    public int getAlpha() {
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
