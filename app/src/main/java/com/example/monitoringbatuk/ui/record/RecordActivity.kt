package com.example.monitoringbatuk.ui.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
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
        const val OUTPUT_DIRECTORY = "VoiceRecorder"
        const val OUTPUT_FILENAME = "recorder.mp3"
    }

    private lateinit var binding: ActivityRecordBinding
    private var nameUser = ""
    private var count = 0

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

    // private val chartRoomDatabase by lazy { ChartRoomDatabase.getDatabase(this).chartDao() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)


//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == Activity.RESULT_OK) {
//                    // Get the new note from the AddNoteActivity
//                    val dataAdded = 100
//                    val noteText = result.data?.getStringExtra("note_text")
//                    // Add the new note at the top of the list
//                    val newNote = Chart(dataAdded)
//
//                    lifecycleScope.launch {
//                        chartRoomDatabase.insert(newNote)
//                    }
//                }
//
//            }
//
//
//        binding.textView14.setOnClickListener {
//            lifecycleScope.launch {
//                Log.d("===============", chartRoomDatabase.getAllChart().data.toString())
//            }
//        }


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
        //  Log.d("suara", recorder?.setOutputFile( file.absoluteFile.toString() + "/" + OUTPUT_FILENAME).toString())
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
        super.onBackPressed()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }


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
                binding.tvPersentase.text = snapshot.value.toString()

                if (snapshot.value.toString().toFloat() > 50.0) {
                    count++
                }

                //             Log.d("+++++++++", snapshot.value.toString())
//                Log.d("snap", snapshot.value.toString())
//                val map = snapshot.value as Map<*, *>?
//                for (data in map?.values!!) {
//                    if (data.toString().toFloat() > 50.0) {
//                        listPoint.add(data.toString().toFloat())
//                        count++
//                        binding.tvPersentase.text = data.toString()
//
//                    }
//                }
//
//                Log.d("uuuuuuuuuuu", map.values.toString())

//                for (data in map.values){
//                    val point = data.toString().toFloat()
//                                        sendToFirebaseChart(point)
//                }

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


//    private fun sendToFirebaseChart(data:Float){
//        databaseReference.child("UserData")
//            .child(firebaseAuth.uid.toString())
//            .child("chart")
//            .child("point")
//            .setValue(data)
//
//
//    }


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
            "removeId" to ""
        )

        db.collection("history")
            .add(history)
            .addOnCompleteListener { result ->
                Log.d("dataCollection", result.toString())
                Log.d("documentId", result.toString())

            }



//        val history = hashMapOf(
//            "data" to mutableListOf(
//                mapOf(
//                "batuk" to count.toString(),
//                "nama" to nameUser,
//                "tanggal" to date,
//                "waktu" to time,
//                "removeId" to ""
//                )
//            )
//        )

//        val history = mapOf(
//            "data" to listOf(
//                History(
//                    count.toString(),
//                    nameUser,
//                    date,
//                    time,
//                    "tes"
//                )
//            )
//        )
//
//
//        val docData: MutableMap<String, Any> = HashMap()
//
//        docData["listExample"] = arrayOf(mapOf(
//            "batuk" to count.toString(),
//            "nama" to nameUser,
//            "tanggal" to date,
//            "waktu" to time,
//            "removeId" to "tes"
//        ))
//
//
//        db.collection("history").document().collection("list")
//            .add(history)
//            .addOnCompleteListener { result ->
//                Log.d("dataCollection", result.toString())
//                Log.d("documentId", result.toString())
//
//            }
//
//        class Product(
//            val satu: String,
//            val dua: String,
//            val tiga: Int,
//        )
//
//        class Obj(
//            val satu: String,
//            val dua: String,
//            val data: List<Product>,
//        )
//
//        val list = ArrayList<Product>()
//        list.add(Product("u1", "1", 1))
//        list.add(Product("u2", "2", 1))
//        list.add(Product("u3", "3", 1))
//
//        val testObject = Obj("Talha", "Kosen", list)
//        FirebaseFirestore.getInstance().collection("Test").document("a")
//            .set(testObject, SetOptions.merge())
//            .addOnCompleteListener {
//
//            }


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