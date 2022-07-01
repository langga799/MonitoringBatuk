package com.example.monitoringbatuk.ui.recordVoice

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityRecordVoiceBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.S)
class RecordVoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordVoiceBinding

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private var timer: Timer? = null
    var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    var fileName = "default.wav"
    var dirPath = ""

    private lateinit var textInputFile: TextInputEditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordVoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textInputFile = findViewById(R.id.inputTextFile)

        binding.btnStopRecord.isEnabled = false
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottomSheet)
        bottomSheet.visibility = View.GONE


        binding.btnStartRecord.setOnClickListener {
            startRecordVoice()
        }

        binding.btnStopRecord.setOnClickListener {
            stopRecordVoive()
        }


        binding.button.setOnClickListener {
            fileName = binding.input.text.toString()
            Log.d("newfff", fileName)
        }

        val setText = findViewById<Button>(R.id.btnSetText)
        setText.setOnClickListener {

            fileName = textInputFile.text.toString()
        }

        saveWithName()

    }


    @SuppressLint("SimpleDateFormat")
    private fun startRecordVoice() {
        if (!permissionIsGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 200)
            return
        }

        binding.apply {
            btnStartRecord.isEnabled = false
            btnStopRecord.isEnabled = true
        }



        recorder = MediaRecorder()

        //Creating file
//        try {
//            audioFile = File.createTempFile(fileName, ".wav", externalCacheDir)
//
//        } catch (e: IOException) {
//            Log.d(RecordVoiceActivity::class.simpleName, e.message ?: e.toString())
//            return
//        }



        dirPath = "${externalCacheDir?.absolutePath}/"
//        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
//        val date = simpleDateFormat.format(Date())
//        fileName = "audio_record_$date"



        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(dirPath+fileName)
            setAudioSamplingRate(48000)
            setAudioEncodingBitRate(48000)

            try {
                prepare()
                start()
                Log.d("filnameeee", fileName)
            } catch (e: Exception) {
                Log.d("MediaRecorder", e.printStackTrace().toString())
            }
        }

        startDrawing()

    }


    private fun startDrawing() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    val currentMaxAmplitude = recorder?.maxAmplitude
                    binding.recordVoice.update(currentMaxAmplitude ?: 0) //redraw view
                } catch (e: Exception) {
                    Log.d("StartDrawing", e.printStackTrace().toString())
                }

            }
        }, 0, 100)
    }


    private fun stopRecordVoive() {
        binding.apply {
            btnStartRecord.isEnabled = true
            btnStopRecord.isEnabled = false
        }

        recorder?.apply {
            stop()
            release()

        }
     //   saveWithName()

        //  showBottomSheet()



        stopDrawing()
    }


    @SuppressLint("InflateParams")
    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null, false)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btn_cancel_save)
        val btnOk = view.findViewById<MaterialButton>(R.id.btn_ok)

        Log.d("filnameeee", "show bottom sheet ditampilkan")


        btnOk.setOnClickListener {
            //  textInputFile.setText(fileName)
            Log.d("filnameeee", fileName)
            Log.d("filnameeee", "button OK dijalankan")

            Handler(mainLooper).postDelayed({
                Log.d("filnameeee", "save dijalankan")
                //  save()
            }, 3000L)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }


    private fun save() {

        Log.d("lasssss", fileName)
        recorder?.apply {
            stop()
            release()
        }
        //   textInputFile.setText(fileName)
//        val newFilename = textInputFile.text.toString()
//        Log.d("filnameeee", fileName)
//        if (newFilename != fileName){
//            var newFile = File("$dirPath$newFilename.mp3")
//            File("$dirPath$fileName.mp3").renameTo(newFile)
//            recorder?.apply {
//                stop()
//                release()
//            }
//        }
    }


    fun stopDrawing() {
        timer?.cancel()
        binding.recordVoice.recreate()
    }


    private fun saveWithName() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Simpan Audio")

        val input = EditText(this)
        input.hint = "Ketikkan nama file"
        input.inputType = InputType.TYPE_CLASS_TEXT


        builder.setView(input)

        val view = LayoutInflater.from(this).inflate(R.layout.sheet_bottom, null, false)
        val btn = view.findViewById<MaterialButton>(R.id.btn_oke)
        builder.setView(view)

        val edt = view.findViewById<TextInputEditText>(R.id.inputNew)
        btn.setOnClickListener {
            fileName = edt.text.toString() + ".wav"
            Log.d("renamed", "neutral bytton $fileName")
        }



        builder.setNeutralButton("CANCEL") { _, _ ->


        }


        builder.setPositiveButton("OK") { _, _ ->
            Toast.makeText(this, edt.text.toString(), Toast.LENGTH_SHORT).show()
            //  fileName = input.text.toString()
            Log.d("simpan", "smpan $dirPath $fileName")


            Log.d("filnameeee", fileName)

         //   fileName += ".wav"
           // setOutputFile(  dirPath+fileName)
//            recorder?.apply {
//
//                stop()
//                release()
//                Log.d("simpanddddd", "smpan=$dirPath$fileName")
//
//            }
           // recorder?.setOutputFile(newFile)


        }

        builder.setNegativeButton("SET") { dialog, g ->
//            fileName = input.text.toString()
            Log.d("renamed", "neutral bytton $fileName")

        }
        builder.setCancelable(false)
        builder.show()


    }


    private fun permissionIsGranted(permission: Array<String>): Boolean {
        for (permiss in permission) {
            val check: Int = checkCallingOrSelfPermission(permiss)
            if (check != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (result in grantResults) {
            if (result in grantResults) {
                return
            }
        }
    }


}