package com.noisefit.data.repository.abstraction

import com.noisefit_commans.data.model.ShopBanner
import com.noisefit_commans.data.model.ShopProduct
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.BaseApiResponseData
import com.noisefit_commans.data.response.SearchProductResponse
import com.noisefit_commans.data.response.ShopCategoryResponse
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    suspend fun searchProduct(request: String): Flow<Resource<SearchProductResponse>>

    suspend fun getShoppingBanners(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<ShopBanner>>>>
    suspend fun getPopularProduct(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>>>
    suspend fun getCategoryProducts(
        collectionId: String,
        sortMode: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>>>

    suspend fun getRecommendedProduct(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>>>
    suspend fun getBestSellingProduct(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>>>

    suspend fun getCategoryList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<ShopCategoryResponse>>>
}