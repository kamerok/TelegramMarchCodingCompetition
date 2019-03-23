package com.kamer.chartapp.view.data.draw;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrawYGuides that = (DrawYGuides) o;
        return alpha == that.alpha &&
                Arrays.equals(yGuides, that.yGuides) &&
                Objects.equals(texts, that.texts);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(texts, alpha);
        result = 31 * result + Arrays.hashCode(yGuides);
        return result;
    }
}
