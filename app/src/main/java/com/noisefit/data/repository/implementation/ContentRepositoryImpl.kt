package com.noisefit.data.repository.implementation

import com.google.gson.JsonObject
import com.noisefit.luna.BuildConfig
import com.noisefit.data.model.ContentListData
import com.noisefit.data.model.SubCategoriesList
import com.noisefit.data.model.VideosList
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.ContentRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit_commans.data.response.BaseApiResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class ContentRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContentRepository {
    override suspend fun getContentList(): Flow<Resource<BaseApiResponse<ContentListData>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/content/list"
            remoteDataSource.getContentList(url)
        }
    }

    override suspend fun getSubCategoryVideoList(id:Int): Flow<Resource<BaseApiResponse<List<VideosList>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/content/sub_category/$id"
            remoteDataSource.getSubCategoriesList(url)
        }
    }

    override suspend fun getVideoDetails(id: Int): Flow<Resource<BaseApiResponse<VideosList>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/content/video_detail/$id"
            remoteDataSource.getVideoDetails(url)
        }
    }

    override suspend fun updateProgressVideo(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/content/video_progress"
            remoteDataSource.updateVideoProgress(url,request)
        }
    }

}