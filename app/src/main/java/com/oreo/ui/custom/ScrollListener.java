package com.oreo.ui.custom;


import com.oreo.data.model.ChartModel;

public interface ScrollListener {

   public void onPositionSelected(int position, ChartModel chartModel);

    public void onScrolling(int position, ChartModel chartModel);

}

