package com.oreo.ui.chatGpt.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CopyOnWriteArrayList

class Mp3Streamer(private val context: Context) {
    private val audioFiles = CopyOnWriteArrayList<File>()
    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    fun addChunk(base64Chunk: String, isCompleted: () -> Unit) {
        try {
            val bytes = Base64.decode(base64Chunk, Base64.DEFAULT)
            val tempFile = File.createTempFile("chunk_", ".mp3", context.cacheDir)
            tempFile.deleteOnExit()
            FileOutputStream(tempFile).use { it.write(bytes) }
            audioFiles.add(tempFile)

            if (mediaPlayer == null || mediaPlayer?.isPlaying == false) {
                playNext(isCompleted)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playNext(isCompleted: () -> Unit) {
        if (currentIndex >= audioFiles.size) {
            isCompleted.invoke()
            return
        }

        val file = audioFiles[currentIndex]
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                currentIndex++
                playNext(isCompleted)
            }
            prepare()
            start()
        }
    }

    fun playMusicFromAsset(afd: AssetFileDescriptor, isCompleted: () -> Unit) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
                setOnCompletionListener {
                    isCompleted.invoke()
                    afd.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

