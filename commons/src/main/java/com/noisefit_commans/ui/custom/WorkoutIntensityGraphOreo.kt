package com.noisefit_commans.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.noisefit_commans.R
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import java.util.Calendar
import java.util.Date

class WorkoutIntensityGraphOreo(var mContext: Context) : View(
    mContext
) {
    private var sleepGraphInteractionListener: SleepGraphInteractionListener? = null
    private var isDisable = false
    private var mPaint: Paint
    private var mPaint2: Paint
    private var outerPaint: Paint
    private lateinit var highPaint: Paint
    private lateinit var lowPaint: Paint
    private lateinit var inactivePaint: Paint
    private lateinit var mediumPaint: Paint
    private var mTextPaint: Paint
    private var mTextPaintCenter: Paint
    private var mTextPaintEdge: Paint

    //    private var toolTipPaint: Paint = Paint()
//    private var toolTipTextPaint: Paint

    private var movementList = ArrayList<Int>()
    private var xAxisList = ArrayList<String?>()
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

        val textXPos = (width.toFloat() - endPadding + pxFromDp(mContext, 14f))
        tooltipEntryArray = ArrayList()
        val textPosOffset = pxFromDp(mContext, 10f)

        if (!isDisable) {
            canvas.drawText("High", textXPos, sectionHeight * 1, mTextPaint)
            canvas.drawText("Med", textXPos, sectionHeight * 2, mTextPaint)
            canvas.drawText("Low", textXPos, sectionHeight * 3, mTextPaint)
            canvas.drawText("None", textXPos, sectionHeight * 4, mTextPaint)
        }


        for (i in 1 until 5) {
            canvas.drawLine(
                0f,
                sectionHeight * i,
                width.toFloat() - endPadding,
                sectionHeight * i,
                mPaint
            )
        }


        if (movementList.size > 0) {

            /*  mStartTime?.let { startTime ->
                  canvas.drawText(
                      startTime.lowercase(),
                      0f,
                      (sectionHeight * 5) - pxFromDp(context, 5.0f),
                      mTextPaintEdge
                  )
              }

              mEndTime?.let { endTime ->


                  val textWidth = mTextPaintEdge.measureText(endTime)


                  canvas.drawText(
                      endTime.lowercase(),
                      (width - textWidth - endPadding),
                      sectionHeight * 5 - pxFromDp(context, 5.0f),
                      mTextPaintEdge
                  )
              }*/


            val dataSize = movementList.size
            val totalDuration = dataSize * 5


            if (totalDuration != 0) {
                val eachSecondsWidth = (width.toFloat() - endPadding) / (totalDuration * 2)

                var start = 0f
                var end: Float
                var top = 0f
                val bottom = sectionHeight * 4 - pxFromDp(mContext, 2f)
                val textY = sectionHeight * 5 - pxFromDp(mContext, 6f)

                val sectionWidth = eachSecondsWidth * 5


                start = sectionWidth / 2
                movementList.forEachIndexed { index, value ->


                    end = start + sectionWidth
                    var rectF: RectF? = null
                    var paint: Paint? = null

                    when (value) {
                        1 -> {
                            top = sectionHeight * 3
                            rectF = RectF(start, top, end, bottom)
                            paint = lowPaint
                        }

                        2 -> {
                            top = sectionHeight * 2
                            rectF = RectF(start, top, end, bottom)
                            paint = mediumPaint

                        }

                        3 -> {
                            top = sectionHeight * 1
                            rectF = RectF(start, top, end, bottom)
                            paint = highPaint
                        }

                        else -> {
                            top = sectionHeight * 3.7f
                            rectF = RectF(start, top, end, bottom)
                            paint = inactivePaint

                        }
                    }

                    canvas.drawRoundRect(
                        rectF,
                        10f,
                        10f,
                        paint
                    )


                    setXAxis(xAxisList, index, canvas, start, sectionWidth, textY, dataSize)

                    start = end + sectionWidth
                }
            }
        } else {
            if (xAxisList.size == 2) {
                canvas.drawText(
                    xAxisList[0].toString(),
                    0f,
                    (sectionHeight * 5) - pxFromDp(context, 5.0f),
                    mTextPaint
                )

                val textWidth = mTextPaint.measureText("12 am")
                canvas.drawText(
                    xAxisList[1].toString(),
                    (width - textWidth - endPadding),
                    sectionHeight * 5 - pxFromDp(context, 5.0f),
                    mTextPaint
                )
            } else {
                canvas.drawText(
                    "12 am",
                    0f,
                    (sectionHeight * 5) - pxFromDp(context, 5.0f),
                    mTextPaint
                )

                val textWidth = mTextPaint.measureText("12 am")
                canvas.drawText(
                    "12 am",
                    (width - textWidth - endPadding),
                    sectionHeight * 5 - pxFromDp(context, 5.0f),
                    mTextPaint
                )
            }

        }
    }

    private fun setXAxis(
        xAxisList: List<String?>,
        index: Int,
        canvas: Canvas,
        start: Float,
        sectionWidth: Float,
        textY: Float,
        dataSize: Int
    ) {
        if (xAxisList.size != dataSize) {
            return
        }

        if (!xAxisList[index].isNullOrEmpty()) {
            when (index) {
                0 -> {
                    val text = xAxisList[index] ?: return
                    val width = mTextPaintCenter.measureText(text)
                    val textStart = start

                    canvas.drawText(text, textStart, textY, mTextPaintEdge)
                }

                (dataSize - 1) -> {
                    val text = xAxisList[index] ?: return
                    val width = mTextPaintCenter.measureText(text)
                    val textStart = start + sectionWidth / 2 - width / 2

                    canvas.drawText(text, textStart, textY, mTextPaintEdge)
                }

                else -> {
                    val value = if (dataSize < 4) 1 else 0

                    if (value == 1) {
                        val text = xAxisList[index] ?: return
                        val width = mTextPaintCenter.measureText(text)
                        val textStart = start + sectionWidth / 2 - width / 2
                        canvas.drawText(text, textStart, textY, mTextPaintCenter)
                    } else {

                        val center = xAxisList.size / 2

                        if (center == index) {
                            val text = xAxisList[index] ?: return
                            val width = mTextPaintCenter.measureText(text)
                            val textStart = start + sectionWidth / 2 - width / 2
                            canvas.drawText(text, textStart, textY, mTextPaintCenter)
                        }
                    }
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

    fun setData(
        movementList: List<Int>,
        xAxisList: List<String?>
    ) {
        this.movementList.clear()
        this.movementList.addAll(movementList)
        this.xAxisList.clear()
        this.xAxisList.addAll(xAxisList)
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
        val fontGilroy = ResourcesCompat.getFont(this.context, R.font.gilroy_medium)

        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.parseColor("#1effffff")
        mPaint.strokeWidth = pxFromDp(mContext, 1f)
        mPaint2 = Paint()
        mPaint2.isAntiAlias = true
        mPaint2.style = Paint.Style.STROKE
        mPaint2.color = ContextCompat.getColor(mContext, R.color.sleep_graph_line)
        mPaint2.strokeWidth = pxFromDp(mContext, 2f)

        mTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = ContextCompat.getColor(mContext, R.color.white_64)
        mTextPaint.textSize = pxFromDp(mContext, 10f)
        mTextPaint.typeface = fontGilroy

        mTextPaintCenter = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaintCenter.color = ContextCompat.getColor(mContext, R.color.white_48)
        mTextPaintCenter.textSize = pxFromDp(mContext, 9f)
        mTextPaintCenter.typeface = fontGilroy

        outerPaint = Paint()
        outerPaint.style = Paint.Style.FILL
        outerPaint.color = Color.TRANSPARENT


        mTextPaintEdge = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mTextPaintEdge.color = ContextCompat.getColor(mContext, R.color.white)
        mTextPaintEdge.typeface = fontGilroy
        mTextPaintEdge.textSize = pxFromDp(mContext, 9f)

    }

    private fun setPaint() {
        val shaderLow =
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                Color.parseColor("#4c88d6eb"),
                Color.parseColor("#4c88d6eb"),
                Shader.TileMode.CLAMP
            )


        lowPaint = Paint()
        lowPaint.shader = shaderLow

        val shaderInactive =
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                Color.parseColor("#6f6f6f"),
                Color.parseColor("#b36f6f6f"),
                Shader.TileMode.CLAMP
            )



        inactivePaint = Paint()
        inactivePaint.shader = shaderInactive
        val shaderMedium =
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                Color.parseColor("#6f6f6f"),
                Color.parseColor("#6f6f6f"),
                Shader.TileMode.CLAMP
            )



        mediumPaint = Paint()
        mediumPaint.shader = shaderMedium


        val shaderHigh =
            LinearGradient(
                0f,
                0f,
                0f,
                pxFromDp(mContext, 10f),
                Color.parseColor("#ffffff"),
                Color.parseColor("#ffffff"),
                Shader.TileMode.CLAMP
            )



        highPaint = Paint()
        highPaint.shader = shaderHigh
    }

    fun init(isDisable: Boolean) {
        previousRect = null
        this.isDisable = isDisable
        setPaint()
        endPadding = pxFromDp(mContext, 40f)
    }
}