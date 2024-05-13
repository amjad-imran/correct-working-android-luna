package com.noisefit_commans.utils.wheel;

import com.noisefit_commans.utils.WheelPicker;

import java.util.List;

public final class WheelAdapterPeriod<T> {
    private List<WheelItemPeriod<T>> data;
    private OnItemSelectedListener<T> onItemSelectedListener;
    private WheelPickerPeriod wheelPicker;

    public void bind(WheelPickerPeriod wheelPicker) {
        this.wheelPicker = wheelPicker;
        wheelPicker.setAdapter(this);
    }

    /**
     * Apply new {@code data} to {@link WheelPicker} and select first item without animation
     */
    public void setData(List<WheelItemPeriod<T>> data) {
        setData(data, 0);
    }

    /**
     * Apply new {@code data} to {@link WheelPicker} and select item with this {@code position} without animation
     */
    public void setData(List<WheelItemPeriod<T>> data, int selectedItemPosition) {
        setData(data, selectedItemPosition, false);
    }

    /**
     * Apply new {@code data} to {@link WheelPicker} and select item with this {@code position} with animation if animated == true
     */
    public void setData(List<WheelItemPeriod<T>> data, int selectedItemPosition, boolean animated) {
        this.data = data;

        if (wheelPicker != null) {
            wheelPicker.setAdapter(this);
            if (selectedItemPosition < data.size()) {
                wheelPicker.setSelectedItemPosition(selectedItemPosition, animated);
            }
        }
    }

    public List<WheelItemPeriod<T>> getData() {
        return data;
    }

    public int getSize() {
        return data != null ? data.size() : 0;
    }

    /**
     * @return selected item position which was set by {@link WheelAdapterPeriod#setSelectedItemPosition(int)}
     */
    public int getSelectedItemPosition() {
        return wheelPicker.getSelectedItemPosition();
    }

    /**
     * @return selected item position during wheel's scroll
     */
    public int getCurrentItemPosition() {
        return wheelPicker.getCurrentItemPosition();
    }

    public void setSelectedItemPosition(int position) {
        wheelPicker.setSelectedItemPosition(position, true);
    }

    public void setSelectedItemPosition(int position, boolean animated) {
        wheelPicker.setSelectedItemPosition(position, animated);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener<T> onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public WheelItemPeriod<T> getCurrentItem() {
        if (data == null || data.size() == 0 || wheelPicker.getCurrentItemPosition() >= data.size() || wheelPicker.getCurrentItemPosition() < 0) {
            return null;
        }

        return data.get(wheelPicker.getCurrentItemPosition());
    }

    void onItemSelected(int position) {
        if (onItemSelectedListener != null && data != null && position < data.size() && position >= 0) {
            onItemSelectedListener.onItemSelected(data.get(position).getData());
        }
    }

    public interface OnItemSelectedListener<T> {
        void onItemSelected(T item);
    }
}
