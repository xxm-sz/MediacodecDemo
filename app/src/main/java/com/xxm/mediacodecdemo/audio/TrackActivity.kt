package com.xxm.mediacodecdemo.audio

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.media.*
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.xxm.mediacodecdemo.R
import com.xxm.mediacodecdemo.databinding.ActivityTrackBinding
import com.xxm.mediacodecdemo.showToast
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream


/**
 *Time:2023/10/7
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
@SuppressLint("MissingPermission")
class TrackActivity : Activity() {
    private val TAG = "TrackActivity"

    //采样率 44.1kHz
    private val sampleRate = 44100

    //声道 单声道
    private val channel = AudioFormat.CHANNEL_OUT_MONO

    //位深
    private val pcmBit = AudioFormat.ENCODING_PCM_16BIT

    //当前AudioRecord最小缓存大小
    private var bufferSize = 0


    private var job: Job? = null

    private lateinit var binding: ActivityTrackBinding

    private val list = mutableListOf<TrackData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_track)

        createAudioTrack()

        binding.rvTrack.layoutManager = LinearLayoutManager(this)
        binding.rvTrack.adapter = adapter
        adapter.setOnItemClickListener { adapter, view, position ->
            if (isPlaying) {
                stopPlay()
            } else {
                startPlay((adapter.getItem(position) as TrackData).path)
            }
        }

        externalCacheDir?.let {
            if (it.exists() && it.listFiles().isNotEmpty()) {
                it.list().forEach {
                    if (it.startsWith("audio")) {
                        list.add(TrackData(it))
                    }
                }
                adapter.submitList(list)
            }
        }
    }

    private lateinit var audioTrack: AudioTrack

    private var adapter: TrackAdapter = TrackAdapter()

    private fun createAudioTrack() {
        bufferSize = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, pcmBit
        )

        if (bufferSize < 0) {
            showToast("获取最小缓存区大小失败")
            return
        }

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(pcmBit)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channel)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()
    }

    private var isPlaying = false

    fun startPlay(path: String) {
        if (isPlaying) return
        val sourceFile = File(externalCacheDir, path)
        if (sourceFile.exists()) {
            isPlaying = true
            getDataToShow()
            job = GlobalScope.launch(Dispatchers.IO) {
                audioTrack!!.play()
                val fileInputStream = FileInputStream(sourceFile)
                val buffer = ByteArray(bufferSize)

                while (isPlaying) {
                    val size = fileInputStream.read(buffer, 0, bufferSize)
                    if (size <= 0) {
                        isPlaying = false
                        continue
                    }
                    audioTrack.write(buffer, 0, bufferSize)
                }
                audioTrack.stop()
                job?.cancel()
            }
        } else {
            showToast("当前文件不存在:${path}")
        }
    }

    fun stopPlay() {
        isPlaying = false
        visualizer.enabled=false
        visualizer.release()
        //   job?.cancel()
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


    override fun onDestroy() {
        super.onDestroy()
        stopPlay()
    }

    private lateinit var visualizer: Visualizer
    private var mCount = 60

    private fun getDataToShow() {
        visualizer = Visualizer(audioTrack.audioSessionId)
        visualizer.captureSize = Visualizer.getCaptureSizeRange()[1]
        visualizer.setDataCaptureListener(object : OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                println("++++++++++++++")
            }

            override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                Log.d(TAG,"采样率：$samplingRate")
                if (fft==null){
                    Log.d(TAG,"fft data is null")
                    visualizer?.enabled=false
                    return
                }

                val n = fft!!.size
                val magnitudes = FloatArray(n / 2 + 1)
                val phases = FloatArray(n / 2 + 1)
                magnitudes[0] = Math.abs(fft[0].toInt()).toFloat() // DC

                magnitudes[n / 2] = Math.abs(fft[1].toInt()).toFloat() // Nyquist

                phases[0] = 0.also { phases[n / 2] = it.toFloat() }.toFloat()
                Log.d(TAG, "data length:${n/2}")
                for (k in 1 until n / 2) {
                    val i = k * 2
                    //取频率点实部与虚部的模
                    magnitudes[k] = Math.hypot(fft!![i].toDouble(), fft!![i + 1].toDouble()).toFloat()
                }
                runOnUiThread {
                    binding.visualizeView.onFftDataCapture(magnitudes)
                }
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true)
        visualizer.enabled = true
    }

}