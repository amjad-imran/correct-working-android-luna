package com.noisefit_commans.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.noisefit_commans.R
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.models.SleepData

class SleepGraphViewNew(var mContext: Context) : View(
    mContext
) {
    private var sleepGraphInteractionListener: SleepGraphInteractionListener? = null
    private var isDisable = false
    private var mPaint: Paint
    private var mPaint2: Paint
    private var outerPaint: Paint
    private lateinit var remPaint: Paint
    private lateinit var deepPaint: Paint
    private lateinit var lightPaint: Paint
    private lateinit var awakePaint: Paint
    private var mTextPaint: Paint

    //    private var toolTipPaint: Paint = Paint()
//    private var toolTipTextPaint: Paint
    private var countCardData: CountCardData? = null
    private var sleepArray: ArrayList<SleepData.SleepDataBreakup>? = ArrayList()
    private var tooltipEntryArray: ArrayList<ToolTipEntry>? = ArrayList()
//    private var toolEntry: ToolTipEntry? = null

    var endPadding = 0.0f

    fun setInteraction(sleepGraphInteractionListener: SleepGraphInteractionListener) {
        this.sleepGraphInteractionListener = sleepGraphInteractionListener
    }

    var previousRect: RectF? = null

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        previousRect = null

        val sectionHeight = height.toFloat() / 5

        val textXPos = (width.toFloat() - endPadding + pxFromDp(mContext, 4f))
        tooltipEntryArray = ArrayList()

        if (!isDisable) {
            canvas.drawText("Awake", textXPos, sectionHeight * 1, mTextPaint)
            canvas.drawText("REM", textXPos, sectionHeight * 2, mTextPaint)
            canvas.drawText("Light", textXPos, sectionHeight * 3, mTextPaint)
            canvas.drawText("Deep", textXPos, sectionHeight * 4, mTextPaint)
        }


        for (i in 0 until 5) {
            canvas.drawLine(
                0f,
                sectionHeight * i,
                width.toFloat() - endPadding,
                sectionHeight * i,
                mPaint
            )
        }


        if (sleepArray != null && sleepArray!!.size > 0) {

            countCardData?.leftValue?.let { startTime ->
                canvas.drawText(startTime, 0f, sectionHeight * 5, mTextPaint)
            }

            countCardData?.rightValue?.let { endTime ->
                canvas.drawText(
                    endTime,
                    (width - pxFromDp(mContext, 45f) - endPadding),
                    sectionHeight * 5,
                    mTextPaint
                )
            }


            var totalDuration = 0
            for (i in sleepArray!!.indices) {
                totalDuration += sleepArray!![i].duration
            }
            if (totalDuration != 0) {
                val eachMinutesWidth = (width.toFloat() - endPadding) / totalDuration
                val lineWidth = pxFromDp(mContext, 1f)

                var start = 0f
                var end: Float
                var top = 0f
                var bottom = 0f
                val barHeight = pxFromDp(mContext, 12f)

                for (i in sleepArray!!.indices) {
                    var paint: Paint? = null
                    val rowData = sleepArray!![i]
                    end = start + eachMinutesWidth * rowData.duration
                    if (rowData.sleepType == "deep") {
                        paint = deepPaint
                        top = sectionHeight * 3 + barHeight / 2
                        bottom = sectionHeight * 4 - barHeight / 2
                    }
                    if (rowData.sleepType == "light") {
                        paint = lightPaint
                        top = sectionHeight * 2 + barHeight / 2
                        bottom = sectionHeight * 3 - barHeight / 2
                    }
                    if (rowData.sleepType == "rem") {
                        paint = remPaint
                        top = sectionHeight * 1 + barHeight / 2
                        bottom = sectionHeight * 2 - barHeight / 2
                    }
                    if (rowData.sleepType == "awake") {
                        paint = awakePaint
                        top = barHeight / 2
                        bottom = sectionHeight * 1 - barHeight / 2
                    }
                    if (paint != null) {
                        val rectF = RectF(start, top, end + lineWidth, bottom)
                        /* canvas.drawRoundRect(
                             rectF,
                             pxFromDp(mContext, 4f),
                             pxFromDp(mContext, 4f),
                             paint
                         )*/


                        val nextElement = try {
                            sleepArray!![i + 1]
                        } catch (exp: Exception) {
                            null
                        }

                        val previousElement = try {
                            sleepArray!![i - 1]
                        } catch (exp: Exception) {
                            null
                        }

                        val radius = pxFromDp(mContext, 4f)
                        var topLeftRadius = radius
                        var topRightRadius = radius
                        var bottomRightRadius = radius
                        var bottomLeftRadius = radius


                        var startColor: Int? = null
                        var endColor: Int? = null

                        if (nextElement != null) {
                            when (rowData.sleepType) {
                                "deep" -> {
                                    topRightRadius = 0f
                                }
                                "light" -> {
                                    when (nextElement.sleepType) {
                                        "deep" -> {
                                            bottomRightRadius = 0f

                                        }
                                        "rem" -> {
                                            topRightRadius = 0f
                                        }
                                        "awake" -> {
                                            topRightRadius = 0f
                                        }

                                    }
                                }
                                "rem" -> {
                                    when (nextElement.sleepType) {
                                        "deep" -> {
                                            bottomRightRadius = 0f
                                        }
                                        "light" -> {
                                            bottomRightRadius = 0f
                                        }
                                        "awake" -> {
                                            topRightRadius = 0f
                                        }

                                    }
                                }
                                "awake" -> {
                                    bottomRightRadius = 0f
                                }

                            }
                        }

                        if (previousElement != null) {
                            when (rowData.sleepType) {
                                "deep" -> {
                                    topLeftRadius = 0f
                                    startColor =
                                        mContext.getColor(R.color.deep_start)
                                    when (previousElement.sleepType) {
                                        "light" -> {
                                            endColor =
                                                mContext.getColor(R.color.light_start)
                                        }
                                        "rem" -> {
                                            endColor =
                                                mContext.getColor(R.color.rem_start)
                                        }
                                        "awake" -> {
                                            endColor =
                                                mContext.getColor(R.color.awake_start)
                                        }
                                    }
                                }
                                "light" -> {
                                    startColor =
                                        mContext.getColor(R.color.light_start)
                                    when (previousElement.sleepType) {
                                        "deep" -> {
                                            bottomLeftRadius = 0f

                                            endColor =
                                                mContext.getColor(R.color.deep_start)
                                        }
                                        "rem" -> {
                                            topLeftRadius = 0f
                                            endColor =
                                                mContext.getColor(R.color.rem_start)
                                        }
                                        "awake" -> {
                                            endColor =
                                                mContext.getColor(R.color.awake_start)
                                            topLeftRadius = 0f
                                        }

                                    }
                                }
                                "rem" -> {
                                    startColor =
                                        mContext.getColor(R.color.rem_start)
                                    when (previousElement.sleepType) {
                                        "deep" -> {
                                            bottomLeftRadius = 0f
                                            endColor =
                                                mContext.getColor(R.color.deep_start)
                                        }
                                        "light" -> {
                                            bottomLeftRadius = 0f
                                            endColor =
                                                mContext.getColor(R.color.light_start)
                                        }
                                        "awake" -> {
                                            topLeftRadius = 0f
                                            endColor =
                                                mContext.getColor(R.color.awake_start)
                                        }

                                    }
                                }
                                "awake" -> {
                                    bottomLeftRadius = 0f

                                    startColor =
                                        mContext.getColor(R.color.awake_start)
                                    when (previousElement.sleepType) {
                                        "deep" -> {
                                            endColor =
                                                mContext.getColor(R.color.deep_start)
                                        }
                                        "rem" -> {
                                            endColor =
                                                mContext.getColor(R.color.rem_start)
                                        }
                                        "light" -> {
                                            endColor =
                                                mContext.getColor(R.color.light_start)
                                        }

                                    }
                                }

                            }
                        }

                        if (isDisable) {
                            startColor = mContext.getColor(R.color.sleep_gray)
                            endColor = mContext.getColor(R.color.sleep_gray)
                        }


                        if (previousRect != null) {
                            if (previousRect!!.top > rectF.top) {

                                val lineShader: Shader = LinearGradient(
                                    previousRect!!.right,
                                    previousRect!!.top,
                                    rectF.left,
                                    rectF.bottom,
                                    endColor ?: 0,
                                    startColor ?: 0,
                                    Shader.TileMode.CLAMP
                                )

                                canvas.drawRect(
                                    previousRect!!.right,
                                    previousRect!!.top,
                                    rectF.left,
                                    rectF.bottom,
                                    Paint().apply {
                                        shader = lineShader
                                    }
                                )
                            } else {

                                val lineShader: Shader = LinearGradient(
                                    previousRect!!.right,
                                    previousRect!!.bottom,
                                    rectF.left,
                                    rectF.top,
                                    endColor ?: 0,
                                    startColor ?: 0,
                                    Shader.TileMode.CLAMP
                                )

                                canvas.drawRect(
                                    previousRect!!.right,
                                    previousRect!!.bottom,
                                    rectF.left,
                                    rectF.top,
                                    Paint().apply {
                                        shader = lineShader
                                    }
                                )
                            }
                        }


                        val corners = floatArrayOf(
                            topLeftRadius, topLeftRadius,   // Top left radius in px
                            topRightRadius, topRightRadius,   // Top right radius in px
                            bottomRightRadius, bottomRightRadius,     // Bottom right radius in px
                            bottomLeftRadius, bottomLeftRadius      // Bottom left radius in px
                        )

                        val path = Path()
                        path.addRoundRect(rectF, corners, Path.Direction.CW)
                        canvas.drawPath(path, paint)
                        previousRect = rectF

                    }
                    val range = sleepArray!![i].startTime + " - " + sleepArray!![i].endTime

                    tooltipEntryArray!!.add(
                        ToolTipEntry(
                            start,
                            end,
                            top,
                            bottom,
                            rowData.duration,
                            rowData.sleepType,
                            range
                        )
                    )
                    start = end
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        val x = event!!.x
        val y = event.y
        if (event.action == MotionEvent.ACTION_UP) {
            if (tooltipEntryArray != null && tooltipEntryArray?.size!! > 0) {
                val entry =
                    tooltipEntryArray!!.filter { it1 -> x > it1.x1 && x < it1.x2 && y > it1.y1 && y < it1.y2 }
                if (entry.isNotEmpty()) {
                    sleepGraphInteractionListener?.onSleepGraphSelected(entry[0])
                } else {
                    sleepGraphInteractionListener?.onSleepGraphSelected(null)
                }
            }
        }
        return true
    }

    fun setData(sleepArray: ArrayList<SleepData.SleepDataBreakup>?) {
        this.sleepArray?.clear()
        this.sleepArray = sleepArray?.filterNot {
            it.duration==0
        } as ArrayList<SleepData.SleepDataBreakup>
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
//        toolTipPaint.color = ContextCompat.getColor(mContext, R.color.white)
//        toolTipPaint.style = Paint.Style.FILL
//        toolTipTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
//        toolTipTextPaint.color = ContextCompat.getColor(mContext, R.color.blood_oxygen_color)
//        toolTipTextPaint.textSize = pxFromDp(mContext, 10f)
//        toolTipTextPaint.textAlign = Paint.Align.CENTER
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = ContextCompat.getColor(mContext, R.color.sleep_graph_line)
        mPaint.strokeWidth = pxFromDp(mContext, 2f)
        mPaint2 = Paint()
        mPaint2.isAntiAlias = true
        mPaint2.style = Paint.Style.STROKE
        mPaint2.color = ContextCompat.getColor(mContext, R.color.sleep_graph_line)
        mPaint2.strokeWidth = pxFromDp(mContext, 2f)
        mTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = ContextCompat.getColor(mContext, R.color.text_color)
        mTextPaint.textSize = pxFromDp(mContext, 12f)
        outerPaint = Paint()
        outerPaint.style = Paint.Style.FILL
        outerPaint.color = Color.TRANSPARENT

    }

    fun toggleStatus(isDisable: Boolean) {
        previousRect = null
        this.isDisable = isDisable
        setPaint()
        invalidate()
    }

    private fun setPaint() {
        val shaderDeep = if (isDisable) {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.deep_start),
                ContextCompat.getColor(mContext, R.color.deep_start),
                Shader.TileMode.CLAMP
            )
        }


        deepPaint = Paint()
        deepPaint.shader = shaderDeep

        val shaderLight = if (isDisable) {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.light_start),
                ContextCompat.getColor(mContext, R.color.light_start),
                Shader.TileMode.CLAMP
            )
        }


        lightPaint = Paint()
        lightPaint.shader = shaderLight
        val shaderAwake = if (isDisable) {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.awake_start),
                ContextCompat.getColor(mContext, R.color.awake_start),
                Shader.TileMode.CLAMP
            )
        }


        awakePaint = Paint()
        awakePaint.shader = shaderAwake


        val shaderRem = if (isDisable) {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                ContextCompat.getColor(mContext, R.color.sleep_gray),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                ContextCompat.getColor(mContext, R.color.rem_start),
                ContextCompat.getColor(mContext, R.color.rem_start),
                Shader.TileMode.CLAMP
            )
        }


        remPaint = Paint()
        remPaint.shader = shaderRem
    }

    fun init(isDisable: Boolean) {
        previousRect = null
        this.isDisable = isDisable
        setPaint()
        endPadding = pxFromDp(mContext, 48f)
    }
}