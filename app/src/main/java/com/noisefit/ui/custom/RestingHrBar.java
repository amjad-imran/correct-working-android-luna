package com.noisefit.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class RestingHrBar extends View {

    private int progress = 0;
    private RectF rectBack;
    private RectF track;
    private Paint backRectPaint;
    private Paint trackColor;

    private Paint hr1ValuePaint;
    private Paint hr2ValuePaint;
    private Paint hr3ValuePaint;
    private Paint hr4ValuePaint;
    private Paint hr5ValuePaint;

    private int backgroundColor;


    public RestingHrBar(Context context) {
        super(context);


    }

    public RestingHrBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        rectBack = new RectF(0, 0, this.getWidth(), dpToPx(16, getContext()));
        track = new RectF(dpToPx(6, getContext()),
                dpToPx(7, getContext()),
                this.getWidth() - dpToPx(6, getContext()),
                dpToPx(9, getContext()));

        backRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundColor = Color.parseColor("#262b2f");
        backRectPaint.setColor(backgroundColor);

        trackColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackColor.setColor(Color.parseColor("#171a1d"));

        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#ffffff"));

        hr1ValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hr1ValuePaint.setColor(Color.parseColor("#ff2c52"));

        hr2ValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hr2ValuePaint.setColor(Color.parseColor("#ff6624"));

        hr3ValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hr3ValuePaint.setColor(Color.parseColor("#ffd600"));

        hr4ValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hr4ValuePaint.setColor(Color.parseColor("#3cfe8a"));

        hr5ValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hr5ValuePaint.setColor(Color.parseColor("#e95050"));

    }

    public RestingHrBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public RestingHrBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //TODO move to constructor


        rectBack = new RectF(0, 0, this.getWidth(), dpToPx(16, getContext()));
        track = new RectF(0,
                dpToPx(7, getContext()),
                this.getWidth(),
                dpToPx(9, getContext()));

        canvas.drawRoundRect(rectBack, 10, 10, backRectPaint);
        canvas.drawRoundRect(track, 2, 2, trackColor);

        float maxSubTrackWidth = this.getWidth() / 4;

        if (progress > 1) {
            canvas.drawRect(0.0f,
                    (float) dpToPx(6, getContext()),
                    maxSubTrackWidth,
                    (float) dpToPx(10, getContext()), hr1ValuePaint);
        }

        if (progress > 25) {
            canvas.drawRect(maxSubTrackWidth,
                    (float) dpToPx(6, getContext()),
                    maxSubTrackWidth * 2,
                    (float) dpToPx(10, getContext()), hr2ValuePaint);
        }

        if (progress > 50) {
            canvas.drawRect(maxSubTrackWidth * 2,
                    (float) dpToPx(6, getContext()),
                    maxSubTrackWidth * 3,
                    (float) dpToPx(10, getContext()), hr3ValuePaint);
        }

        if (progress > 75) {
            canvas.drawRect(maxSubTrackWidth * 3,
                    (float) dpToPx(6, getContext()),
                    maxSubTrackWidth * 4,
                    (float) dpToPx(10, getContext()), hr4ValuePaint);
        }

        /*if (progress > 80) {
            canvas.drawRect(maxSubTrackWidth * 4,
                    (float) dpToPx(6, getContext()),
                    maxSubTrackWidth * 5,
                    (float) dpToPx(10, getContext()), hr5ValuePaint);
        }*/

        /*float circleXAxis = dpToPx(6, getContext()) + (progress * trackWidth) / 100;
        canvas.drawCircle(circleXAxis, dpToPx(8, getContext()), dpToPx(5, getContext()), circlePaint);*/

    }

    public void setBackgroundColor(int colorRes){
        backgroundColor = colorRes;
        backRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backRectPaint.setColor(backgroundColor);
        invalidate();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    int dpToPx(int dp, Context context) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()
        ));
    }
}
