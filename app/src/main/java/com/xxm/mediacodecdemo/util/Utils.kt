package com.xxm.mediacodecdemo.util

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

/**
 *Time:2023/9/22
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
object Utils {
    private const val TAG = "Utils"

    var hwEncoderList = mutableListOf<MediaCodecInfo>()
        private set
    var softEncoderList = mutableListOf<MediaCodecInfo>()
        private set
    var hwDecoderList = mutableListOf<MediaCodecInfo>()
        private set
    var softDecoderList = mutableListOf<MediaCodecInfo>()
        private set


    fun getSupportMediaCodec() {
        var list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        list.codecInfos.forEach {
            if (it.isEncoder) {
                if (isSoftwareOnly(it)) {
                    softEncoderList.add(it)
                } else {
                    hwEncoderList.add(it)
                }
            } else {
                if (isSoftwareOnly(it)) {
                    softDecoderList.add(it)
                } else {
                    hwDecoderList.add(it)
                }
            }
        }

    }

    private fun isSoftwareOnly(mediaCodecInfo: MediaCodecInfo): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return mediaCodecInfo.isSoftwareOnly
        } else {
            return !mediaCodecInfo.name.contains("qcom")
        }
    }

}