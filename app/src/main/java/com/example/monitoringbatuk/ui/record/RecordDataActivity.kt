package com.example.monitoringbatuk.ui.record

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityRecordDataBinding
import com.example.monitoringbatuk.model.RawResultResponse
import com.example.monitoringbatuk.network.RetrofitBuilder
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RecordDataActivity : AppCompatActivity() {

    companion object {
       // const val API_KEY = "ei_df61ce4600bf606a9b1d2e549a3bedf99846e96d136127c681007755243b01f9"
        const val API_KEY = "ei_ab3a704d9c39eadb6bcac8bfa6afaaf1ff3a5815ecce5f7df43b48131a976892"
    }

    private lateinit var binding: ActivityRecordDataBinding
    private lateinit var lineChart: LineChart


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDataBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lineChart = binding.graphView


        getDataRecord()


    }


    private fun getDataRecord() {
        RetrofitBuilder.getApiService().getRawResult(API_KEY, "application/json")
            .enqueue(object : Callback<RawResultResponse> {
                override fun onResponse(
                    call: Call<RawResultResponse>,
                    response: Response<RawResultResponse>,
                ) {

                    if (response.isSuccessful) {
                        val dataSample = response.body()?.payload?.values!!
                        sendToFirebase(dataSample)
                        val record = ArrayList<Entry>()
                        val mutableData = mutableListOf<Int>()

                        for (i in dataSample) {
                            for (j in i.indices) {
                                mutableData.add(i[j])
                            }
                        }

                        for ((x, y) in mutableData.indices.withIndex()) {
                            record.add(Entry(x.toFloat(), mutableData[y].toFloat()))
                        }

                        recordMonitoring(record)
                    }


                }

                override fun onFailure(call: Call<RawResultResponse>, t: Throwable) {

                }

            })
    }


    private fun recordMonitoring(record: ArrayList<Entry>) {

        // Style
        val lineDataSetRecord = LineDataSet(record, "Record")
        lineDataSetRecord.setCircleColor(ContextCompat.getColor(this, R.color.teal_700))
        lineDataSetRecord.color = ContextCompat.getColor(this, R.color.teal_700)
        lineDataSetRecord.lineWidth = 0.5F
        lineDataSetRecord.setDrawCircles(false)
        lineDataSetRecord.valueTextSize = 10F
        lineDataSetRecord.valueTextColor = Color.BLACK
        lineDataSetRecord.circleHoleColor = ContextCompat.getColor(this, R.color.teal_700)
        lineDataSetRecord.mode = LineDataSet.Mode.CUBIC_BEZIER

        // Behavior
        val lineChart = binding.graphView
        lineChart.setNoDataTextColor(Color.BLACK)
        lineChart.setDrawBorders(true)
        lineChart.isScaleYEnabled = false
        lineChart.isDoubleTapToZoomEnabled = false
        lineChart.description.text = ""
        lineChart.data = LineData(lineDataSetRecord)
        lineChart.animateXY(100, 500)
        lineChart.xAxis.valueFormatter = XAxisFormatter()
        lineChart.axisRight.isEnabled = false

    }

    private fun sendToFirebase(data: ArrayList<List<Int>>){

        Log.d("sample", data.toString())

        val reference: DatabaseReference = Firebase.database.reference

        reference.child("tes").setValue(data)

    }


}