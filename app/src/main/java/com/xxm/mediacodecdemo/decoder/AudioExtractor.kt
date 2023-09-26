package com.xxm.mediacodecdemo.decoder

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.xxm.mediacodecdemo.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

/**
 *Time:2023/9/25
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
class AudioExtractor {
    private val TAG = "Extractor"
    private var extractor by Delegates.notNull<MediaExtractor>()
    private var audioCodec: MediaCodec by Delegates.notNull()
    private var audioTrack: AudioTrack by Delegates.notNull()
    private var isCodec = false
    private var miniBufferSize = 0


    fun setDataSource(filePath: String, isAssert: Boolean = true) {
        extractor = MediaExtractor()
        if (isAssert) {
            MyApplication.context.assets.openFd(filePath).also {
                extractor.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
        } else {
            extractor.setDataSource(filePath)
        }
        initData()
    }

    private fun initData() {
        for (track in 0 until extractor.trackCount) {
            val mediaFormat = extractor.getTrackFormat(track)
            val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mimeType?.startsWith("audio/") == true) {
                extractor.selectTrack(track)
                audioCodec = MediaCodec.createDecoderByType(mimeType)
                audioCodec.configure(mediaFormat, null, null, 0)
                val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                val bitRate = try {
                    mediaFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
                } catch (e: Exception) {
                    e.printStackTrace()
                    AudioFormat.ENCODING_PCM_16BIT
                }
                val channel = if (channelCount == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
                miniBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate, channel, bitRate
                )
                audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channel, bitRate, miniBufferSize, AudioTrack.MODE_STREAM)
                return
            }
        }
    }

    fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            var isEOS = false
            audioCodec.start()
            audioTrack.play()
            var buffer: ShortArray = ShortArray(miniBufferSize / 2)
            val info = BufferInfo()
            while (!isCodec) {
                if (!isEOS) {
                    val index = audioCodec.dequeueInputBuffer(10000)
                    if (index > 0) {
                        val buffer = audioCodec.getInputBuffer(index)
                        val size = extractor.readSampleData(buffer!!, 0)
                        if (size < 0) {
                            audioCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            audioCodec.queueInputBuffer(index, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    } else {
                        when (index) {
                            MediaCodec.INFO_TRY_AGAIN_LATER -> {
                                // Log.d(TAG,"INFO_TRY_AGAIN_LATER")
                            }

                            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED")
                            }

                            MediaCodec.INFO_TRY_AGAIN_LATER -> {
                                Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED")
                            }
                        }
                    }
                }


                val outIndex = audioCodec.dequeueOutputBuffer(info, 10000)
                if (outIndex >= 0) {
                    val outData = audioCodec.outputBuffers[outIndex]
                    outData.position(0)
                    outData.asShortBuffer().get(buffer, 0, info.size / 2)
                    audioTrack.write(buffer, 0, info.size / 2)
                    audioCodec.releaseOutputBuffer(outIndex, true)

                }
            }
            release()
        }
    }

    fun release() {
        audioCodec.stop()
        audioCodec.release()
        extractor.release()
    }

}