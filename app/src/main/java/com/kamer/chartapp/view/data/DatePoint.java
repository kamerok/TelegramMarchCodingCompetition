package com.kamer.chartapp.view.data;

public class DatePoint {

    private float percent;
    private long timestamp;
    private String text;
    private String textExtended;

    public DatePoint(float percent, long timestamp, String text, String textExtended) {
        this.percent = percent;
        this.timestamp = timestamp;
        this.text = text;
        this.textExtended = textExtended;
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

    public String getTextExtended() {
        return textExtended;
    }

    @Override
    public String toString() {
        return "DatePoint{" +
                "percent=" + percent +
                ", timestamp=" + timestamp +
                ", text='" + text + '\'' +
                ", textExtended='" + textExtended + '\'' +
                '}';
    }
}
