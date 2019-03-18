package com.kamer.chartapp.data;

public class InputItem {

    private long timestamp;
    private long value;

    public InputItem(long timestamp, long value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "InputItem{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
}
