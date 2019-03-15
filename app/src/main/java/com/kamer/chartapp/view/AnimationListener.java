package com.kamer.chartapp.view;

import java.util.Map;

interface AnimationListener {

    void onValuesUpdated(float totalMinY, float totalMaxY, Map<String, Float> alphas);

}