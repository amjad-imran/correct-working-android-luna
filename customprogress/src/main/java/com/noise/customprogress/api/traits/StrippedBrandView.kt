package com.noise.customprogress.api.traits

import com.noise.customprogress.api.attrs.StrippedBrand

/**
 * Views which implement this interface change their color according to the given Bootstrap Brand
 */
interface StrippedBrandView {

	/**
	 * @return the current Bootstrap Brand
	 */
	/**
	 * Changes the color of the view to match the given Bootstrap Brand
	 *
	 * @param bootstrapBrand the Bootstrap Brand
	 */
	var strippedBrand: StrippedBrand

	companion object {

		val KEY = "com.noise.customprogress.api.view.BootstrapBrandView"
	}
	
}
