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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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


    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )


    private var timer: Timer? = null
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null


    val listPoint = arrayListOf<Float>()


    private val db = Firebase.firestore
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = Firebase.database.reference
        firebaseAuth = FirebaseAuth.getInstance()

        // Visualisasi bentuk audio grafik
        binding.audioRecordView.apply {
            chunkRoundedCorners = true
            chunkAlignTo = com.visualizer.amplitude.AudioRecordView.AlignTo.CENTER
            chunkMaxHeight = 300.dp()
            chunkMinHeight = 2.dp()
            chunkWidth = 0.001.toFloat().toInt().dp()
            chunkSpace = 1.dp()
        }

        // Untuk membersihkan area grafik
        binding.clearChart.setOnClickListener {
            binding.chart.clear()
        }

        getStateFromFirebase()
        getNameUser()
    }


    // Fungsi untuk memulai record suara
    private fun startRecording() {
        if (!permissionsIsGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE)
            return
        }

        try {
            // audioFile = externalCacheDir
            audioFile = File.createTempFile("cough_", ".wav", externalCacheDir) // work code
        } catch (e: java.io.IOException) {
            Log.e(RecordActivity::class.simpleName, e.message ?: e.toString())
            return
        }


        //Membuat MediaRecorder
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


    // Fungsi untuk memberhentikan perakaman
    private fun stopRecording() {
        //stopping recorder
        recorder?.apply {
            stop()
            release()
        }
        Log.d("suara", audioFile.toString())
        stopDrawing()
    }


    // Untuk memulai menggambar grafik aplitudo
    private fun startDrawing() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                try {
                    val currentMaxAmplitude =
                        recorder?.maxAmplitude // Untuk mendapatkan nilai frekuensi
                    if ((currentMaxAmplitude ?: 0) > 1000) {
                        binding.audioRecordView.update(currentMaxAmplitude
                            ?: 0) //menggambar kembali amplitudo

                        binding.tvFrequency.text = currentMaxAmplitude.toString() + " Hz"

                        val db =
                            20 * kotlin.math.log10(currentMaxAmplitude?.toDouble()!! / 32767.0) // Untuk mendapatkan nilai dB

                        binding.tvDecibel.text = db.toString()
                    }


                    Log.d("audio", currentMaxAmplitude.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
        }, 1000, 1)
    }

    // Memberhentikan gambar amplitude
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

    // dipanggil ketika user menekan tombol kembali
    override fun onBackPressed() {
        timer?.cancel()
        stopRecordState()
        handler.removeCallbacks(runnable!!)
        super.onBackPressed()
    }

    // dipanggil ketika activity telah dihancurkan
    override fun onDestroy() {
        timer?.cancel()
        handler.removeCallbacks(runnable!!)
        super.onDestroy()
    }

    // dipanggil ketika activity dalam keadaan resume
    override fun onResume() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())

            // Kirim ke firestore setiap 10 detik
            sendToFirestore()

        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    // dipanggil ketika activity dalam keadaan pause
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!) // untuk menghapus thread (utas) pada saat activity pause
    }


    // ====================================================================================
    // Fungsi-fungsi Firebase
    // ====================================================================================

    private fun getStateFromFirebase() {  // fungsi utuk mendapatkan status recording 0/1
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


    private fun getPersentaseBatuk() { // fungsi untuk mendapatkan nilai persentase
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
                (snapshot.value.toString() + "%").also {
                    binding.tvPersentase.text = it
                } // utuk menampilkan ke TextView

                persentase = snapshot.value.toString()
                Log.d("persentase", persentase)


                sendDataPersentaseToFirestore(mapOf("${snapshot.key}" to "${snapshot.value}"))

                if (snapshot.value.toString().toFloat() > 50.0) {
                    count++ // filterisasi dan perhitungan jumlah batuk
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


    private fun recordMonitoring(record: ArrayList<Entry>) { // fungsi untuk menampilkan data ke grafik

        // Style Grafik
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

        // Perilaku grafik
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


    // Fungsi untuk mengirim semua hasil persentase batuk ke firestore
    private fun sendDataPersentaseToFirestore(persentase: Map<String, String>) {
        db.collection("persentase")
            .add(persentase)
            .addOnSuccessListener { result ->
                result.toString()
            }
    }


    // fungsi untuk stop melakukan record
    private fun stopRecordState() {
        val uid = firebaseAuth.uid
        val reference =
            databaseReference
                .child("UserData")
                .child("$uid")
                .child("nilaibatuk")
                .child("status")

        reference.setValue("0")

    }


    private fun sendToFirestore() { // fungsi untuk mengirim hasil riwayat
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


    // fungsi untuk mendapatkan nama user
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