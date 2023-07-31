package com.noisefit.data.repository.implementation

import com.noisefit_commans.data.model.ShopBanner
import com.noisefit_commans.data.model.ShopProduct
import com.noisefit.data.remote.abstraction.ShopService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.BaseApiResponseData
import com.noisefit_commans.data.response.SearchProductResponse
import com.noisefit_commans.data.response.ShopCategoryResponse
import com.noisefit.data.repository.abstraction.ShopRepository
import com.noisefit.data.safeApiCallFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class ShopRepositoryImpl(
    private val shopService: ShopService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ShopRepository {

    override suspend fun searchProduct(request: String): Flow<Resource<SearchProductResponse>> {
        return safeApiCallFlow(dispatcher) {
            shopService.searchProduct(request)
        }
    }

    override suspend fun getShoppingBanners(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<ShopBanner>>>> {
        return safeApiCallFlow(dispatcher) {
            shopService.getShoppingBanners()
        }
    }

    override suspend fun getPopularProduct(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>>> {
        return safeApiCallFlow(dispatcher) {
            shopService.getPopularProduct()
        }
    }

    override suspend fun getCategoryProducts(
        collectionId: String,
        sortMode: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>>> {
        return safeApiCallFlow(dispatcher) {
            shopService.getCategoryProducts(collectionId, sortMode)
        }
    }

    override suspend fun getRecommendedProduct(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>>> {
        return safeApiCallFlow(dispatcher) {
            shopService.getRecommendedProduct()
        }
    }

    override suspend fun getBestSellingProduct(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<ShopProduct>>>> {
        return safeApiCallFlow(dispatcher) {
            shopService.getBestSellingProduct()
        }
    }

    override suspend fun getCategoryList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<ShopCategoryResponse>>> {
        return safeApiCallFlow(dispatcher) {
            shopService.getMobileCategory()
        }
    }
}