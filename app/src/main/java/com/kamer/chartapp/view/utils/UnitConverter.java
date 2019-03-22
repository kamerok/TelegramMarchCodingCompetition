package com.kamer.chartapp.view.utils;

import android.content.res.Resources;
import android.util.TypedValue;

public class UnitConverter {

    public static float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

}
