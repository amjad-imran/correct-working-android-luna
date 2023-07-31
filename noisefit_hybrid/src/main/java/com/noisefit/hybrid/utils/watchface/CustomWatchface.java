package com.noisefit.hybrid.utils.watchface;

import android.util.Size;

import java.util.List;



public class CustomWatchface {

    private int id;

    private String imagePath;
    private Size size;
    private Size thumbnailSize;
    private List<Widget> widgetList;

    public boolean hasWidgets() {
        return (getWidgetList() != null && getWidgetList().size() > 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Size getThumbnailSize() {
        return thumbnailSize;
    }

    public void setThumbnailSize(Size thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public List<Widget> getWidgetList() {
        return widgetList;
    }

    public void setWidgetList(List<Widget> widgetList) {
        this.widgetList = widgetList;
    }
}
