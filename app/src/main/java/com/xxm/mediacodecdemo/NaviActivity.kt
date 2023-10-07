package com.xxm.mediacodecdemo

import android.app.Activity
import android.content.Intent
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
import com.xxm.mediacodecdemo.audio.AudioActivity
import com.xxm.mediacodecdemo.audio.TrackActivity
import com.xxm.mediacodecdemo.databinding.ActivityMainBinding
import com.xxm.mediacodecdemo.databinding.ActivityNaviBinding
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
class NaviActivity : Activity() {
    private val TAG = "NaviActivity"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //    Utils.getSupportMediaCodec()
        val extractor = Extractor()
        val audioExtractor = AudioExtractor()
        val binding = DataBindingUtil.setContentView<ActivityNaviBinding>(this, R.layout.activity_navi)
        binding.btnRecordVoice.setOnClickListener {
            startActivity(Intent(this,AudioActivity::class.java))
        }
        binding.btnPlayVoice.setOnClickListener {
            startActivity(Intent(this,TrackActivity::class.java))
        }
        binding.btnLocalPlay.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}