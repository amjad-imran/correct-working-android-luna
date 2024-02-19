package com.noisefit_commans.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.noisefit_commans.R
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.Calendar
import java.util.Date

class SleepGraphViewOreo(var mContext: Context) : View(
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

    private lateinit var remPaintInteracting: Paint
    private lateinit var deepPaintInteracting: Paint
    private lateinit var lightPaintInteracting: Paint
    private lateinit var awakePaintInteracting: Paint

    var overlayLinePaint: Paint
    var edgeTextBackPaint: Paint

    var lastSelectedEntry: ToolTipEntry? = null


    private var mTextPaint: Paint
    private var mTextPaintEdge: Paint

    //    private var toolTipPaint: Paint = Paint()
//    private var toolTipTextPaint: Paint
    private var countCardData: CountCardData? = null
    private var sleepArray: ArrayList<SleepData.SleepDataBreakup>? = ArrayList()
    private var tooltipEntryArray: ArrayList<ToolTipEntry>? = ArrayList()
//    private var toolEntry: ToolTipEntry? = null

    private var interactiveMode = true
    private var isInteracting = false
    private var listener: SleepStageAction? = null
    private var touchX: Float? = null


    var endPadding = 0.0f

    fun setInteraction(sleepGraphInteractionListener: SleepGraphInteractionListener) {
        this.sleepGraphInteractionListener = sleepGraphInteractionListener
    }

    var previousRect: RectF? = null

    init {
//        toolTipPaint.color = ContextCompat.getColor(mContext, R.color.white)
//        toolTipPaint.style = Paint.Style.FILL
//        toolTipTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
//        toolTipTextPaint.color = ContextCompat.getColor(mContext, R.color.blood_oxygen_color)
//        toolTipTextPaint.textSize = pxFromDp( 10f)
//        toolTipTextPaint.textAlign = Paint.Align.CENTER
        val fontGilroy = ResourcesCompat.getFont(this.context, R.font.gilroy_medium)

        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.parseColor("#1effffff")
        mPaint.strokeWidth = pxFromDp(1f)
        mPaint2 = Paint()
        mPaint2.isAntiAlias = true
        mPaint2.style = Paint.Style.STROKE
        mPaint2.color = ContextCompat.getColor(mContext, R.color.sleep_graph_line)
        mPaint2.strokeWidth = pxFromDp(2f)

        mTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = ContextCompat.getColor(mContext, R.color.white_64)
        mTextPaint.textSize = pxFromDp(10f)
        mTextPaint.setTypeface(fontGilroy)

        outerPaint = Paint()
        outerPaint.style = Paint.Style.FILL
        outerPaint.color = Color.TRANSPARENT


        mTextPaintEdge = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaintEdge.color = ContextCompat.getColor(mContext, R.color.white)
        mTextPaintEdge.setTypeface(fontGilroy)
        mTextPaintEdge.textSize = pxFromDp(10f)

        overlayLinePaint = Paint()
        overlayLinePaint.color = Color.parseColor("#bad4f2")

        edgeTextBackPaint = Paint()
        edgeTextBackPaint.color = Color.parseColor("#808080")

    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val sectionHeight = height.toFloat() / 5
        drawBackGrid(canvas, sectionHeight)
        drawYAxis(canvas, sectionHeight)
        drawOverlay(canvas, sectionHeight)
        drawContent(canvas, sectionHeight)
    }

    private fun drawOverlay(canvas: Canvas, sectionHeight: Float) {
        if (!isInteracting) return
        if (touchX != null) {
            val textXPos = (width.toFloat() - endPadding)

            if (touchX!! > 0 && touchX!! < textXPos) {
                val rectF = RectF()
                rectF.left = touchX!! - 2
                rectF.right = touchX!! + 2
                rectF.top = 0f
                rectF.bottom = sectionHeight * 4

                canvas.drawRect(rectF, overlayLinePaint)

                if (listener != null && touchX != null) {

                    if (tooltipEntryArray != null && tooltipEntryArray?.size!! > 0) {

                        val x = touchX!!.toFloat()

                        val entry =
                            tooltipEntryArray!!.filter { it1 -> x > it1.x1 && x < it1.x2 }
                        if (entry.isNotEmpty()) {

                            if (lastSelectedEntry == null || lastSelectedEntry != entry.first()) {
                                lastSelectedEntry = entry.first()
                                listener?.onValueSelected(entry.first())

                                this.performHapticFeedback(
                                    HapticFeedbackConstants.KEYBOARD_TAP
                                )
                                postInvalidate()
                            }

                            //sleepGraphInteractionListener?.onSleepGraphSelected(entry[0])
                        } else {
                            //sleepGraphInteractionListener?.onSleepGraphSelected(null)
                        }
                    }


                }

                /* if (listener != null) {
                     val position = value.first as Int
                     val selectedValue = value.second as Int
                     if (lastSentValuePos == null) {
                         listener?.onValueSelected(selectedValue, position)
                         lastSentValuePos = position
                         performHapticFeedbackCustom(selectedValue)
                     } else {
                         if (lastSentValuePos != position) {
                             listener?.onValueSelected(selectedValue, position)
                             lastSentValuePos = position
                             performHapticFeedbackCustom(selectedValue)
                         }
                     }
                 }*/
            }
        }
    }

    private fun drawContent(canvas: Canvas, sectionHeight: Float) {
        previousRect = null
        tooltipEntryArray = ArrayList()

        if (sleepArray != null && sleepArray!!.size > 0) {

            var totalDuration = 0
            for (i in sleepArray!!.indices) {
                totalDuration += sleepArray!![i].duration
            }
            if (totalDuration != 0) {
                val eachSecondsWidth = (width.toFloat() - endPadding) / totalDuration

                drawXAxisTime(
                    canvas,
                    sectionHeight,
                    eachSecondsWidth,
                    countCardData?.leftValue,
                    countCardData?.rightValue
                )

                val lineWidth = pxFromDp(1f)

                var start = 0f
                var end: Float
                var top = 0f
                var bottom = 0f
                val barHeight = pxFromDp(16f)


                for (i in sleepArray!!.indices) {
                    var paint: Paint? = null
                    val rowData = sleepArray!![i]


                    end = start + eachSecondsWidth * rowData.duration
                    if (rowData.sleepType == "deep") {
                        paint = if (isInteracting) deepPaintInteracting else deepPaint
                        top = sectionHeight * 3 + barHeight / 2
                        bottom = sectionHeight * 4 - barHeight / 2
                    }
                    if (rowData.sleepType == "light") {
                        paint = if (isInteracting) lightPaintInteracting else lightPaint
                        top = sectionHeight * 2 + barHeight / 2
                        bottom = sectionHeight * 3 - barHeight / 2
                    }
                    if (rowData.sleepType == "rem") {
                        paint = if (isInteracting) remPaintInteracting else remPaint
                        top = sectionHeight * 1 + barHeight / 2
                        bottom = sectionHeight * 2 - barHeight / 2
                    }
                    if (rowData.sleepType == "awake") {
                        paint = if (isInteracting) awakePaintInteracting else awakePaint
                        top = barHeight / 2
                        bottom = sectionHeight * 1 - barHeight / 2
                    }

                    if (lastSelectedEntry != null) {
                        if (rowData.startTime.equals(lastSelectedEntry?.startTime, true)
                            && rowData.endTime.equals(lastSelectedEntry?.endTime, true)
                        ) {
                            if (lastSelectedEntry?.type.equals("deep", true)) {
                                paint = deepPaint
                            } else if (lastSelectedEntry?.type.equals("light", true)) {
                                paint = lightPaint
                            } else if (lastSelectedEntry?.type.equals("rem", true)) {
                                paint = remPaint
                            } else if (lastSelectedEntry?.type.equals("awake", true)) {
                                paint = awakePaint
                            }
                        }
                    }


                    if (paint != null) {
                        val rectF = RectF(start, top, end + lineWidth, bottom)
                        /* canvas.drawRoundRect(
                             rectF,
                             pxFromDp( 4f),
                             pxFromDp( 4f),
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

                        val radius = pxFromDp(4f)
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
                                        if (isInteracting) mContext.getColor(R.color.deep_start_oreo_i) else
                                            mContext.getColor(R.color.deep_start_oreo)
                                    when (previousElement.sleepType) {
                                        "light" -> {
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.light_start_oreo_i) else
                                                    mContext.getColor(R.color.light_start_oreo)
                                        }

                                        "rem" -> {
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.rem_start_oreo_i) else
                                                    mContext.getColor(R.color.rem_start_oreo)
                                        }

                                        "awake" -> {
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.awake_start_oreo_i) else
                                                    mContext.getColor(R.color.awake_start_oreo)
                                        }
                                    }
                                }

                                "light" -> {
                                    startColor =
                                        if (isInteracting) mContext.getColor(R.color.light_start_oreo_i) else
                                            mContext.getColor(R.color.light_start_oreo)
                                    when (previousElement.sleepType) {
                                        "deep" -> {
                                            bottomLeftRadius = 0f

                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.deep_start_oreo_i) else
                                                    mContext.getColor(R.color.deep_start_oreo)
                                        }

                                        "rem" -> {
                                            topLeftRadius = 0f
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.rem_start_oreo_i) else
                                                    mContext.getColor(R.color.rem_start_oreo)
                                        }

                                        "awake" -> {
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.awake_start_oreo_i) else
                                                    mContext.getColor(R.color.awake_start_oreo)
                                            topLeftRadius = 0f
                                        }

                                    }
                                }

                                "rem" -> {
                                    startColor =
                                        if (isInteracting) mContext.getColor(R.color.rem_start_oreo_i) else
                                            mContext.getColor(R.color.rem_start_oreo)
                                    when (previousElement.sleepType) {
                                        "deep" -> {
                                            bottomLeftRadius = 0f
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.deep_start_oreo_i) else
                                                    mContext.getColor(R.color.deep_start_oreo)
                                        }

                                        "light" -> {
                                            bottomLeftRadius = 0f
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.light_start_oreo_i) else
                                                    mContext.getColor(R.color.light_start_oreo)
                                        }

                                        "awake" -> {
                                            topLeftRadius = 0f
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.awake_start_oreo_i) else
                                                    mContext.getColor(R.color.awake_start_oreo)
                                        }

                                    }
                                }

                                "awake" -> {
                                    bottomLeftRadius = 0f

                                    startColor =
                                        if (isInteracting) mContext.getColor(R.color.awake_start_oreo_i) else
                                            mContext.getColor(R.color.awake_start_oreo)
                                    when (previousElement.sleepType) {
                                        "deep" -> {
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.deep_start_oreo_i) else
                                                    mContext.getColor(R.color.deep_start_oreo)
                                        }

                                        "rem" -> {
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.rem_start_oreo_i) else
                                                    mContext.getColor(R.color.rem_start_oreo)
                                        }

                                        "light" -> {
                                            endColor =
                                                if (isInteracting) mContext.getColor(R.color.light_start_oreo_i) else
                                                    mContext.getColor(R.color.light_start_oreo)
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

                    tooltipEntryArray!!.add(
                        ToolTipEntry(
                            x1 = start,
                            x2 = end,
                            y1 = top,
                            y2 = bottom,
                            duration = rowData.duration,
                            type = rowData.sleepType,
                            startTime = sleepArray!![i].startTime,
                            endTime = sleepArray!![i].endTime
                        )
                    )
                    start = end
                }
            }
        } else {

            val edgeTextPadding = pxFromDp(4f)



            var rectF = RectF(
                0f,
                (sectionHeight * 4) + pxFromDp(8f),
                mTextPaintEdge.measureText("12 am") + edgeTextPadding * 2,
                height.toFloat()
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
                (sectionHeight * 5) - pxFromDp(5.0f),
                mTextPaintEdge
            )


            val text = "12 am"
            val textWidth = mTextPaintEdge.measureText(text)
            rectF = RectF(
                (width - textWidth - endPadding) - edgeTextPadding * 2,
                (sectionHeight * 4) + pxFromDp(8f),
                width - endPadding,
                height.toFloat()
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
                sectionHeight * 5 - pxFromDp(5.0f),
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

                canvas.drawText(
                    currentDateTime.toString(formatterDisplay).lowercase(),
                    startX - textWidth / 2,
                    sectionHeight * 5 - pxFromDp(5.0f),
                    mTextPaint
                )


            }
        }

        val edgeTextPadding = pxFromDp(4f)

        countCardData?.leftValue?.let { startTime ->

            val startText = DateFormats.formatDate(
                startTime,
                DateFormats.dateTimeFormat5,
                DateFormats.time12Meridian
            ).lowercase()

            val rectF = RectF(
                0f,
                (sectionHeight * 4) + pxFromDp(8f),
                mTextPaintEdge.measureText(startText) + edgeTextPadding * 2,
                height.toFloat()
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
                (sectionHeight * 5) - pxFromDp(5.0f),
                mTextPaintEdge
            )


        }

        countCardData?.rightValue?.let { endTime ->

            val text = DateFormats.formatDate(
                endTime,
                DateFormats.dateTimeFormat5,
                DateFormats.time12Meridian
            )
            val textWidth = mTextPaintEdge.measureText(text)


            val rectF = RectF(
                (width - textWidth - endPadding) - edgeTextPadding * 2,
                (sectionHeight * 4) + pxFromDp(8f),
                width - endPadding,
                height.toFloat()
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
                    DateFormats.time12Meridian
                ).lowercase(),
                (width - textWidth - endPadding) - edgeTextPadding,
                sectionHeight * 5 - pxFromDp(5.0f),
                mTextPaintEdge
            )
        }

    }

    private fun drawBackGrid(canvas: Canvas, sectionHeight: Float) {
        for (i in 0 until 5) {
            canvas.drawLine(
                0f,
                sectionHeight * i,
                width.toFloat() - endPadding,
                sectionHeight * i,
                mPaint
            )
        }
    }

    private fun drawYAxis(canvas: Canvas, sectionHeight: Float) {

        val textXPos = (width.toFloat() - endPadding + pxFromDp(14f))
        val textPosOffset = pxFromDp(10f)

        canvas.drawText("Awake", textXPos, (sectionHeight * 1 - textPosOffset), mTextPaint)
        canvas.drawText("Rem", textXPos, (sectionHeight * 2 - textPosOffset), mTextPaint)
        canvas.drawText("Light", textXPos, (sectionHeight * 3 - textPosOffset), mTextPaint)
        canvas.drawText("Deep", textXPos, (sectionHeight * 4 - textPosOffset), mTextPaint)


    }

    private fun drawMidPoints(
        canvas: Canvas,
        sectionHeight: Float,
        eachSecondsWidth: Float,
        startTimeStr: String?,
        endTimeStr: String?
    ) {
        if (startTimeStr == null || endTimeStr == null) return

        tryCatch {
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
        }
    }


    private fun getCenterTime(
        startTime: Date,
        endTime: Date
    ): Date {
        val timeRange = endTime.time - startTime.time
        val singleDuration = timeRange / 2

        val centerTime = startTime.time + singleDuration
        val calendar = Calendar.getInstance()
        calendar.time = Date(centerTime)

        /* val minutes = calendar.get(Calendar.MINUTE)
         val seconds = calendar.get(Calendar.SECOND)

         val totalSeconds = (minutes * 60) + seconds

         calendar.set(Calendar.MINUTE, 0)
         calendar.set(Calendar.SECOND, 0)*/

        return Date(calendar.timeInMillis)

    }


    fun setData(sleepArray: ArrayList<SleepData.SleepDataBreakup>?) {
        this.sleepArray?.clear()
        this.sleepArray = sleepArray?.filterNot {
            it.duration == 0
        } as ArrayList<SleepData.SleepDataBreakup>
    }

    fun setData(countCardData: CountCardData?) {
        this.countCardData = countCardData
    }

    private fun pxFromDp(dp: Float): Float {
        return dp * this.resources.displayMetrics.density
    }


    fun toggleStatus(isDisable: Boolean) {
        previousRect = null
        this.isDisable = isDisable
        setPaint()
        invalidate()
    }

    private fun setPaint() {

        deepPaint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(10f),
                ContextCompat.getColor(mContext, R.color.deep_start_oreo),
                ContextCompat.getColor(mContext, R.color.deep_start_oreo),
                Shader.TileMode.CLAMP
            )
        }
        lightPaint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(10f),
                ContextCompat.getColor(mContext, R.color.light_start_oreo),
                ContextCompat.getColor(mContext, R.color.light_start_oreo),
                Shader.TileMode.CLAMP
            )
        }
        remPaint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(10f),
                ContextCompat.getColor(mContext, R.color.rem_start_oreo),
                ContextCompat.getColor(mContext, R.color.rem_start_oreo),
                Shader.TileMode.CLAMP
            )
        }
        awakePaint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(10f),
                ContextCompat.getColor(mContext, R.color.awake_start_oreo),
                ContextCompat.getColor(mContext, R.color.awake_start_oreo),
                Shader.TileMode.CLAMP
            )
        }


        deepPaintInteracting = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(10f),
                ContextCompat.getColor(mContext, R.color.deep_start_oreo_i),
                ContextCompat.getColor(mContext, R.color.deep_start_oreo_i),
                Shader.TileMode.CLAMP
            )
        }

        lightPaintInteracting = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(10f),
                ContextCompat.getColor(mContext, R.color.light_start_oreo_i),
                ContextCompat.getColor(mContext, R.color.light_start_oreo_i),
                Shader.TileMode.CLAMP
            )
        }
        remPaintInteracting = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(10f),
                ContextCompat.getColor(mContext, R.color.rem_start_oreo_i),
                ContextCompat.getColor(mContext, R.color.rem_start_oreo_i),
                Shader.TileMode.CLAMP
            )
        }
        awakePaintInteracting = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(10f),
                ContextCompat.getColor(mContext, R.color.awake_start_oreo_i),
                ContextCompat.getColor(mContext, R.color.awake_start_oreo_i),
                Shader.TileMode.CLAMP
            )
        }
    }

    fun init(isDisable: Boolean) {
        previousRect = null
        this.isDisable = isDisable
        setPaint()
        endPadding = pxFromDp(48f)
    }


    /*@SuppressLint("ClickableViewAccessibility")
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
    }*/

    fun enableInteractiveMode(mode: Boolean) {
        interactiveMode = mode
    }

    fun setClickListener(listener: SleepStageAction?) {
        this.listener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (interactiveMode) {
            val parent = parent
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = event.x
                    handler.postDelayed(
                        mLongPressed,
                        ViewConfiguration.getLongPressTimeout().toLong()
                    )
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isInteracting) {
                        touchX = event.x
                        invalidate()
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(mLongPressed)
                    isInteracting = false
                    lastSelectedEntry = null
                    listener?.isInteractionOnGoing(false)
                    touchX = 0.0f
                    invalidate()
                    return true
                }
            }
        } else {
            return super.onTouchEvent(event)
        }
        return false
    }


    private val handler = Handler(Looper.getMainLooper())
    private var mLongPressed = Runnable {
        isInteracting = true
        invalidate()
        listener?.isInteractionOnGoing(true)
        rootView.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS
        )
    }
}