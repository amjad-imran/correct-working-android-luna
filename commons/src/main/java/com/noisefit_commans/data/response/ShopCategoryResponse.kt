package com.noisefit_commans.data.response

import com.noisefit_commans.data.model.ShopBanner
import com.noisefit_commans.data.model.ShopCategory


data class ShopCategoryResponse(
    val category : List<ShopCategory>,
    val banner1 : ShopBanner,
    val banner2 : ShopBanner,
    val slider : List<ShopBanner>
)
