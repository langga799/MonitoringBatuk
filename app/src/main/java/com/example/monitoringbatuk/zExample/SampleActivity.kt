package com.example.monitoringbatuk.zExample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import com.anand.brose.graphviewlibrary.GraphView
import com.anand.brose.graphviewlibrary.WaveSample
//
//
class SampleActivity : AppCompatActivity(){
//    private var scale = 8
//    private var graphView: GraphView? = null
//    private var recorder: VoiceRecorder? = null
//    private var samples: List<WaveSample>? = null
//    @SuppressLint("CutPasteId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_sample)
//
//
//        graphView = findViewById<GraphView>(R.id.graphView)
//        graphView?.setGraphColor(Color.rgb(255, 255, 255))
//        graphView?.setCanvasColor(Color.rgb(20, 20, 20))
//        graphView?.setTimeColor(Color.rgb(255, 255, 255))
//
//
//        recorder = VoiceRecorder.getInstance()
//        if (recorder?.isRecording == true) {
//            (findViewById<Button>(R.id.control)).text = resources.getString(R.string.stop)
//            recorder!!.startPlotting(graphView)
//        }
//        if (savedInstanceState != null) {
//            scale = savedInstanceState.getInt(SCALE)
//            graphView?.setWaveLengthPX(scale)
//            if (!recorder?.isRecording!!) {
//                samples = recorder!!.samples as List<WaveSample>?
//                graphView?.showFullGraph(samples)
//            }
//        }
//
//
//        val zoomIn = findViewById<Button>(R.id.zoomIn)
//        zoomIn.setOnClickListener {
//            scale += 1
//            if (scale > 15) {
//                scale = 15
//            }
//            graphView?.setWaveLengthPX(scale)
//            if (!recorder?.isRecording!!) {
//                graphView?.showFullGraph(samples)
//            }
//        }
//
//        val zoomOut = findViewById<Button>(R.id.zoomOu)
//        zoomOut.setOnClickListener {
//            scale -= 1
//            if (scale < 2) {
//                scale = 2
//            }
//
//            graphView?.setWaveLengthPX(scale)
//            if (!recorder?.isRecording!!) {
//                graphView?.showFullGraph(samples)
//            }
//        }
//
//        val controlClick = findViewById<Button>(R.id.control)
//        controlClick.setOnClickListener {
//            if (recorder?.isRecording == true) {
//                (findViewById<Button>(R.id.control)).text = this.resources.getString(R.string.record)
//                graphView?.stopPlotting()
//                samples = recorder?.stopRecording() as List<WaveSample>?
//                graphView?.showFullGraph(samples)
//            } else if (checkRecordPermission() && checkStoragePermission()) {
//                graphView?.reset()
//                val filepath = Environment.getExternalStorageDirectory().path
//                val file = File(filepath, OUTPUT_DIRECTORY)
//                if (!file.exists()) {
//                    file.mkdirs()
//                }
//                recorder?.outputFilePath = file.absoluteFile.toString() + "/" + OUTPUT_FILENAME
//                recorder?.startRecording()
//                recorder?.startPlotting(graphView)
//                (findViewById<Button>(R.id.control)).text = this.resources.getString(R.string.stop)
//            } else {
//                requestPermissions()
//            }
//        }
//
//    }
//
//    @SuppressLint("MissingSuperCall")
//    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putInt(SCALE, scale)
//        super.onSaveInstanceState(outState)
//
//    }
//
//
//
//    override fun onBackPressed() {
//        super.onBackPressed()
//        graphView?.stopPlotting()
//        if (recorder?.isRecording == true) {
//            recorder?.stopRecording()
//        }
//        if (recorder != null) {
//            recorder?.release()
//        }
//    }
//
//
//    private fun requestPermissions() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.RECORD_AUDIO)
//        ) {
//
//            ActivityCompat.requestPermissions(this,
//                arrayOf(Manifest.permission.RECORD_AUDIO,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                MY_PERMISSIONS_REQUEST_CODE)
//        } else {
//            ActivityCompat.requestPermissions(this,
//                arrayOf(Manifest.permission.RECORD_AUDIO,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                MY_PERMISSIONS_REQUEST_CODE)
//
//        }
//    }
//
//    private fun checkRecordPermission(): Boolean {
//        return (ContextCompat.checkSelfPermission(this,
//            Manifest.permission.RECORD_AUDIO)
//                === PackageManager.PERMISSION_GRANTED)
//    }
//
//    private fun checkStoragePermission(): Boolean {
//        return (ContextCompat.checkSelfPermission(this,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                === PackageManager.PERMISSION_GRANTED)
//    }
//
//    companion object {
//        const val SCALE = "scale"
//        const val OUTPUT_DIRECTORY = "VoiceRecorder"
//        const val OUTPUT_FILENAME = "recorder.mp3"
//        private const val MY_PERMISSIONS_REQUEST_CODE = 0
//        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
//    }
//
//
}