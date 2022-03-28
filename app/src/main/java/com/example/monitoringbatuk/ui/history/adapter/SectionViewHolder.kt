package com.example.monitoringbatuk.ui.history.adapter

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import com.example.monitoringbatuk.R

class SectionViewHolder(
    context: Context
): FrameLayout(context) {

    private lateinit var textViewDate : TextView

    init {
        inflate(context, R.layout.item_history_section, this)

        findView()
    }

    private fun findView(){
        textViewDate = findViewById(R.id.textViewDate)
    }

    fun setDate(dateString: String){
        this.textViewDate.text = dateString
    }
}