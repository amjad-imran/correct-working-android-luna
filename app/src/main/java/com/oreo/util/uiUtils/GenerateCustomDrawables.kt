package com.oreo.util.uiUtils

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.core.graphics.toColorInt

object GenerateCustomDrawables {

    fun chatHistorySearchBar(
        borderWidth: Float = 3f,
        cornerRadius: Float = 100f,
        backgroundColor: Int = "#0D1113".toColorInt(),
        borderStartColor: Int = "#26FFFFFF".toColorInt(),
        borderEndColor: Int = "#00FFFFFF".toColorInt()
    ): Drawable{

        val gradientColors = intArrayOf(borderStartColor, borderEndColor)

        // Background fill paint
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }

        // Gradient border paint
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, 400f,
                gradientColors, null, Shader.TileMode.CLAMP
            )
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }

        return object : Drawable() {
            override fun draw(canvas: Canvas) {
                val halfBorder = borderWidth / 2

                // Background rect
                val fillRect = RectF(
                    0f,
                    0f,
                    bounds.width().toFloat(),
                    bounds.height().toFloat()
                )
                canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, fillPaint)

                // Border rect
                val strokeRect = RectF(
                    halfBorder,
                    halfBorder,
                    bounds.width() - halfBorder,
                    bounds.height() - halfBorder
                )
                canvas.drawRoundRect(strokeRect, cornerRadius, cornerRadius, strokePaint)
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(cf: ColorFilter?) {}
            override fun getOpacity() = PixelFormat.TRANSLUCENT
        }
    }

    enum class BgGradient{
        LINEAR, ANGULAR
    }

    enum class BorderGradient{
        LINEAR, ANGULAR
    }

}