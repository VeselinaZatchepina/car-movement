package com.github.veselinazatchepina.carmovement

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.DisplayMetrics
import android.view.View


class Car(context: Context?) : View(context) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.apply {
            color = Color.GRAY
            canvas?.drawRect(
                getMaxDisplayDimension().first * 0.45F,
                getMaxDisplayDimension().second * 0.75F,
                getMaxDisplayDimension().first * 0.55F,
                getMaxDisplayDimension().second * 0.85F,
                this
            )
        }
    }

    private fun getMaxDisplayDimension(): Pair<Float, Float> {
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        return Pair(displayMetrics.widthPixels.toFloat(), displayMetrics.heightPixels.toFloat())
    }

}