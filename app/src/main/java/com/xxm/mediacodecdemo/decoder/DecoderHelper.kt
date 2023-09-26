package com.xxm.mediacodecdemo.decoder

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.provider.MediaStore.Audio.Media
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import com.xxm.mediacodecdemo.MyApplication
import kotlin.properties.Delegates

/**
 *Time:2023/9/22
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
class DecoderHelper {

    private var mediaCodec: MediaCodec by Delegates.notNull()
    private val windowManager by lazy {
        MyApplication.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val metrics by lazy {
        DisplayMetrics().apply {
            windowManager.defaultDisplay.getMetrics(this)
        }
    }

    private var mediaFormat by Delegates.notNull<MediaFormat>()
    private lateinit var surface: Surface

    fun init() {
        mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, metrics.widthPixels, metrics.heightPixels)
        //视频码率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,5*metrics.widthPixels*metrics.heightPixels);
        //视频宽高
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH,metrics.widthPixels)
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT,metrics.heightPixels)
        //视频帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30)
        //视频I帧间隔
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1)
        mediaCodec.configure(mediaFormat,surface,null,0)
    }

    fun init(mediaCodecInfo: MediaCodecInfo) {
        mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.name)
    }


}