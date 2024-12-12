package com.oreo.util.audiorecorder

import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.LinkedList

internal class FileWriter(private val outputStream: DataOutputStream) {
    fun writeDataToStream(
        lastSkippedData: LinkedList<ByteArray>,
        data: ByteArray
    ) {
        if (lastSkippedData.isNotEmpty()) {
            lastSkippedData.forEach { outputStream.write(it) }
        }
        lastSkippedData.clear()
        outputStream.write(data)
    }

    fun writeDataToStream(
        lastSkippedData: LinkedList<FloatArray>,
        data: FloatArray
    ) {
        if (lastSkippedData.isNotEmpty()) {
            lastSkippedData.forEach { floatArray ->
                floatArray.forEach {
                    val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                        .putFloat(it).array()
                    outputStream.write(bytes)
                }
            }
        }
        lastSkippedData.clear()
        data.forEach {
            val bytes =
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(it)
                    .array()
            outputStream.write(bytes)
        }
    }

}