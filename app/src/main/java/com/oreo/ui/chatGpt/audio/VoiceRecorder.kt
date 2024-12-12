package com.oreo.ui.chatGpt.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.noisefit_commans.utils.LOGS
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class VoiceRecorder(private val context: Context) {
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var currentFile: FileOutputStream? = null
    private var lastSoundTime: Long = System.currentTimeMillis()
    private val silenceDuration: Long = 2000 // 2 seconds silence duration
    private var handler: Handler
    private var lastFile: File? = null

    private var peakCount = 0

    init {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize
        )
        handler = Handler(Looper.getMainLooper())
    }

    fun startRecording() {
        if (isRecording) return

        isRecording = true
        audioRecord?.startRecording()

        startNewRecording()

        val audioData = ByteArray(bufferSize)

        // Start the recording loop
        Thread {
            try {
                while (isRecording) {
                    val readResult = audioRecord?.read(audioData, 0, audioData.size) ?: 0

                    if (readResult > 0) {
                        val isSilent = isSilent(audioData)
                        val currentTime = System.currentTimeMillis()

                        LOGS.d("VoiceRecorder isSilent $isSilent")
                        if (isSilent) {
                            // If user is silent for more than 2 seconds, save the current file
                            if (currentTime - lastSoundTime > silenceDuration) {
                                lastSoundTime = currentTime
                                saveCurrentFile()
                                peakCount = 0
                                startNewRecording() // Start a new file after 2 seconds of silence
                            }
                        } else {
                            // If sound is detected, update the last sound time
                            lastSoundTime = currentTime
                            peakCount += 1
                        }

                        currentFile?.write(audioData, 0, readResult)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun isSilent(audioData: ByteArray): Boolean {
        var sum = 0.0
        for (i in audioData.indices step 2) {
            val sample = (audioData[i].toInt() or (audioData[i + 1].toInt() shl 8)).toShort()
            sum += (sample * sample).toDouble()
        }

        val rms = Math.sqrt(sum / (audioData.size / 2)) // RMS for 16-bit samples
        val db = 20 * Math.log10(rms) // Convert RMS to decibels

        LOGS.d("VoiceRecorder RMS: $rms, dB: $db")
        return db < 60/*silenceThreshold*/
    }

    private fun startNewRecording() {
        currentFile?.close()

        val fileName = "recording_${System.currentTimeMillis()}.wav"
        lastFile = File(context.filesDir, fileName)
        currentFile = FileOutputStream(lastFile)

        writeWavHeader(currentFile!!)

        Log.d("VoiceRecorder", "New recording started: ${lastFile?.absolutePath}")
    }

    private fun saveCurrentFile() {
        // Close the current file after 2 seconds of silence
        try {
            currentFile?.flush()
            currentFile?.close()
            if (peakCount<10) {
                Log.d("VoiceRecorder", "Recording deleted- no audio detected peakCount - $peakCount")
                lastFile?.delete()
                return
            }
            Log.d("VoiceRecorder", "Recording saved with audio peakCount - $peakCount")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        audioRecord?.stop()
        saveCurrentFile() // Save the last file if needed
    }

    private fun writeWavHeader(outputStream: FileOutputStream) {
        val header = ByteArray(44)

        // Chunk ID: "RIFF"
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()

        // Chunk Size: 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
        header[4] = 0 // Placeholder for chunk size
        header[5] = 0
        header[6] = 0
        header[7] = 0

        // Format: "WAVE"
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()

        // Subchunk1 ID: "fmt "
        header[12] = 'f'.toByte()
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()

        // Subchunk1 Size: 16 for PCM
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        // AudioFormat: 1 (PCM)
        header[20] = 1
        header[21] = 0

        // Number of Channels: 1 (Mono)
        header[22] = 1
        header[23] = 0

        // Sample Rate: 44100
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = ((sampleRate shr 8) and 0xFF).toByte()
        header[26] = ((sampleRate shr 16) and 0xFF).toByte()
        header[27] = ((sampleRate shr 24) and 0xFF).toByte()

        // Byte Rate: sampleRate * numChannels * bytesPerSample
        val byteRate = sampleRate * 1 * 2
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = ((byteRate shr 8) and 0xFF).toByte()
        header[30] = ((byteRate shr 16) and 0xFF).toByte()
        header[31] = ((byteRate shr 24) and 0xFF).toByte()

        // Block Align: numChannels * bytesPerSample
        header[32] = 2
        header[33] = 0

        // Bits per Sample: 16 (PCM)
        header[34] = 16
        header[35] = 0

        // Subchunk2 ID: "data"
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()

        // Subchunk2 Size: data size (0 for now)
        header[40] = 0
        header[41] = 0
        header[42] = 0
        header[43] = 0

        // Write the header
        outputStream.write(header)
    }
}