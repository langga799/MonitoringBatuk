package com.example.monitoringbatuk.ui.recordimport com.github.mikephil.charting.components.AxisBaseimport com.github.mikephil.charting.formatter.ValueFormatterimport java.text.SimpleDateFormatimport java.util.*class XAxisFormatter: ValueFormatter() {    override fun getAxisLabel(value: Float, axis: AxisBase?): String {        val dateFormat = SimpleDateFormat("hh:mm", Locale.getDefault())        return dateFormat.format(Date())    }}