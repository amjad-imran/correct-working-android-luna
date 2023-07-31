package com.noisefit_commans.utils;

import java.util.List;

public final class WheelAdapter<T> {
	private List<WheelItem<T>> data;
	private OnItemSelectedListener<T> onItemSelectedListener;
	private WheelPicker wheelPicker;

	public void bind(WheelPicker wheelPicker) {
		this.wheelPicker = wheelPicker;
		wheelPicker.setAdapter(this);
	}

	/**
	 * Apply new {@code data} to {@link WheelPicker} and select first item without animation
	 */
	public void setData(List<WheelItem<T>> data) {
		setData(data, 0);
	}

	/**
	 * Apply new {@code data} to {@link WheelPicker} and select item with this {@code position} without animation
	 */
	public void setData(List<WheelItem<T>> data, int selectedItemPosition) {
		setData(data, selectedItemPosition, false);
	}

	/**
	 * Apply new {@code data} to {@link WheelPicker} and select item with this {@code position} with animation if animated == true
	 */
	public void setData(List<WheelItem<T>> data, int selectedItemPosition, boolean animated) {
		this.data = data;

		if (wheelPicker != null) {
			wheelPicker.setAdapter(this);
			if (selectedItemPosition < data.size()) {
				wheelPicker.setSelectedItemPosition(selectedItemPosition, animated);
			}
		}
	}

	public List<WheelItem<T>> getData() {
		return data;
	}

	public int getSize() {
		return data != null ? data.size() : 0;
	}

	/**
	 * @return selected item position which was set by {@link WheelAdapter#setSelectedItemPosition(int)}
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

	public WheelItem<T> getCurrentItem() {
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
