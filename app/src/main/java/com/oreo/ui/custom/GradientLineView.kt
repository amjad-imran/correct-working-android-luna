package com.oreo.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View


class GradientLineView : View {
    private var paint: Paint? = null
    private var path: Path? = null

    var startValue = 0
    var currentValue = 0
    var lastValue = 0


    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        // Initialize the Paint and Path objects
        paint = Paint()
        path = Path()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val x1 = -dpToPx(16)
        val y1 = dpToPx((120 * (((100 - startValue)).toFloat() / 100)).toInt())
        val x2 = dpToPx(16)
        val y2 = dpToPx((120 * (((100 - currentValue)).toFloat() / 100)).toInt())
        val x3 = dpToPx(16) + dpToPx(32)
        val y3 = dpToPx((120 * (((100 - lastValue)).toFloat() / 100)).toInt())

        /* val path = Path()

         val startX: Float = -dpToPx(16)//x1
         val startY: Float = dpToPx((120 * (((100 - startValue)).toFloat() / 100)).toInt())//y1
         path.moveTo(startX, startY)


         val controlX: Float = dpToPx(16)//x2
         val controlY: Float = dpToPx((120 * (((100 - currentValue)).toFloat() / 100)).toInt())//y2

         val endX: Float = dpToPx(16) + dpToPx(32)//x3
         val endY: Float = dpToPx((120 * (((100 - lastValue)).toFloat() / 100)).toInt())//y3
         path.quadTo(controlX, controlY, endX, endY)


         LOGS.d("CO_ORDINATES   $startX $startY | $controlX $controlY |  $endX $endY ")

         val paint = Paint()
         paint.color = Color.WHITE
         paint.style = Paint.Style.STROKE
         paint.strokeWidth = 2f

         canvas.drawPath(path, paint)*/


        //Draw Straight lines
        val midX1 = (x1 + x2) / 2f
        val midY1 = (y1 + y2) / 2f


        val midX2 = (x2 + x3) / 2f
        val midY2 = (y2 + y3) / 2f


        val path = Path()
        path.moveTo(x1, y1)
        path.quadTo(midX1, midY1, x2, y2)
        path.quadTo(midX2, midY2, x3, y3)


        val paint = Paint()
        paint.color = Color.parseColor("#ca99ff")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f

        canvas.drawPath(path,paint)

        /**
         * Clip
         */

        val paintGrad = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                canvas.height.toFloat(),
                Color.parseColor("#4caa5bff"),
                Color.parseColor("#00aa5bff"),
                Shader.TileMode.CLAMP
            )
        }

        val width = 6f

        val clipPath = Path().apply {

            moveTo(x1, canvas.height.toFloat())
            lineTo(x1, y1 + width)
            lineTo(x2, y2 + width)
            lineTo(x3, y3 + width)
            lineTo(x3, canvas.height.toFloat())
            close()
        }

        canvas.clipPath(clipPath)
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paintGrad)

        /**
         * =====
         */


        //setGradient(path, canvas, x2, y2, x3, y3)

    }

    private fun setGradient(canvas: Canvas, path: Path) {
        val paint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                canvas.height.toFloat(),
                Color.RED,
                Color.BLUE,
                Shader.TileMode.CLAMP
            )
        }

        val clipPath = Path().apply {
            val rect = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
            addRoundRect(rect, 20f, 20f, Path.Direction.CW)
        }
        canvas.clipPath(path)
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
    }

    private fun setGradient(
        path: Path,
        canvas: Canvas,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float
    ) {


        val slopePoints = ArrayList<Point>()
        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length

        val point = FloatArray(2)
        var distance = 0f
        while (distance <= pathLength) {

            // Retrieve the point at the given distance along the path
            pathMeasure.getPosTan(distance, point, null)
            val x: Float = point.get(0)
            val y: Float = point.get(1)

            // Use the obtained point for your desired operation
            // For example, you can draw a circle at each point:
            //canvas.drawCircle(x, y, 5f, paint!!)
            slopePoints.add(Point(x.toInt(), y.toInt()))
            distance += 2f
        }


        for (pointInt in slopePoints) {
            val shader: Shader =
                LinearGradient(
                    pointInt.x.toFloat(),
                    (pointInt.y + 6).toFloat(),
                    (pointInt.x + 1).toFloat(),
                    height.toFloat(),
                    Color.parseColor("#4caa5bff"),
                    Color.parseColor("#00aa5bff"),
                    TileMode.CLAMP
                )
            val paint = Paint()
            paint.shader = shader
            canvas.drawRect(
                RectF(
                    pointInt.x.toFloat(),
                    (pointInt.y + 6).toFloat(),
                    (pointInt.x + 1).toFloat(),
                    height.toFloat()
                ), paint
            )
        }


    }

    fun setData(startPos: Int, currentPos: Int, lastPos: Int) {
        startValue = startPos
        currentValue = currentPos
        lastValue = lastPos
        invalidate()
    }

    fun dpToPx(px: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }
}
