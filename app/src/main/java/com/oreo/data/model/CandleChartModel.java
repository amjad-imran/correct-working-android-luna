package com.oreo.data.model;

public class CandleChartModel extends ChartModel {

    private int length;
    private int color;

    private String bottomLineText;
    private Type type;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getBottomLineText() {
        return bottomLineText;
    }

    public void setBottomLineText(String bottomLineText) {
        this.bottomLineText = bottomLineText;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }



    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        HIGH, MEDIUM, LOW, INACTIVE
    }
}
