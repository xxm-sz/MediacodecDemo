package com.xxm.mediacodecdemo

import android.app.Activity
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.databinding.DataBindingUtil
import com.xxm.mediacodecdemo.databinding.ActivityMainBinding
import com.xxm.mediacodecdemo.decoder.AudioExtractor
import com.xxm.mediacodecdemo.decoder.DecoderHelper
import com.xxm.mediacodecdemo.decoder.Extractor
import com.xxm.mediacodecdemo.util.Utils
import kotlin.properties.Delegates

/**
 *Time:2023/9/22
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
class MainActivity : Activity() {
    private val TAG = "MainActivity"

    private val decoderHelper by lazy {
        DecoderHelper()
    }

    private var surfaceHolder by Delegates.notNull<SurfaceHolder>()


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    //    Utils.getSupportMediaCodec()
        val extractor = Extractor()
        val audioExtractor=AudioExtractor()
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.surface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG,"surfaceCreated")
                surfaceHolder = holder
                val path="oceans.mp4"
                extractor.setDataSource(path, holder.surface, callback = {newWidth,newHeight->
                    binding.surface.layoutParams.apply {
                        width=newWidth
                        height=newHeight
                        binding.surface.layoutParams=this
                    }
                })
                extractor.start()

                audioExtractor.setDataSource(path)
                audioExtractor.start()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                surfaceHolder = holder
                Log.d(TAG,"width:${width},height:${height}")
                Log.d(TAG,"surfaceChanged")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d(TAG,"surfaceDestroyed")
            }
        })
    }

    private fun initDecoderHelper() {
        for (mediaCodeInfo in Utils.hwDecoderList) {
            for (type in mediaCodeInfo.supportedTypes) {
                if (type == MediaFormat.MIMETYPE_VIDEO_AVC) {
                    Log.d(TAG, "选择解码器:${mediaCodeInfo.name}")
                    decoderHelper.init(mediaCodeInfo)
                    return
                }
            }
        }

    }
}