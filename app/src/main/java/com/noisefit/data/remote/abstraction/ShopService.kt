package com.noisefit.data.remote.abstraction

import com.noisefit_commans.data.model.ShopBanner
import com.noisefit_commans.data.model.ShopProduct
import com.noisefit_commans.data.response.*
import retrofit2.http.GET
import retrofit2.http.Path


interface ShopService {
    //Shop APIs
    @GET("/mobile/revamp/category")
    suspend fun getShoppingBanners(): com.noisefit_commans.data.response.BaseApiResponse<List<ShopBanner>>

    @GET("/device_feature_images")
    suspend fun getDeviceFeatureImage(): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>

    @GET("/orders")
    suspend fun getOrders(): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>

    @GET("/mobile/category")
    suspend fun getMobileCategory(): com.noisefit_commans.data.response.BaseApiResponseData<ShopCategoryResponse>

    @GET("/mobile/category/list/{categoryId}/{sortMode}")
    suspend fun getCategoryProducts(
        @Path("categoryId") categoryId: String,
        @Path("sortMode") sortMode: String
    ): com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>

    @GET("/mobile/list/product/popular")
    suspend fun getPopularProduct(): com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>

    @GET("/mobile/list/product/recommend")
    suspend fun getRecommendedProduct(): com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>

    @GET("/mobile/list/product/best-selling")
    suspend fun getBestSellingProduct(): com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>

    @GET("/mobile/search/{searchText}")
    suspend fun searchProduct(
        @Path("searchText") searchText: String
    ): SearchProductResponse
}