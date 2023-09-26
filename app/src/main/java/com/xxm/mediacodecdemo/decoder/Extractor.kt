package com.xxm.mediacodecdemo.decoder

import android.content.Context
import android.icu.text.ListFormatter.Width
import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.text.BoringLayout
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.WindowManager
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
class Extractor {
    private val TAG = "Extractor"
    private var extractor by Delegates.notNull<MediaExtractor>()
    private var videoCodec: MediaCodec by Delegates.notNull()
    private var isCodec = false

    /**
     *
     * @param isAssert true表示asserts资源 false表示路径，注意权限申请
     * @param callback 用于解决视频播放宽高比问题
     */
    fun setDataSource(filePath: String, surface: Surface, isAssert: Boolean = true,callback: (Int, Int) -> Unit) {
        extractor = MediaExtractor()
        if (isAssert) {
            MyApplication.context.assets.openFd(filePath).also {
                extractor.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
        } else {
            extractor.setDataSource(filePath)
        }
        initData(surface,callback)
    }

    private fun initData(surface: Surface, callback: (Int, Int) -> Unit) {
        for (track in 0 until extractor.trackCount) {
            val mediaFormat = extractor.getTrackFormat(track)
            val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mimeType?.startsWith("video/") == true) {
                extractor.selectTrack(track)
                callbackWH(mediaFormat, callback)
                videoCodec = MediaCodec.createDecoderByType(mimeType)
                videoCodec.configure(mediaFormat, surface, null, 0)
                return
            }
        }
    }

    private fun callbackWH(mediaFormat: MediaFormat, callback: (Int, Int) -> Unit) {
        val width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
        val height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
        val manager = MyApplication.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(metrics)
        val aHeight = metrics.widthPixels * height / width
        callback(metrics.widthPixels, aHeight)
    }

    fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            var isEOS = false
            videoCodec.start()
            val startMs = System.currentTimeMillis()
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
                sleepRender(info, startMs)
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
        if (timeDifference > 25) {
            try {
                Thread.sleep(timeDifference)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}