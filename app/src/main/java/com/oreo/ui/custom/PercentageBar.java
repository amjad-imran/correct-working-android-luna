package com.oreo.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.noisefit.luna.R;

public class PercentageBar extends View {

    private Paint paint;
    private int lineColor;
    private int percentageColor;
    private float interval;
    private int max;
    private int percentage;
    private float lineWidth = dp2px(1);
    private float cornerLine = dp2px(10);
    private float cornerPercentage = dp2px(2);

    private float intervalReal;
    private float percentageIndex;

    private int count;
    private RectF rectF;


    public PercentageBar(Context context) {
        super(context);
        initPaint();
    }

    public PercentageBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PercentageBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public PercentageBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.PercentageBar);
        lineColor = ta.getColor(R.styleable.PercentageBar_lineColor, Color.GRAY);
        percentageColor = ta.getColor(R.styleable.PercentageBar_percentageColor, Color.RED);
        interval = ta.getDimension(R.styleable.PercentageBar_interval, dp2px(2));
        max = ta.getInt(R.styleable.PercentageBar_max, 100);
        percentage = ta.getInt(R.styleable.PercentageBar_percentage, 50);
        lineWidth = ta.getDimension(R.styleable.PercentageBar_lineWidth, dp2px(2));
        ta.recycle();
        initPaint();
    }


    private void initPaint() {

        paint = new Paint();
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(lineWidth);
        paint.setAntiAlias(true);

        rectF = new RectF();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        count = (int) Math.ceil((getWidth() / interval) / 2);

        intervalReal = getWidth() * 1f / count;

        percentageIndex = (int) Math.ceil(count * (percentage * 1f / max));


    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawContent(canvas);
    }

    private void drawContent(Canvas canvas) {
        Log.d("5555", "drawContent" + count + "  " + intervalReal);
        paint.setColor(lineColor);
        for (int i = 0; i < count; i++) {
            if (i > percentageIndex) {
                rectF.left = i * intervalReal;
                rectF.top = 0;
                rectF.right = rectF.left + lineWidth;
                rectF.bottom = getHeight();
                canvas.drawRoundRect(rectF, cornerLine, cornerLine, paint);
            }
        }

        paint.setColor(percentageColor);
        rectF.left = 0;
        rectF.top = 0;
        rectF.right = percentageIndex * intervalReal + lineWidth;
        rectF.bottom = getHeight();
        canvas.drawRoundRect(rectF, cornerPercentage, cornerPercentage, paint);
    }


    private int dp2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public int getPercentageColor() {
        return percentageColor;
    }

    public void setPercentageColor(int percentageColor) {
        this.percentageColor = percentageColor;
    }

    public float getInterval() {
        return interval;
    }

    public void setInterval(float interval) {
        this.interval = interval;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getCornerLine() {
        return cornerLine;
    }

    public void setCornerLine(float cornerLine) {
        this.cornerLine = cornerLine;
    }

    public float getCornerPercentage() {
        return cornerPercentage;
    }

    public void setCornerPercentage(float cornerPercentage) {
        this.cornerPercentage = cornerPercentage;
    }
}
