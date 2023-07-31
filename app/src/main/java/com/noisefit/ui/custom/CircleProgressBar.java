package com.noisefit.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.noisefit.ui.onboarding.pairing.pair.PairState;

public class CircleProgressBar extends View {

    private int dotRadius = 10;

    //current dot position
    private int dotPosition = 1;

    private final int dotAmount = 20;

    private int circleRadius = 120;
    private int animationTime = 6;

    private Paint defaultPaint;
    private Paint pairingPaint;
    private Paint pairingSuccessPaint;
    private Paint pairingFailedPaint;

    private Paint currentPaint;
    private Canvas canvas;


    public CircleProgressBar(Context context) {
        super(context);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void stopAnimation() {
        clearAnimation();
    }

    public void setState(@NonNull PairState pairState) {
        switch (pairState) {
            case PAIRED: {
                currentPaint = pairingSuccessPaint;
                stopAnimation();
                setStateSuccess();
            }
            break;
            case FAILED: {
                currentPaint = pairingFailedPaint;
                dotPosition = 1;
                stopAnimation();
                setStateFailed();

            }
            break;
            case PAIRING: {
                currentPaint = pairingPaint;
                startAnimation();
            }
            break;
        }

    }

    private void setStateFailed() {
        int angle = 360 / dotAmount;
        for (int i = 1; i <= dotAmount; i++) {
            float x = (float) (circleRadius * (Math.cos((angle * i) * (Math.PI / 180))));
            float y = (float) (circleRadius * (Math.sin((angle * i) * (Math.PI / 180))));
            canvas.drawCircle(x, y, dotRadius, pairingFailedPaint);
        }
        invalidate();
    }

    private void setStateSuccess() {
        int angle = 360 / dotAmount;
        for (int i = 1; i <= dotAmount; i++) {
            float x = (float) (circleRadius * (Math.cos((angle * i) * (Math.PI / 180))));
            float y = (float) (circleRadius * (Math.sin((angle * i) * (Math.PI / 180))));
            canvas.drawCircle(x, y, dotRadius, pairingSuccessPaint);
        }
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //take the point to the center of the screen
        canvas.translate(this.getWidth() / 2, this.getHeight() / 2);
        circleRadius = dpToPx(90, this.getContext());
        dotRadius = dpToPx(2, this.getContext());
        defaultPaint = new Paint();
        defaultPaint.setColor(Color.parseColor("#262b2f"));

        pairingPaint = new Paint();
        pairingPaint.setColor(Color.parseColor("#6AC5FF"));

        pairingSuccessPaint = new Paint();
        pairingSuccessPaint.setColor(Color.parseColor("#51bf76"));

        pairingFailedPaint = new Paint();
        pairingFailedPaint.setColor(Color.parseColor("#e95050"));


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }


        //call create dot method
        this.canvas = canvas;
        createDotInCircle(canvas);
    }

    private void createDotInCircle(Canvas canvas) {
        //angle for each dot angle = (360/number of dots) i.e  (360/10)
        //int angle = 36;
        int angle = 360 / dotAmount;

        for (int i = 1; i <= dotAmount; i++) {

            float x = (float) (circleRadius * (Math.cos((angle * i) * (Math.PI / 180))));
            float y = (float) (circleRadius * (Math.sin((angle * i) * (Math.PI / 180))));

            if (dotPosition < i) {
                canvas.drawCircle(x, y, dotRadius, defaultPaint);
            } else {
                if (currentPaint == null) {
                    currentPaint = pairingPaint;
                }
                canvas.drawCircle(x, y, dotRadius, currentPaint);
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = 0;
        int height = 0;
        width = dpToPx(210, this.getContext());
        height = dpToPx(210, this.getContext());
        setMeasuredDimension(width, height);
    }

    int dpToPx(int dp, Context context) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()
        ));
    }

    private void startAnimation() {
        Anim bounceAnimation = new Anim();
        bounceAnimation.setDuration(100);
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
                dotPosition++;
                if (dotPosition > dotAmount) {
                    dotPosition = 1;
                }


            }
        });
        startAnimation(bounceAnimation);
    }


    private class Anim extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            invalidate();
        }
    }
}