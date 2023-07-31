package com.noisefit.data.repository.abstraction

import com.noisefit.data.repository.implementation.Download
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DownloadRepository {

    suspend fun downloadFileFromUrl(url: String, file: File, fileName: String): Flow<Download>
}

