package com.noisefit_commans.data.response.stock

import com.google.gson.annotations.SerializedName

data class StockResponse(
    @SerializedName("data")
    val data: List<StockNetwork>?
)

data class StockNetwork(
    @SerializedName("symbol")
    val symbol: String = "",
    @SerializedName("country")
    val country: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("instrument_name")
    val instrumentName: String = "",
    @SerializedName("currency")
    val currency: String = "",
    @SerializedName("exchange")
    val exchange: String = "",
    @SerializedName("type")
    val type: String = ""
) {
    fun getDisplayName(): String {
        return name.ifEmpty { symbol }
    }

    fun matches(searchText: String) :Boolean{
        return name.contains(searchText,true) || symbol.contains(searchText,true)
    }
}