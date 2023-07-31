package com.noisefit.data.repository.pagingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bumptech.glide.load.HttpException
import com.google.gson.JsonObject
import com.noisefit.BuildConfig
import com.noisefit.data.model.timeline.FriendTimeline
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.data.remote.abstraction.NetworkService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.IOException


private const val TIMELINE_PAGE_INDEX = 1
const val NETWORK_PAGE_SIZE = 50


class TimelinePagingSource(
    private val remoteDataSource: NetworkService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PagingSource<Int, TimelineData>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TimelineData> {
        val pageIndex = params.key ?: TIMELINE_PAGE_INDEX
        val url =
            "${BuildConfig.BASE_URL_NEW}/feeds/user/timeline?page=${pageIndex}"
        return try {
            val response = remoteDataSource.getTimeLine(
                url
            )
            val timelineData = response.data
            val nextKey =
                if (timelineData?.response.isNullOrEmpty()) {
                    null
                } else {
                    // By default, initial load size = 3 * NETWORK PAGE SIZE
                    // ensure we're not requesting duplicating items at the 2nd request
                    pageIndex + (params.loadSize / NETWORK_PAGE_SIZE)
                }
            LoadResult.Page(
                data = timelineData?.response ?: ArrayList(),
                prevKey = if (pageIndex == TIMELINE_PAGE_INDEX) null else pageIndex,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    /**
     * The refresh key is used for subsequent calls to PagingSource.Load after the initial load.
     */
    override fun getRefreshKey(state: PagingState<Int, TimelineData>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index.
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}