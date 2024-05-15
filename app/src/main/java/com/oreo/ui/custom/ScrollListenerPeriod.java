package com.oreo.ui.custom;

import com.oreo.data.model.PeriodChartModel;

public interface ScrollListenerPeriod {

    public void onPositionSelected(int position, PeriodChartModel chartModel);

    public void onScrolling(int position, PeriodChartModel chartModel);

}
