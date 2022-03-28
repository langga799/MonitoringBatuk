package com.example.monitoringbatuk.ui.record

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.monitoringbatuk.R


open class WaveFormView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6F
    private var w = 9F

    init {
        paint.color = ContextCompat.getColor(context, R.color.purple_200)
    }

    fun addAmplitude(amp:Float){
        amplitudes.add(amp)

        var left = 0F
        var top = 0F
        var right = left + w
        var bottom = amp

        spikes.add(RectF(left, top, right, bottom))

        invalidate()

    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        spikes.forEach {
            canvas?.drawRoundRect(it, radius, radius, paint)
        }
    }
}