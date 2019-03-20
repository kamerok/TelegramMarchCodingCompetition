package com.kamer.chartapp.view.data;

import java.util.Arrays;
import java.util.Objects;

public class YGuides {

    private float[] percent;
    private boolean isActive;

    public YGuides(float[] percent, boolean isActive) {
        this.percent = percent;
        this.isActive = isActive;
    }

    public float[] getPercent() {
        return percent;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return "YGuides{" +
                "percent=" + Arrays.toString(percent) +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YGuides yGuides = (YGuides) o;
        return isActive == yGuides.isActive &&
                Arrays.equals(percent, yGuides.percent);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(isActive);
        result = 31 * result + Arrays.hashCode(percent);
        return result;
    }
}
