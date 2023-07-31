package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.model.ContentListData
import com.noisefit.data.model.SubCategoriesList
import com.noisefit.data.model.VideosList
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    suspend fun getContentList(): Flow<Resource<BaseApiResponse<ContentListData>>>
    suspend fun getSubCategoryVideoList(id:Int): Flow<Resource<BaseApiResponse<List<VideosList>>>>
    suspend fun getVideoDetails(id:Int): Flow<Resource<BaseApiResponse<VideosList>>>
    suspend fun updateProgressVideo(request:JsonObject): Flow<Resource<BaseApiResponse<Any>>>

}