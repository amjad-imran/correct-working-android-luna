package com.noisefit.data.repository.implementation


import com.noisefit.data.remote.NetworkConstants
import com.noisefit.data.remote.abstraction.DownloadService
import com.noisefit.data.repository.abstraction.DownloadRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody
import java.io.File

class DownloadRepositoryImpl(
    private val downloadService: DownloadService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
)  : DownloadRepository {

    override suspend fun downloadFileFromUrl(url : String,file : File,fileName : String): Flow<Download> {
        return safeApiCallDownloadFlow(file,fileName,dispatcher){
            downloadService.downloadFileFromUrl(url)
        }
    }

}

suspend fun safeApiCallDownloadFlow(
    file: File, filename: String,
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> ResponseBody?
): Flow<Download> {

    return flow {
        emit(Download.Progress(0))

        try {
            // throws TimeoutCancellationException
            withTimeout(NetworkConstants.DOWNLOAD_TIMEOUT) {

                val response = apiCall.invoke()
                // flag to delete file if download errors or is cancelled
                val fileNew = File(file, filename)

                try {
                    response?.byteStream().use { inputStream ->
                        fileNew.outputStream().use { outputStream ->
                            val totalBytes = response?.contentLength()
                            val data = ByteArray(8_192)
                            var progressBytes = 0L

                            while (true) {
                                val bytes = inputStream?.read(data)

                                if (bytes == -1) {
                                    break
                                }

                                outputStream.channel
                                outputStream.write(data, 0, bytes!!)
                                progressBytes += bytes

                                emit(Download.Progress(percent = ((progressBytes * 100) / totalBytes!!).toInt()))
                            }

                            when {
                                progressBytes < totalBytes!! ->
                                    throw Exception("missing bytes")
                                progressBytes > totalBytes ->
                                    throw Exception("too many bytes")
                                else ->
                                {
                                    emit(Download.Finished(fileNew))
                                }
                            }
                        }
                    }

                } catch (exp: Exception) {
                    emit(Download.Failed)
                    exp.printStackTrace()
                } finally {
                    // check if download was successful

                }
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
            emit(Download.Failed)
        }

    }.flowOn(dispatcher).distinctUntilChanged()

}

sealed class Download {
    data class Progress(val percent: Int) : Download()
    data class Finished(val file: File) : Download()
    object Failed : Download()
}

