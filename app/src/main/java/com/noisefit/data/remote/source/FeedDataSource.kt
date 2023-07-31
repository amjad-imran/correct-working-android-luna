package com.noisefit.data.remote.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bumptech.glide.load.HttpException
import com.noisefit.BuildConfig
import com.noisefit.data.model.timeline.TimelineData
import com.noisefit.data.remote.abstraction.NetworkService
import java.io.IOException

private const val TIMELINE_PAGE_INDEX = 1

class FeedDataSource(
    private val remoteDataSource: NetworkService
) : PagingSource<Int, TimelineData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TimelineData> {
        val pageIndex = params.key ?: TIMELINE_PAGE_INDEX
        val url = "${BuildConfig.BASE_URL_NEW}/feeds/user/dashboard/timeline?page=$pageIndex"
        return try {
            val response = remoteDataSource.getDashboardFeed(
                url
            )
            val timelineData = response.data
            val nextKey =
                if (timelineData?.has_next == false) {
                    null
                } else {
                    pageIndex + 1
                }
            LoadResult.Page(
                data = timelineData?.response.apply {
                    this?.forEach {
                        if (it.feedType.equals("ad", true)) {
                            it.postId = (it.actionId * -1L)
                        }
                    }
                } ?: ArrayList(),
                prevKey = if (pageIndex == TIMELINE_PAGE_INDEX) null else pageIndex,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TimelineData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}