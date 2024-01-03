package com.oreo.ui.custom

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.noisefit.luna.R


class GradientTextView : AppCompatTextView {

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup()
    }


    private fun setup() {
        val paint: Paint = paint
        val width = paint.measureText(text.toString())

        val gradient: Shader = LinearGradient(
            0f, 0f, width, textSize,
            intArrayOf(context.getColor(R.color.nap_sleep_grad_start), context.getColor(R.color.nap_sleep_grad_end)), null, Shader.TileMode.CLAMP
        )
        
        paint.shader = gradient
        invalidate()
    }
}