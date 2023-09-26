package com.xxm.mediacodecdemo.decoder

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
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
class Extractor {
    private val TAG = "Extractor"
    private var extractor by Delegates.notNull<MediaExtractor>()
    private var videoCodec: MediaCodec by Delegates.notNull()
    private var isCodec = false

    fun setDataSource(filePath: String, surface: Surface) {
        extractor = MediaExtractor()
        extractor.setDataSource(filePath)
        initData(surface)
    }

    private fun initData(surface: Surface) {
        for (track in 0 until extractor.trackCount) {
            val mediaFormat = extractor.getTrackFormat(track)
            val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mimeType?.startsWith("video/") == true) {
                extractor.selectTrack(track)
                videoCodec = MediaCodec.createDecoderByType(mimeType)
                videoCodec.configure(mediaFormat, surface, null, 0)
                return
            }
        }
    }

    fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            var isEOS = false
            videoCodec.start()
            val info = BufferInfo()
            while (!isCodec) {
                if (!isEOS) {
                    val index = videoCodec.dequeueInputBuffer(10000)
                    if (index > 0) {
                        val buffer = videoCodec.getInputBuffer(index)
                        val size = extractor.readSampleData(buffer!!, 0)
                        if (size < 0) {
                            videoCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            videoCodec.queueInputBuffer(index, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIndex = videoCodec.dequeueOutputBuffer(info, 10000)
                if (outIndex >= 0) {
                    videoCodec.releaseOutputBuffer(outIndex, true)
                }
            }
            release()
        }
    }

    fun release() {
        videoCodec.stop()
        videoCodec.release()
        extractor.release()
    }

    private fun sleepRender(info: BufferInfo, startMs: Long) {
        val timeDifference = info.presentationTimeUs / 1000 - (System.currentTimeMillis() - startMs)
        if (timeDifference > 0) {
            try {
                Thread.sleep(timeDifference)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}