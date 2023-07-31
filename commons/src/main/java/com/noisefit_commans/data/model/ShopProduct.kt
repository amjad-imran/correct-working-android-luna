package com.noisefit_commans.data.model

data class ShopProduct(
    val handle: String,
    val id: String,
    val title: String,
    val images: List<ShopImage>,
    val variants: List<ShopProductVariant>?,
) {
    fun getProductUrl(): String {
        return "https://www.gonoise.com/products/$handle"
    }

    fun getProductPrice(): String {
        if (variants != null) {
            variants.firstOrNull()?.let {
                return it.price
            } ?: return ""

        } else {
            return ""
        }
    }
}

data class ShopImage(
    val src: String
)

data class ShopProductVariant(
    val price: String
)