package com.oreo.ui.chatGpt.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CopyOnWriteArrayList

class Mp3Streamer(private val context: Context) {
    private val audioFiles = CopyOnWriteArrayList<File>()
    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    val isSpeaking: MutableLiveData<Boolean?> = MutableLiveData(null)
    fun addChunk(base64Chunk: String) {
        isSpeaking.postValue(true)
        try {
            val bytes = Base64.decode(base64Chunk, Base64.DEFAULT)
            val tempFile = File.createTempFile("chunk_", ".mp3", context.cacheDir)
            tempFile.deleteOnExit()
            FileOutputStream(tempFile).use { it.write(bytes) }
            audioFiles.add(tempFile)

            if (mediaPlayer == null || mediaPlayer?.isPlaying == false) {
                playNext()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playNext() {
        if (currentIndex >= audioFiles.size) {
            isSpeaking.postValue(false)
            return
        }

        val file = audioFiles[currentIndex]
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                currentIndex++
                playNext()
            }
            prepare()
            start()
        }
    }

    fun resume() = mediaPlayer?.start()
    fun pause() = mediaPlayer?.pause()

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentIndex = 0
        audioFiles.forEach { it.delete() }
        audioFiles.clear()
    }
}

