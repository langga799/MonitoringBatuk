package com.example.monitoringbatuk.ui.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityRecordBinding
import com.example.monitoringbatuk.ui.history.History
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.visualizer.amplitude.dp
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.O)
class RecordActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 200
    }

    private lateinit var binding: ActivityRecordBinding
    private var nameUser = ""
    private var count = 0
    private var persentase: String = "0.0"

    var handler: Handler = Handler()
    var runnable: Runnable? = null
    var delay = 10000

    private val db = Firebase.firestore

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private var timer: Timer? = null
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    val listPoint = arrayListOf<Float>()

    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        databaseReference = Firebase.database.reference
        firebaseAuth = FirebaseAuth.getInstance()


        binding.audioRecordView.apply {
            chunkRoundedCorners = true
            chunkAlignTo = com.visualizer.amplitude.AudioRecordView.AlignTo.CENTER
            chunkMaxHeight = 300.dp()
            chunkMinHeight = 2.dp()
            chunkWidth = 0.001.toFloat().toInt().dp()
            chunkSpace = 1.dp()
        }

        binding.clearChart.setOnClickListener {
            binding.chart.clear()
        }

        getStateFromFirebase()
        getNameUser()
    }


    private fun startRecording() {
        if (!permissionsIsGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE)
            return
        }

        try {
            audioFile = File.createTempFile("audio", "tmp.mp3", cacheDir)
        } catch (e: java.io.IOException) {
            Log.e(RecordActivity::class.simpleName, e.message ?: e.toString())
            return
        }


        //Creating MediaRecorder
        recorder = MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(48000)
            setAudioEncodingBitRate(48000)
            setOutputFile(audioFile?.absolutePath)


            try {
                prepare()
                start()

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        startDrawing()

        Log.d("suara", audioFile.toString())

    }


    private fun stopRecording() {
        //stopping recorder
        recorder?.apply {
            stop()
            release()
        }
        Log.d("suara", audioFile.toString())
        stopDrawing()
    }


    private fun startDrawing() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                try {
                    val currentMaxAmplitude = recorder?.maxAmplitude
                    if (currentMaxAmplitude ?: 0 > 1000) {
                        binding.audioRecordView.update(currentMaxAmplitude ?: 0) //redraw view

                        binding.tvFrequency.text = currentMaxAmplitude.toString() + " Hz"

                        val db = 20 * kotlin.math.log10(currentMaxAmplitude?.toDouble()!! / 32767.0)

                        binding.tvDecibel.text = db.toString()
                    }


                    Log.d("audio", currentMaxAmplitude.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
        }, 1000, 1)
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
        stopRecordState()
        handler.removeCallbacks(runnable!!)
        super.onBackPressed()
    }

    override fun onDestroy() {
        timer?.cancel()
        handler.removeCallbacks(runnable!!)
        super.onDestroy()
    }


    // ====================================================================================
    // Fungsi-fungsi Firebase
    // ====================================================================================

    private fun getStateFromFirebase() {
        val uid = firebaseAuth.uid
        val reference =
            databaseReference
                .child("UserData")
                .child("$uid")
                .child("nilaibatuk")
                .child("status")

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
            databaseReference
                .child("UserData")
                .child("$uid")
                .child("nilaibatuk")
                .child("databatuk")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                listPoint.add(snapshot.value.toString().toFloat())
                (snapshot.value.toString() + "%").also { binding.tvPersentase.text = it }

                persentase = snapshot.value.toString()
                Log.d("persentase" , persentase)


                sendDataPersentaseToFirestore(mapOf("${snapshot.key}" to "${snapshot.value}"))

                if (snapshot.value.toString().toFloat() > 50.0) {
                    count++
                }


                val record = ArrayList<Entry>()
                val mutableData = mutableListOf<Float>()

                for (j in listPoint.indices) {
                    mutableData.add(listPoint[j])
                }

                for ((x, y) in mutableData.indices.withIndex()) {
                    record.add(Entry(x.toFloat(), mutableData[y]))
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


    private fun sendDataPersentaseToFirestore(persentase:Map<String, String>){
        db.collection("persentase")
            .add(persentase)
            .addOnSuccessListener { result ->
                result.toString()
            }

    }

    private fun stopRecordState() {
        val uid = firebaseAuth.uid
        val reference =
            databaseReference
                .child("UserData")
                .child("$uid")
                .child("nilaibatuk")
                .child("status")

        reference.setValue("0")

        sendToFirestore()
    }


    private fun sendToFirestore() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = current.format(formatter)

        val formatTime = DateTimeFormatter.ofPattern("HH:mm:ss")
        val time = current.format(formatTime)

        Log.d("countt", count.toString())

        val history = hashMapOf(
            "batuk" to count.toString(),
            "nama" to nameUser,
            "tanggal" to date,
            "waktu" to time,
            "persentase" to persentase
        )

        db.collection("history")
            .add(history)
            .addOnCompleteListener { result ->
                Log.d("dataCollection", result.toString())
            }


    }

    override fun onResume() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())

            // Kirim ke firestore setiap 10 detik
            sendToFirestore()

        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }


    private fun getNameUser() {
        val uid = firebaseAuth.uid
        val reference = databaseReference.child("UserData").child(uid.toString()).child("fullName")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    nameUser = snapshot.value.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


}