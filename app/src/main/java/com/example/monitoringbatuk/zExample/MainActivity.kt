package com.example.monitoringbatuk.zExample

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.monitoringbatuk.databinding.ActivityMainBinding
import com.visualizer.amplitude.AudioRecordView
import java.io.File
import java.util.*

open class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private var timer: Timer? = null
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.startRecording.setOnClickListener {
            startRecording()
        }
        binding.stopRecording.setOnClickListener {
            stopRecording()
        }
        setSwitchListeners()
    }

    private fun startRecording() {
        if (!permissionsIsGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 200)
            return
        }

        binding.startRecording.isEnabled = false
        binding.stopRecording.isEnabled = true
        //Creating file
        try {
            audioFile = File.createTempFile("audio", "tmp", cacheDir)
        } catch (e: java.io.IOException) {
            Log.e(MainActivity::class.simpleName, e.message ?: e.toString())
            return
        }
        //Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            setAudioSamplingRate(48000)
            setAudioEncodingBitRate(48000)
            prepare()
            start()
        }

        startDrawing()
    }

    private fun stopRecording() {
        binding.startRecording.isEnabled = true
        binding.stopRecording.isEnabled = false
        //stopping recorder
        recorder?.apply {
            stop()
            release()
        }
        stopDrawing()
    }

    private fun setSwitchListeners() {
        binding.switchAlignTo.setOnCheckedChangeListener { _, isChecked ->
            binding.audioRecordView.chunkAlignTo = if (isChecked) {
                AudioRecordView.AlignTo.CENTER
            } else {
                AudioRecordView.AlignTo.BOTTOM
            }
        }
        binding.switchRoundedCorners.setOnCheckedChangeListener { _, isChecked ->
            binding.audioRecordView.chunkRoundedCorners = isChecked
        }
        binding.switchSoftTransition.setOnCheckedChangeListener { _, isChecked ->
            binding.audioRecordView.chunkSoftTransition = isChecked
        }
        binding.switchDirection.setOnCheckedChangeListener { _, isChecked ->
            binding.audioRecordView.direction = if (isChecked) {
                AudioRecordView.Direction.RightToLeft
            } else {
                AudioRecordView.Direction.LeftToRight
            }
        }
    }

    private fun startDrawing() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                val currentMaxAmplitude = recorder?.maxAmplitude
                binding.audioRecordView.update(currentMaxAmplitude ?: 0) //redraw view

                Log.d("aau", currentMaxAmplitude.toString())

            }
        }, 0, 100)
    }

    private fun stopDrawing() {
        timer?.cancel()
        binding.audioRecordView.recreate()
    }

    private fun permissionsIsGranted(perms: Array<String>): Boolean {
        for (perm in perms) {
            val checkVal: Int = checkCallingOrSelfPermission(perm)
            if (checkVal != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        startRecording()
    }
}