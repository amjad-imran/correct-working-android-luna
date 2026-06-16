package com.oreo.receiver.workManager

import com.google.common.truth.Truth.assertThat
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import org.junit.Test

class OreoSyncDataWorkTest {

    @Test
    fun `hasPostedServerSyncResponse returns false for success with null body`() {
        val resource = Resource.Success<BaseApiResponse<VersionCheckResponse>>(null)

        assertThat(hasPostedServerSyncResponse(resource)).isFalse()
    }

    @Test
    fun `hasPostedServerSyncResponse returns true for success with posted body`() {
        val resource = Resource.Success(
            BaseApiResponse(
                message = "ok",
                data = VersionCheckResponse()
            )
        )

        assertThat(hasPostedServerSyncResponse(resource)).isTrue()
    }
}
