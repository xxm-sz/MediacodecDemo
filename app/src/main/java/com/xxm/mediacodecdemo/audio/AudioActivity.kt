package com.xxm.mediacodecdemo.audio

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.xxm.mediacodecdemo.R
import com.xxm.mediacodecdemo.databinding.ActivityAudioBinding
import com.xxm.mediacodecdemo.showToast
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

/**
 *Time:2023/10/7
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
@SuppressLint("MissingPermission")
class AudioActivity : Activity() {
    private val TAG = "AudioActivity"

    //采样率 44.1kHz
    private val sampleRate = 44100

    //声道 单声道
    private val channel = AudioFormat.CHANNEL_IN_MONO

    //位深
    private val pcmBit = AudioFormat.ENCODING_PCM_16BIT

    //当前AudioRecord最小缓存大小
    private var bufferSize = 0

    private lateinit var audioRecord: AudioRecord

    private var job: Job? = null

    private lateinit var binding: ActivityAudioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio)

        if (checkPermissions()) {
            initAudioRecord()
        }



        binding.btnStart.setOnClickListener {
            startRecording()
        }

        binding.btnStop.setOnClickListener {
            stopRecording()
        }
    }

    private fun initAudioRecord() {
        bufferSize = AudioRecord.getMinBufferSize(
            sampleRate, channel, pcmBit
        )
        if (bufferSize<0){
            showToast("获取最小缓存区大小失败")
            return
        }

        //需要RECORD_AUDIO权限
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, //音频来源，麦克风
            sampleRate,
            channel,
            pcmBit,
            bufferSize
        )

    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "需要权限", Toast.LENGTH_LONG).show()
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return false
        }

        return true

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "需要权限", Toast.LENGTH_LONG).show()
//            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
//            return
//        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkPermissions()
    }

    private var isRecording = false
    private fun startRecording() {
        if (isRecording) return
        isRecording = true
        Log.d(TAG, "job status:${job?.isCancelled}")
        job?.cancel()
        audioRecord.startRecording()
        job = GlobalScope.launch(Dispatchers.IO) {
        //getDataToShow()
            val buffer = ByteArray(bufferSize)
            val file = application.externalCacheDir
            val saveFile = File(file, "audio_${System.currentTimeMillis()}.pcm")
            Log.d(TAG, "${saveFile.absolutePath}")
            withContext(Dispatchers.Main){
                binding.tvState.text="正在录音...\n${saveFile.absolutePath}"
            }
            val ous = FileOutputStream(saveFile)
            while (isRecording) {
                Log.d(TAG, "job status:${job?.isCancelled}")
                val result = audioRecord.read(buffer, 0, bufferSize)
                ous.write(buffer)
            }
            isRecording = false
            ous.close()
            audioRecord.stop()
        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord.stop()
        binding.tvState.text="录音已停止"
        visualizer.enabled=false
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecord.stop()
        audioRecord.release()
        job?.cancel()
    }

    private lateinit var visualizer: Visualizer


}