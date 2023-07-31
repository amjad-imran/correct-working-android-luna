package com.oreo.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;


public class CustomView extends View {

    private Paint paint = new Paint();
    private Path path = new Path();

    {
        paint.setColor(Color.BLUE);
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, 300, Color.BLUE, Color.YELLOW, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setStyle(Paint.Style.FILL);
    }

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#80EDEDED"));
        path.moveTo(200, 0);
        path.lineTo(0, 200);
        path.lineTo(100, 300);
        path.lineTo(500, 500);

//        path.addCircle(200, 200, 200f, Path.Direction.CCW);
        canvas.drawPath(path, paint);

    }
}
