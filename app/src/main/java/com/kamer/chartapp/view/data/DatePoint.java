package com.kamer.chartapp.view.data;

public class DatePoint {

    private float percent;
    private long timestamp;
    private String text;

    public DatePoint(float percent, long timestamp, String text) {
        this.percent = percent;
        this.timestamp = timestamp;
        this.text = text;
    }

    public float getPercent() {
        return percent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "DatePoint{" +
                "percent=" + percent +
                ", timestamp=" + timestamp +
                ", text='" + text + '\'' +
                '}';
    }
}
