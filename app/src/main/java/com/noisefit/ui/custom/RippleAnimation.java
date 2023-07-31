package com.noisefit.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.annotation.Nullable;

public class RippleAnimation extends View {

    private Paint circlePaint;
    int currentPosition = 1;


    public RippleAnimation(Context context) {
        super(context);
    }

    public RippleAnimation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RippleAnimation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RippleAnimation(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor("#6AC5FF"));
        circlePaint.setStrokeWidth(dpToPx(1, this.getContext()));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setAntiAlias(true);
        circlePaint.setDither(true);

        drawRipple(canvas);
    }

    private final int[] radius = {82, 66, 50,34};

    void drawRipple(Canvas canvas) {
        int width = dpToPx(180, this.getContext());


        for (int i = 1; i <= 4; i++) {
            if(i<=currentPosition){
                canvas.drawCircle(width / 2, width / 2, dpToPx(radius[i-1], this.getContext()), circlePaint);
            }
        }

        /*canvas.drawCircle(width / 2, width / 2, dpToPx(82, this.getContext()), circlePaint);

        canvas.drawCircle(width / 2, width / 2, dpToPx(66, this.getContext()), circlePaint);

        canvas.drawCircle(width / 2, width / 2, dpToPx(50, this.getContext()), circlePaint);

        canvas.drawCircle(width / 2, width / 2, dpToPx(34, this.getContext()), circlePaint);*/
    }

    int dpToPx(int dp, Context context) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()
        ));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = dpToPx(180, this.getContext());
        int height = dpToPx(180, this.getContext());
        setMeasuredDimension(width, height);
    }

    public void startAnimation() {
        Anim bounceAnimation = new Anim();
        bounceAnimation.setDuration(400);
        bounceAnimation.setRepeatCount(Animation.INFINITE);
        bounceAnimation.setInterpolator(new LinearInterpolator());
        bounceAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                currentPosition++;
                if (currentPosition > 4) {
                    currentPosition = 1;
                }


            }
        });
        startAnimation(bounceAnimation);
    }

    public void stopAnimation() {
        clearAnimation();
        currentPosition = 1;
    }


    private class Anim extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            invalidate();
        }
    }
}
