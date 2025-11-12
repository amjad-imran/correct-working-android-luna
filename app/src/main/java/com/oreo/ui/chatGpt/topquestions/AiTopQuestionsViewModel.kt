package com.oreo.ui.chatGpt.topquestions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModelCompose
import com.oreo.data.model.ai.TopQuestions
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.ui.chatGpt.AITopics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class AiTopQuestionsViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository,
    private val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
) : BaseViewModelCompose() {

    val questions = MutableStateFlow<List<TopQuestions>>(arrayListOf())
    val showHistoryIcon = MutableStateFlow<Boolean>(false)
    val userName = MutableStateFlow<String>("")

    val IMAGE_MAX_BYTES = 5 * 1024 * 1024 // 5 MB
    val DEFAULT_IMAGE_QUALITY = 80 // JPEG quality (0-100)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            userName.value = localDataStore.getUser()?.firstName ?: ""
        }
    }

    fun getAiTopQuestions(aiTopic: AITopics) {
        viewModelScope.launch {
            oreoDeviceRepository.getAiTopQuestions(aiTopic).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getAiTopQuestions(aiTopic)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            questions.value = it.questions ?: arrayListOf()
                            showHistoryIcon.value = it.hasHistory
                        }
                    }
                }
            }
        }


    }

    fun getMimeType(context: Context, uri: Uri): String? =
        context.contentResolver.getType(uri)

    fun convertHeicToJpeg(context:Context,sourceUri: Uri, quality: Int): Uri? {
        return try {
            val resolver = context.contentResolver
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(resolver, sourceUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                resolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
            }

            if (bitmap == null) return null

            val outFile = File(context.cacheDir, "heic_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(0, 100), fos)
            }
            bitmap.recycle()

            FileProvider.getUriForFile(
                context,
                "com.noisefit.luna.fileprovider",
                outFile
            )
        } catch (_: Exception) {
            null
        }
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) cursor.getLong(sizeIndex) else -1L
        } ?: -1L
    }

    fun getDisplayName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }

}