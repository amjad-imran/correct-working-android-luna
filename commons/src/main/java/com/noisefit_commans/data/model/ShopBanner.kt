package com.noisefit_commans.data.model

data class ShopBanner(
    val img: String,
    val url: String?=null,
    val handle: String?=null,
) {
    fun getFullUrl(): String {
        if(url.isNullOrEmpty()){
            if(!handle.isNullOrEmpty()){
                return "https://www.gonoise.com/products/$handle"
            }
            return "https://www.gonoise.com/"
        }else{
            return url
        }
    }
}