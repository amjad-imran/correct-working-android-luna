package com.noisefit_commans.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.noisefit_commans.R
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

class NightTimeGraphViewOreo(var mContext: Context) : View(
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
    private var mTextPaintEdge: Paint

    private var mPaintHighLine: Paint
    private var mPaintMedLine: Paint
    private var mPaintLowLine: Paint
    private var mPaintLow: Paint
    private var mPaintMed: Paint
    private var mPaintHigh: Paint
    var edgeTextBackPaint: Paint


    //    private var toolTipPaint: Paint = Paint()
//    private var toolTipTextPaint: Paint
    private var countCardData: CountCardData? = null
    private var sleepArray: ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>? = ArrayList()
    private var tooltipEntryArray: ArrayList<ToolTipEntry>? = ArrayList()
//    private var toolEntry: ToolTipEntry? = null

    var endPadding = 0.0f
    var startPadding = 0.0f

    fun setInteraction(sleepGraphInteractionListener: SleepGraphInteractionListener) {
        this.sleepGraphInteractionListener = sleepGraphInteractionListener
    }

    var previousRect: RectF? = null

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        previousRect = null

        val sectionHeight = height.toFloat() / 5

        val textXPos = (width.toFloat() - pxFromDp(mContext, 6f))
        tooltipEntryArray = ArrayList()

        if (!isDisable) {


            canvas.drawText(
                "High",
                textXPos - mTextPaint.measureText("High"),
                sectionHeight * 1,
                mTextPaint
            )
            canvas.drawText(
                "Med",
                textXPos - mTextPaint.measureText("Med"),
                sectionHeight * 2,
                mTextPaint
            )
            canvas.drawText(
                "Low",
                textXPos - mTextPaint.measureText("Low"),
                sectionHeight * 3,
                mTextPaint
            )
        }


        drawLines(sectionHeight, canvas)


        if (sleepArray != null && sleepArray!!.size > 0) {
            var totalDuration = 0
            for (i in sleepArray!!.indices) {
                totalDuration += sleepArray!![i].duration
            }

            val eachSecondsWidth = (width.toFloat() - endPadding) / totalDuration

            drawXAxisTime(
                canvas,
                sectionHeight,
                eachSecondsWidth,
                countCardData?.leftValue,
                countCardData?.rightValue
            )

            if (totalDuration != 0) {
                val eachMinutesWidth = (width.toFloat() - endPadding) / totalDuration
                LOGS.d("NIGHT_GRAPH $eachMinutesWidth")

                var start = startPadding
                var end: Float
                val radius = pxFromDp(mContext, 3f)

                for (i in sleepArray!!.indices) {
                    val rowData = sleepArray!![i]

                    end = start + eachMinutesWidth * rowData.duration


                    val mid = start + ((end - start) / 2)

                    LOGS.d("NIGHT_GRAPH start:$start mid:$mid end:$end")

                    when (SleepMovementType.getValueFromString(rowData.movementType)) {
                        SleepMovementType.LOW -> {
                            canvas.drawCircle(
                                mid,
                                sectionHeight * 3,
                                radius,
                                mPaintLow
                            )
                        }

                        SleepMovementType.MEDIUM -> {
                            canvas.drawCircle(
                                mid,
                                sectionHeight * 2,
                                radius,
                                mPaintMed
                            )
                        }

                        SleepMovementType.INTENSE -> {
                            canvas.drawCircle(
                                mid,
                                sectionHeight * 1,
                                radius,
                                mPaintHigh
                            )
                        }

                        SleepMovementType.NO_MOVEMENT -> {}
                    }

                    start = end
                }
            }
        } else {

            val edgeTextPadding = pxFromDp(4f)

            var rectF = RectF(
                0f,
                (sectionHeight * 3) + pxFromDp(8f),
                mTextPaintEdge.measureText("12 am") + edgeTextPadding * 2,
                (sectionHeight * 3) + pxFromDp(26f)
            )
            canvas.drawRoundRect(
                rectF,
                pxFromDp(4f),
                pxFromDp(4f),
                edgeTextBackPaint
            )

            canvas.drawText(
                "12 am",
                edgeTextPadding,
                (sectionHeight * 3) + pxFromDp(20f),
                mTextPaintEdge
            )


            val text = "12 am"
            val textWidth = mTextPaintEdge.measureText(text)
            rectF = RectF(
                (width - textWidth - endPadding) - edgeTextPadding * 2,
                (sectionHeight * 3) + pxFromDp(8f),
                width - endPadding,
                (sectionHeight * 3) + pxFromDp(26f)
            )
            canvas.drawRoundRect(
                rectF,
                pxFromDp(4f),
                pxFromDp(4f),
                edgeTextBackPaint
            )

            canvas.drawText(
                text,
                (width - textWidth - endPadding) - edgeTextPadding,
                (sectionHeight * 3) + pxFromDp(20f),
                mTextPaintEdge
            )
        }
    }

    private fun drawXAxisTime(
        canvas: Canvas,
        sectionHeight: Float,
        eachSecondsWidth: Float,
        startTimeStr: String?,
        endTimeStr: String?
    ) {

        if (startTimeStr == null || endTimeStr == null) return

        tryCatch {
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

            val startDateTime = LocalDateTime.parse(startTimeStr, formatter)
            val endDateTime = LocalDateTime.parse(endTimeStr, formatter)

            var currentDateTime = startDateTime

            while (currentDateTime < endDateTime) {
                var nextEvenHour =
                    currentDateTime.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
                if (nextEvenHour.hourOfDay % 2 != 0) {
                    nextEvenHour = nextEvenHour.plusHours(1)
                }

                currentDateTime = nextEvenHour
                if (nextEvenHour > endDateTime) {
                    break
                }

                val formatterDisplay = DateTimeFormat.forPattern("h a")
                val duration = Duration(startDateTime.toDateTime(), currentDateTime.toDateTime())
                val secondsDifference = duration.toStandardSeconds().seconds
                val startX = secondsDifference * eachSecondsWidth
                val textWidth =
                    mTextPaint.measureText(currentDateTime.toString(formatterDisplay).lowercase())


                val maxWidth = width - endPadding
                if (startX + textWidth < maxWidth) {
                    canvas.drawText(
                        currentDateTime.toString(formatterDisplay).lowercase(),
                        startX - textWidth / 2,
                        (sectionHeight * 3) + pxFromDp(20f),
                        mTextPaint
                    )
                }


            }
        }

        val edgeTextPadding = pxFromDp(4f)

        countCardData?.leftValue?.let { startTime ->

            val startText = DateFormats.formatDate(
                startTime,
                DateFormats.dateTimeFormat5,
                DateFormats.timeFormat12_2
            ).lowercase()

            val rectF = RectF(
                0f,
                (sectionHeight * 3) + pxFromDp(8f),
                mTextPaintEdge.measureText(startText) + edgeTextPadding * 2,
                (sectionHeight * 3) + pxFromDp(26f)
            )

            canvas.drawRoundRect(
                rectF,
                pxFromDp(4f),
                pxFromDp(4f),
                edgeTextBackPaint
            )

            canvas.drawText(
                startText,
                edgeTextPadding,
                (sectionHeight * 3) + pxFromDp(20f),
                mTextPaintEdge
            )


        }

        countCardData?.rightValue?.let { endTime ->

            val text = DateFormats.formatDate(
                endTime,
                DateFormats.dateTimeFormat5,
                DateFormats.timeFormat12_2
            )
            val textWidth = mTextPaintEdge.measureText(text)


            val rectF = RectF(
                (width - textWidth - endPadding) - edgeTextPadding * 2,
                (sectionHeight * 3) + pxFromDp(8f),
                width - endPadding,
                (sectionHeight * 3) + pxFromDp(26f)
            )
            canvas.drawRoundRect(
                rectF,
                pxFromDp(4f),
                pxFromDp(4f),
                edgeTextBackPaint
            )

            canvas.drawText(
                DateFormats.formatDate(
                    endTime,
                    DateFormats.dateTimeFormat5,
                    DateFormats.timeFormat12_2
                ).lowercase(),
                (width - textWidth - endPadding) - edgeTextPadding,
                (sectionHeight * 3) + pxFromDp(20f),
                mTextPaintEdge
            )
        }


        /*tryCatch {
            val startTime = DateFormats.dateTimeFormat5.parse(startTimeStr)
            val endTime = DateFormats.dateTimeFormat5.parse(endTimeStr)


            val duration = (endTime.time - startTime.time) / 1000
            LOGS.d("SLEEP_TIME $duration")

            if (duration > 18000) {//5 hour


                val midTime = getCenterTime(startTime, endTime)
                val midLeftTIme = getCenterTime(startTime, midTime)
                val midRightTIme = getCenterTime(midTime, endTime)

                val center = (width - endPadding) / 2

                val textWidth1 = mTextPaint.measureText(
                    DateFormats.time12Meridian.format(midLeftTIme).lowercase()
                )
                canvas.drawText(
                    DateFormats.time12Meridian.format(midLeftTIme).lowercase(),
                    center / 2 - textWidth1 / 2,
                    sectionHeight * 5 - pxFromDp(5.0f),
                    mTextPaint
                )


                val textWidthCenter =
                    mTextPaint.measureText(DateFormats.time12Meridian.format(midTime).lowercase())

                canvas.drawText(
                    DateFormats.time12Meridian.format(midTime).lowercase(),
                    center - textWidthCenter / 2,
                    sectionHeight * 5 - pxFromDp(5.0f),
                    mTextPaint
                )
                val textWidth2 = mTextPaint.measureText(
                    DateFormats.time12Meridian.format(midRightTIme).lowercase()
                )
                canvas.drawText(
                    DateFormats.time12Meridian.format(midRightTIme).lowercase(),
                    center + (center / 2) - textWidth2 / 2,
                    sectionHeight * 5 - pxFromDp(5.0f),
                    mTextPaint
                )

            } else {
                val midTime = getCenterTime(startTime, endTime)
                val center = (width - endPadding) / 2

                val textWidthCenter =
                    mTextPaint.measureText(DateFormats.time12Meridian.format(midTime).lowercase())

                canvas.drawText(
                    DateFormats.time12Meridian.format(midTime).lowercase(),
                    center - textWidthCenter / 2,
                    sectionHeight * 5 - pxFromDp(5.0f),
                    mTextPaint
                )
            }
        }*/


    }

    fun pxFromDp(dp: Float): Float {
        return dp * this.resources.displayMetrics.density
    }

    private fun drawLines(sectionHeight: Float, canvas: Canvas) {
        canvas.drawLine(
            startPadding,
            sectionHeight * 1,
            width.toFloat() - endPadding,
            sectionHeight * 1,
            mPaintHighLine
        )
        canvas.drawLine(
            startPadding,
            sectionHeight * 2,
            width.toFloat() - endPadding,
            sectionHeight * 2,
            mPaintMedLine
        )
        canvas.drawLine(
            startPadding,
            sectionHeight * 3,
            width.toFloat() - endPadding,
            sectionHeight * 3,
            mPaintLowLine
        )
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

    fun setData(sleepArray: List<OreoSleepData.OreoSleepMovementDataBreakup>?) {
        this.sleepArray?.clear()
        this.sleepArray = sleepArray?.filterNot {
            it.duration == 0
        } as ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>
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

        edgeTextBackPaint = Paint()
        edgeTextBackPaint.color = Color.parseColor("#394653")

        val fontGilroy = ResourcesCompat.getFont(this.context, R.font.gilroy_medium)



        mPaintHighLine = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.parseColor("#80ffffff")
            strokeWidth = pxFromDp(mContext, 1f)
        }
        mPaintMedLine = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.parseColor("#80ac7edb")
            strokeWidth = pxFromDp(mContext, 1f)
        }
        mPaintLowLine = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.parseColor("#807156cc")
            strokeWidth = pxFromDp(mContext, 1f)
        }

        mPaintLow = Paint().apply {
            style = Paint.Style.FILL
            typeface = fontGilroy
            color = Color.parseColor("#7156cc")
        }
        mPaintMed = Paint().apply {
            style = Paint.Style.FILL
            typeface = fontGilroy
            color = Color.parseColor("#ac7edb")
        }
        mPaintHigh = Paint().apply {
            style = Paint.Style.FILL
            typeface = fontGilroy
            color = Color.parseColor("#ffffff")
        }



        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = ContextCompat.getColor(mContext, R.color.white_12)
        mPaint.strokeWidth = pxFromDp(mContext, 1f)
        mPaint.typeface = fontGilroy


        mPaint2 = Paint()
        mPaint2.isAntiAlias = true
        mPaint2.style = Paint.Style.STROKE
        mPaint2.color = ContextCompat.getColor(mContext, R.color.sleep_graph_line)
        mPaint2.strokeWidth = pxFromDp(mContext, 2f)
        mPaint2.typeface = fontGilroy


        mTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = ContextCompat.getColor(mContext, R.color.white_64)
        mTextPaint.textSize = pxFromDp(mContext, 10f)
        mTextPaint.typeface = fontGilroy

        outerPaint = Paint()
        outerPaint.style = Paint.Style.FILL
        outerPaint.color = Color.TRANSPARENT

        mTextPaintEdge = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaintEdge.color = ContextCompat.getColor(mContext, R.color.white)
        mTextPaintEdge.typeface = fontGilroy
        mTextPaintEdge.textSize = pxFromDp(mContext, 10f)

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
                ContextCompat.getColor(mContext, R.color.deep_start_oreo),
                ContextCompat.getColor(mContext, R.color.deep_start_oreo),
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
                ContextCompat.getColor(mContext, R.color.light_start_oreo),
                ContextCompat.getColor(mContext, R.color.light_start_oreo),
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
                ContextCompat.getColor(mContext, R.color.awake_start_oreo),
                ContextCompat.getColor(mContext, R.color.awake_start_oreo),
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
                ContextCompat.getColor(mContext, R.color.rem_start_oreo),
                ContextCompat.getColor(mContext, R.color.rem_start_oreo),
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
        endPadding = pxFromDp(mContext, 40f)
        startPadding = pxFromDp(mContext, 3f)
    }
}