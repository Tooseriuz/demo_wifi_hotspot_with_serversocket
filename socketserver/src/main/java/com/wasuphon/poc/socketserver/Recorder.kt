package com.wasuphon.poc.socketserver

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

class Recorder {
    val sampleRate = 44100
    val channelConfig: Int = AudioFormat.CHANNEL_CONFIGURATION_MONO
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
    val minBufSize = AudioRecord.getMinBufferSize(
        sampleRate, channelConfig,
        audioFormat
    )
    val recorder = AudioRecord(
        MediaRecorder.AudioSource.DEFAULT,
        sampleRate, channelConfig, audioFormat,
        minBufSize
    )
    val buffer = ByteArray(minBufSize)

    fun record() {
        recorder.startRecording()
    }

    fun stop() {
        recorder.stop()
    }

    fun getAudioByte(): Pair<ByteArray, Int> {
        val readSize = recorder.read(buffer, 0, minBufSize)
        return Pair(buffer, readSize)
    }
}
