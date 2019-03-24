package com.kamer.chartapp.view.utils;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

public class DrawUtils {

    public static Path scalePath(int width, int height, Path path, float minY, float maxY, float minX, float maxX, float paddingVertical, float paddingHorizontal) {

        float scaledWidth = width * 1f / (maxX - minX);
        float scaledHeight = (height - paddingVertical * 2) * (1f / (maxY - minY));

        Path result = new Path(path);

        //scale
        Matrix scale = new Matrix();
        scale.setScale(scaledWidth, -scaledHeight);
        result.transform(scale);

        //move
        float moveX = -minX * scaledWidth;
        float moveY = scaledHeight - (1 - maxY) * scaledHeight + paddingVertical;
        result.offset(moveX, moveY);

        //x insets
        Matrix inset = new Matrix();
        RectF bounds = new RectF();
        result.computeBounds(bounds, true);
        RectF boundsWithInsets = new RectF(bounds);
        boundsWithInsets.inset(paddingHorizontal, 0);
        inset.setRectToRect(bounds, boundsWithInsets, Matrix.ScaleToFit.FILL);
        result.transform(inset);

        return result;
    }

}
