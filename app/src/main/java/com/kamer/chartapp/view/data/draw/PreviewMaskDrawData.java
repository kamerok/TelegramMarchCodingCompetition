package com.kamer.chartapp.view.data.draw;


public class PreviewMaskDrawData {

    private float left;
    private float right;

    public PreviewMaskDrawData(float left, float right) {
        this.left = left;
        this.right = right;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "PreviewMaskDrawData{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
