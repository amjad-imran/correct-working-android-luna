package com.noisefit_commans.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

internal class DrawView(context: Context?) : View(context) {
    var paint: Paint = Paint()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(10f, 20f, 30f, 40f, paint)
//        canvas.drawLine(20, 10, 50, 20, paint)
    }

    init {
        paint.color = Color.WHITE
    }
}
