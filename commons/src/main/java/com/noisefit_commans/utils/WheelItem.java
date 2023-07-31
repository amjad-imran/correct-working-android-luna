package com.noisefit_commans.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public final class WheelItem<T> {
	private final T data;
//	private final Bitmap icon;

	public WheelItem(T data) {
		this.data = data;
//		this.icon = null;
	}

	public WheelItem(Resources resources, T data) {
		this.data = data;
//		this.icon = BitmapFactory.decodeResource(resources, drawableId).copy(Bitmap.Config.ARGB_8888, true);
	}

	public WheelItem(T data, Bitmap bitmap) {
		this.data = data;
//		if (bitmap == null) {
//			icon = null;
//		} else {
//			this.icon = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//		}
	}

	public T getData() {
		return data;
	}

//	public Bitmap getIcon() {
//		return icon;
//	}

	@Override
	public String toString() {
		if (data != null) {
			return data.toString();
		} else {
			return "";
		}
	}
}
