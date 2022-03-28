package com.example.monitoringbatuk.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anand.brose.graphviewlibrary.WaveSample
import com.example.monitoringbatuk.MainActivity
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityRecordBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.visualizer.amplitude.dp
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
class RecordActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 200
    }

    private lateinit var binding: ActivityRecordBinding

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private var timer: Timer? = null
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    var handler = Handler()


    val pointList: MutableList<WaveSample> = ArrayList()
    private var scale = 8


    val listPoint = arrayListOf<Float>()


    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        databaseReference = Firebase.database.reference
        firebaseAuth = FirebaseAuth.getInstance()

//        binding.apply {
//            startRecording.setOnClickListener {
//                startRecording()
//            }
//            stopRecording.setOnClickListener {
//                stopRecording()
//            }
//        }


        handler.postDelayed({
            //    startRecording()
        }, 100L)


        binding.audioRecordView.apply {
            chunkRoundedCorners = true
            chunkAlignTo = com.visualizer.amplitude.AudioRecordView.AlignTo.CENTER
            chunkMaxHeight = 300.dp()
            chunkMinHeight = 2.dp()
            chunkWidth = 0.001.toFloat().toInt().dp()
            chunkSpace = 1.dp()
        }


        // ============================= Behavior ================================
//        val graphView = findViewById<GraphView>(R.id.graphView)
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

//        val zoomOut = findViewById<Button>(R.id.zoomOu)
//        zoomOut.setOnClickListener {
//            scale -= 1
//            if (scale < 2) {
//                scale = 2
//            }

//            graphView?.setWaveLengthPX(scale)
//            if (!recorder?.isRecording!!) {
//                graphView?.showFullGraph(samples)
//            }
//        }


        getStateFromFirebase()

    }


    private fun startRecording() {
        if (!permissionsIsGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE)
            return
        }

//        binding.startRecording.isEnabled = false
//        binding.stopRecording.isEnabled = true

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

            try {
                prepare()
                start()

            } catch (e: Exception) {
                e.printStackTrace()
            }


            //setOutputFile("/dev/null")

        }



        startDrawing()

    }


    private fun stopRecording() {
//        binding.startRecording.isEnabled = true
//        binding.stopRecording.isEnabled = false
        //stopping recorder
        recorder?.apply {
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }
            stop()
            release()
        }

        stopDrawing()
    }


    private fun startDrawing() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    val currentMaxAmplitude = recorder?.maxAmplitude
                    binding.audioRecordView.update(currentMaxAmplitude ?: 0) //redraw view

//
//                    val startTime = System.currentTimeMillis()
//                    val date = Date(startTime)
//                    val format = SimpleDateFormat("HH:mm")
//                    val time = format.format(date)

//                    val graphView = findViewById<GraphView>(R.id.graphView)
//                    graphView.maxAmplitude = 48000
//                    graphView.setMasterList(pointList)
//                    graphView.startPlotting()
                    //pointList.add(WaveSample(1000L, currentMaxAmplitude ?: 0))


//                    runOnUiThread {
//                        listPoint.add(recorder?.maxAmplitude ?: 0)
//                        val record = ArrayList<Entry>()
//                        val mutableData = mutableListOf<Int>()
//
//
//                        for (j in listPoint.indices) {
//                            mutableData.add(listPoint[j])
//
//                        }
//
//                        for ((x, y) in mutableData.indices.withIndex()) {
//                            record.add(Entry(x.toFloat(), mutableData[y].toFloat()))
//                        }
//
//                        recordMonitoring(record)
//
//                    }


                    Log.d("audio", currentMaxAmplitude.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
        }, 0, 1)


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


    override fun onBackPressed() {
        timer?.cancel()
        super.onBackPressed()
//        val graphView = findViewById<GraphView>(R.id.graphView)
//        graphView?.stopPlotting()

    }

    override fun onDestroy() {
        timer?.cancel()
        stopRecording()
        super.onDestroy()

    }

    // ==============================================

    private fun getStateFromFirebase() {
        val uid = firebaseAuth.uid
        val reference =
            databaseReference.child("UserData").child("$uid").child("batuk").child("status")

        Log.d("uid", uid.toString())
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("snap", snapshot.value.toString())
                if (snapshot.value == "1") {
                    getPersentaseBatuk()
                    startRecording()
                } else {
                    stopRecording()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.message)
            }

        })
    }


    private fun getPersentaseBatuk() {
        val uid = firebaseAuth.uid
        val reference =
            databaseReference.child("UserData").child("$uid").child("batuk").child("persentase")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                Log.d("snap", snapshot.value.toString())
                listPoint.add(snapshot.value.toString().toFloat())

                val record = ArrayList<Entry>()
                val mutableData = mutableListOf<Float>()

                for (j in listPoint.indices) {
                    mutableData.add(listPoint[j])
                }

                for ((x, y) in mutableData.indices.withIndex()) {
                    record.add(Entry(x.toFloat(), mutableData[y].toFloat()))
                }

                recordMonitoring(record)
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.message)
            }

        })

    }


    private fun recordMonitoring(record: ArrayList<Entry>) {

        // Style
        val lineDataSetRecord = LineDataSet(record, "Record")
        lineDataSetRecord.setCircleColor(ContextCompat.getColor(this, R.color.teal_700))
        lineDataSetRecord.color = ContextCompat.getColor(this, R.color.teal_700)
        lineDataSetRecord.lineWidth = 1.5F
        lineDataSetRecord.setDrawCircles(false)
        lineDataSetRecord.setDrawFilled(true)
        lineDataSetRecord.fillDrawable = ContextCompat.getDrawable(this, R.drawable.gradient)
        lineDataSetRecord.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSetRecord.valueTextSize = 10F
        lineDataSetRecord.valueTextColor = Color.BLACK
        lineDataSetRecord.circleHoleColor = ContextCompat.getColor(this, R.color.teal_700)

        // Behavior
        val lineChart = binding.chart
        lineChart.setNoDataTextColor(Color.BLACK)
        lineChart.setDrawBorders(true)
        lineChart.isScaleYEnabled = false
        lineChart.isDoubleTapToZoomEnabled = false
        lineChart.description.text = ""
        lineChart.data = LineData(lineDataSetRecord)
        lineChart.animateXY(100, 10)
        lineChart.xAxis.valueFormatter = XAxisFormatter()
        lineChart.axisRight.isEnabled = false


    }


}