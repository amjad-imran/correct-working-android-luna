package com.noisefit.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.content.ContextCompat
import com.noisefit.R
import com.noisefit_commans.data.model.CountCardData

import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.custom.ToolTipEntry

class SleepGraphViewSmall(var mContext: Context) : View(
    mContext
) {
    private var isDisable = false
    var mPaint: Paint
    var mPaint2: Paint
    var outerPaint: Paint
    lateinit var remPaint: Paint
    lateinit var deepPaint: Paint
    lateinit var lightPaint: Paint
    lateinit var awakePaint: Paint
    private var mTextPaint: Paint
    var toolTipPaint: Paint = Paint()
    private var toolTipTextPaint: Paint
    var countCardData: CountCardData? = null
    var sleepArray: ArrayList<SleepData.SleepDataBreakup>? = ArrayList()
    var tooltipEntryArray: ArrayList<ToolTipEntry>? = ArrayList()
    var toolEntry: ToolTipEntry? = null

    var endPadding = 0.0f

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        tooltipEntryArray = ArrayList()
//        canvas.drawLine(
//            0f,
//            pxFromDp(mContext, 6f),
//            width.toFloat() - endPadding,
//            pxFromDp(mContext, 6f),
//            mPaint
//        )
//        canvas.drawLine(
//            0f,
//            pxFromDp(mContext, 18f),
//            width.toFloat() - endPadding,
//            pxFromDp(mContext, 18f),
//            mPaint
//        )
//        canvas.drawLine(
//            0f,
//            pxFromDp(mContext, 30f),
//            width.toFloat() - endPadding,
//            pxFromDp(mContext, 30f),
//            mPaint
//        )
//
//        canvas.drawLine(
//            0f,
//            pxFromDp(mContext, 42f),
//            width.toFloat() - endPadding,
//            pxFromDp(mContext, 42f),
//            mPaint
//        )
//        canvas.drawLine(
//            0f,
//            pxFromDp(mContext, 54f),
//            width.toFloat() - endPadding,
//            pxFromDp(mContext, 54f),
//            mPaint
//        )
        if (sleepArray != null && sleepArray!!.size > 0) {

            var totalDuration = 0
            for (i in sleepArray!!.indices) {
                totalDuration += sleepArray!![i].duration
            }
            if (totalDuration != 0) {
                val eachMinutesWidth = (width.toFloat() - endPadding) / totalDuration

                var start = 0f
                var end: Float
                var top = 0f
                var bottom = 0f
                for (i in sleepArray!!.indices) {
                    var paint: Paint? = null
                    val rowData = sleepArray!![i]
                    end = start + eachMinutesWidth * rowData.duration
                    if (rowData.sleepType == "deep") {
                        paint = deepPaint
                        top = pxFromDp(mContext, 44f)
                        bottom = pxFromDp(mContext, 52f)
                    }
                    if (rowData.sleepType == "light") {
                        paint = lightPaint
                        top = pxFromDp(mContext, 32f)
                        bottom = pxFromDp(mContext, 40f)
                    }
                    if (rowData.sleepType == "rem") {
                        paint = remPaint
                        top = pxFromDp(mContext, 20f)
                        bottom = pxFromDp(mContext, 28f)
                    }
                    if (rowData.sleepType == "awake") {
                        paint = awakePaint
                        top = pxFromDp(mContext, 8f)
                        bottom = pxFromDp(mContext, 16f)
                    }
                    if (paint != null) {
                        val rectF = RectF(start, top, end, bottom)
                        canvas.drawRoundRect(
                            rectF,
                            pxFromDp(mContext, 1.5f),
                            pxFromDp(mContext, 1.5f),
                            paint
                        )
                    }
                    tooltipEntryArray!!.add(
                        ToolTipEntry(
                            start,
                            end,
                            top,
                            bottom,
                            rowData.duration,
                            rowData.sleepType
                        )
                    )
                    start = end
                }
            }
        }
    }

    fun setData(sleepArray: ArrayList<SleepData.SleepDataBreakup>?) {
        this.sleepArray = sleepArray
        toolEntry = null
    }

    fun setData(countCardData: CountCardData?) {
        this.countCardData = countCardData
    }

    companion object {
        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }
    }


    init {
        toolTipPaint.color = ContextCompat.getColor(mContext, R.color.white)
        toolTipPaint.style = Paint.Style.FILL
        toolTipTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        toolTipTextPaint.color = ContextCompat.getColor(mContext, R.color.blood_oxygen_color)
        toolTipTextPaint.textSize = pxFromDp(mContext, 10f)
        toolTipTextPaint.textAlign = Paint.Align.CENTER
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = ContextCompat.getColor(mContext, R.color.white)
        mPaint.strokeWidth = pxFromDp(mContext, 0.5f)
        mPaint2 = Paint()
        mPaint2.isAntiAlias = true
        mPaint2.style = Paint.Style.STROKE
        mPaint2.color = ContextCompat.getColor(mContext, R.color.white)
        mPaint2.strokeWidth = pxFromDp(mContext, 0.5f)
        mTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = ContextCompat.getColor(mContext, R.color.text_color)
        mTextPaint.textSize = pxFromDp(mContext, 12f)
        outerPaint = Paint()
        outerPaint.style = Paint.Style.FILL
        outerPaint.color = Color.TRANSPARENT

    }

    fun init() {

        val shaderDeep: Shader = LinearGradient(
            0f,
            0f,
            0f,
            pxFromDp(mContext, 10f),
            ContextCompat.getColor(mContext, R.color.deep_start),
            ContextCompat.getColor(mContext, R.color.deep_start),
            Shader.TileMode.CLAMP
        )


        deepPaint = Paint()
        deepPaint.shader = shaderDeep
        val shaderLight = LinearGradient(
            0f,
            0f,
            0f,
            pxFromDp(mContext, 10f),
            ContextCompat.getColor(mContext, R.color.light_start),
            ContextCompat.getColor(mContext, R.color.light_start),
            Shader.TileMode.CLAMP
        )

        lightPaint = Paint()
        lightPaint.shader = shaderLight
        val shaderAwake: Shader = LinearGradient(
            0f,
            0f,
            0f,
            pxFromDp(mContext, 10f),
            ContextCompat.getColor(mContext, R.color.awake_start),
            ContextCompat.getColor(mContext, R.color.awake_start),
            Shader.TileMode.CLAMP
        )

        awakePaint = Paint()
        awakePaint.shader = shaderAwake

        val shaderRem: Shader = LinearGradient(
            0f,
            0f,
            0f,
            pxFromDp(mContext, 10f),
            ContextCompat.getColor(mContext, R.color.rem_start),
            ContextCompat.getColor(mContext, R.color.rem_start),
            Shader.TileMode.CLAMP
        )


        remPaint = Paint()
        remPaint.shader = shaderRem
        //endPadding = pxFromDp(mContext, 48f)
    }
}