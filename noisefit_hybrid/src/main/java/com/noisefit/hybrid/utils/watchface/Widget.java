package com.noisefit.hybrid.utils.watchface;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Size;

public class Widget {
    // Indicate the widget type, such as step widget
    /**
     * 0: heartrate
     * 1: step
     * 2: calorie
     * 3: distance
     * 4: date
     * 5: dial
     * 6: weather
     * 8: battery
     * 9: duration
     * 13: time
     * 16: hour hand
     * 17: minute hand
     * 18: second hand
     */
    private int type;
    // widget position
    private Point position;
    // widget size
    private Size size;

    // RGBA color
    // such as: 0xFFFFFFFF
    private int color = 0;

    // Indicate the widget style, such as step widget style1, ask firmware developer for supported styles
    // style: -1 none
    private int style = -1;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }
}
