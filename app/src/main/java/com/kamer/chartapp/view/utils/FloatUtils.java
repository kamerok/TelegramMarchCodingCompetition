package com.kamer.chartapp.view.utils;

public class FloatUtils {

    public static boolean isFloatEquals(float f1, float f2) {
        return Math.abs(f1 - f2) < 0.0001;
    }

}
