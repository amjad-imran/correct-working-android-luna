package com.noisefit_commans.utils.wheel;

import android.content.res.Resources;
import android.graphics.Bitmap;

public final class WheelItemPeriod<T> {
	private final T data;
//	private final Bitmap icon;

	public WheelItemPeriod(T data) {
		this.data = data;
//		this.icon = null;
	}

	public WheelItemPeriod(Resources resources, T data) {
		this.data = data;
//		this.icon = BitmapFactory.decodeResource(resources, drawableId).copy(Bitmap.Config.ARGB_8888, true);
	}

	public WheelItemPeriod(T data, Bitmap bitmap) {
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
