package com.oreo.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import com.noisefit.luna.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StressCombinedChart extends View {

    private int bgColor;
    private int bgLeftColor;
    private int bgRightColor;
    private int bgTopColor;
    private int bgBottomColor;
    private int xTextColor;
    private int restLineColor;
    private int highColor;
    private int mediumColor;
    private int lowColor;
    private float chartLineWidth = 10f;

    private int gridColor;
    private int xMax;
    private int xMin;

    private float leftWith;
    private float rightWith;
    private float bottomWith;
    private float topWith;
    private float xTextSize;
    private Paint bgPaint;
    private Paint bgLeftPaint;
    private Paint bgRightPaint;
    private Paint bgTopPaint;
    private Paint bgBottomPaint;

    private Paint xTextPaint;
    private Paint gridPaint;
    private Paint chartLinePaint;
    private Paint chartLineFillPaint;

    private Path path = new Path();
    private Path fillPath = new Path();
    private float unitHLenth;

    private int mWith;
    private int mHeight;

    private Rect xTextBounds;
    private StressCombineModel combineModel;
    private List<StressCombineModel.Item> list = new ArrayList<>();
    private List<Integer> highlightIndexs = new ArrayList<>();
    private int highlightColor;
    private boolean showXAxis = true;
    private int interval = 0;
    private RectF rectF;
    private LinearGradient linearGradient;
    private LinearGradient chartLineGradient;
    private LinearGradient linearGradientShadow;
    private Map<Integer, Pair<LinearGradient, Bitmap>> resMap;
    private int shadowWidth = dip2px(50);


    public StressCombinedChart(Context context) {
        super(context);
        initPaint();
    }

    public StressCombinedChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public StressCombinedChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CombineLineChart);
        bgColor = ta.getColor(R.styleable.CombineLineChart_bgColor, 0x00000000);
        bgLeftColor = ta.getColor(R.styleable.CombineLineChart_bgLeftColor, 0x00000000);
        bgRightColor = ta.getColor(R.styleable.CombineLineChart_bgRightColor, 0x00000000);
        bgTopColor = ta.getColor(R.styleable.CombineLineChart_bgTopColor, 0x00000000);
        bgBottomColor = ta.getColor(R.styleable.CombineLineChart_bgBottomColor, 0x00000000);
        xTextColor = ta.getColor(R.styleable.CombineLineChart_xTextColor, 0xff000000);
        gridColor = ta.getColor(R.styleable.CombineLineChart_gridColor, 0xffff00ff);
        xMax = ta.getInt(R.styleable.CombineLineChart_xMax, 10);
        xMin = ta.getInt(R.styleable.CombineLineChart_xMin, 0);
        xTextSize = ta.getDimension(R.styleable.CombineLineChart_xTextSize, 8f);
        leftWith = ta.getDimension(R.styleable.CombineLineChart_leftWith, 16f);
        rightWith = ta.getDimension(R.styleable.CombineLineChart_rightWith, 8f);
        bottomWith = ta.getDimension(R.styleable.CombineLineChart_bottomWith, 16f);
        topWith = ta.getDimension(R.styleable.CombineLineChart_topWith, 8f);
        restLineColor = ta.getColor(R.styleable.CombineLineChart_restLineColor, 0xff000000);
        highColor = ta.getColor(R.styleable.CombineLineChart_highColor, 0xff000000);
        mediumColor = ta.getColor(R.styleable.CombineLineChart_mediumColor, 0xff000000);
        lowColor = ta.getColor(R.styleable.CombineLineChart_lowColor, 0xff000000);
        chartLineWidth = ta.getDimension(R.styleable.CombineLineChart_chartLineWidth, 10f);
        showXAxis = ta.getBoolean(R.styleable.CombineLineChart_showXAxis, true);
        ta.recycle();

        resMap = new HashMap<>();
        initPaint();

    }

    private void initPaint() {
        bgPaint = new Paint();
        bgPaint.setColor(bgColor);

        bgLeftPaint = new Paint();
        bgLeftPaint.setColor(bgLeftColor);

        bgRightPaint = new Paint();
        bgRightPaint.setColor(bgRightColor);

        bgTopPaint = new Paint();
        bgTopPaint.setColor(bgTopColor);

        bgBottomPaint = new Paint();
        bgBottomPaint.setColor(bgBottomColor);

        xTextPaint = new Paint();
        xTextPaint.setTextSize(xTextSize);
        xTextPaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(gridColor);

        chartLinePaint = new Paint();
        chartLinePaint.setStrokeWidth(chartLineWidth);
//        chartLinePaint.setColor(restLineColor);
        chartLinePaint.setAntiAlias(true);
        chartLinePaint.setStyle(Paint.Style.STROKE);

        chartLineFillPaint = new Paint();
//        chartLineFillPaint.setColor(Color.GRAY);
        chartLineFillPaint.setStyle(Paint.Style.FILL);
        chartLineFillPaint.setAntiAlias(true);


        xTextBounds = new Rect();
        rectF = new RectF();
    }

    public void updateData(StressCombineModel datas) {
        combineModel = datas;
        list.clear();
        list.addAll(combineModel.getItems());
        Collections.reverse(list);
        interval = (int) (list.size() / 4f);
        postInvalidate();
    }

    public void updateHighlight(List<Integer> indexList, int color) {
        highlightIndexs.clear();
        highlightIndexs.addAll(indexList);
        highlightColor = color;

        if (mHeight > 0) {
            linearGradient = new LinearGradient(0, 0, 0, mHeight - bottomWith, highlightColor, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        }
        postInvalidate();
    }

    public int getMax() {
        return xMax;
    }

    public void setMax(int xMax) {
        this.xMax = xMax;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWith = w;
        mHeight = h;

        chartLineGradient = new LinearGradient(0, topWith, 0, mHeight - bottomWith,
                new int[]{highColor, mediumColor, lowColor},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        linearGradient = new LinearGradient(0, 0, 0, mHeight - bottomWith, highlightColor, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        linearGradientShadow = new LinearGradient(mWith - rightWith - shadowWidth, mHeight / 2f, mWith - rightWith, mHeight / 2f, Color.TRANSPARENT, Color.parseColor("#C0000000"), Shader.TileMode.CLAMP);
        unitHLenth = (mWith - leftWith - rightWith) / (list.size() - 1);

        StressCombineModel.Section section;
        for (int i = 0; i < combineModel.getSections().size(); i++) {
            section = combineModel.getSections().get(i);
            resMap.put(i, new Pair<>(new LinearGradient(0, 0, 0, mHeight / 2f, section.getColor(), Color.TRANSPARENT, Shader.TileMode.CLAMP),
                    BitmapFactory.decodeResource(getResources(), section.getImageRes())
            ));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawTop(canvas);
        drawBottom(canvas);
        drawRight(canvas);
        drawLeft(canvas);
        drawContent(canvas);
        drawDesc(canvas);
    }


    private void drawBg(Canvas canvas) {
        canvas.drawRect(0, 0, mWith, mHeight, bgPaint);
    }

    private void drawTop(Canvas canvas) {
        canvas.drawRect(0, 0, mWith, topWith, bgTopPaint);
    }

    private void drawRight(Canvas canvas) {
        canvas.drawRect(mWith - rightWith, 0, mWith, mHeight, bgRightPaint);
    }

    private void drawBottom(Canvas canvas) {
        canvas.drawRect(0, mHeight - bottomWith, mWith, mHeight, bgBottomPaint);
        if (showXAxis) {
            String xText = "24";
            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
            xTextPaint.setColor(Color.parseColor("#ffffff"));
            canvas.drawText(xText, mWith - rightWith - xTextBounds.width() - dip2px(5), mHeight - bottomWith / 4, xTextPaint);


            xText = "0";
            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
            canvas.drawText(xText, leftWith + dip2px(5), mHeight - bottomWith / 4, xTextPaint);
        }
    }


    private void drawLeft(Canvas canvas) {
        gridPaint.setColor(gridColor);
        canvas.drawLine(leftWith, topWith, mWith - rightWith, topWith, gridPaint);
        canvas.drawLine(leftWith, mHeight - bottomWith, mWith - rightWith, mHeight - bottomWith, gridPaint);

        if (combineModel.getHigh() > 0) {
            float high = (mHeight - bottomWith) - (combineModel.getHigh() * 1f / xMax) * (mHeight - bottomWith - topWith);
            canvas.drawLine(leftWith, high, mWith - rightWith, high, gridPaint);
        }

        if (combineModel.getMedium() > 0) {
            float medium = (mHeight - bottomWith) - (combineModel.getMedium() * 1f / xMax) * (mHeight - bottomWith - topWith);
            canvas.drawLine(leftWith, medium, mWith - rightWith, medium, gridPaint);
        }

    }

    private void drawDesc(Canvas canvas) {

        rectF.left = mWith - rightWith - shadowWidth;
        rectF.top = topWith;
        rectF.right = mWith - rightWith;
        rectF.bottom = mHeight - bottomWith;
        chartLineFillPaint.setShader(linearGradientShadow);
        canvas.drawRect(rectF, chartLineFillPaint);
        float high = 0;
        if (combineModel.getHigh() > 0) {
            high = (mHeight - bottomWith) - (combineModel.getHigh() * 1f / xMax) * (mHeight - bottomWith - topWith);
            String highText = "Stressed";
            xTextPaint.getTextBounds(highText, 0, highText.length(), xTextBounds);
            canvas.drawText(highText, mWith - rightWith - xTextBounds.width() - dip2px(5), (high + topWith) / 2 + xTextBounds.height() / 2f, xTextPaint);
        }

        if (combineModel.getMedium() > 0) {
            float medium = (mHeight - bottomWith) - (combineModel.getMedium() * 1f / xMax) * (mHeight - bottomWith - topWith);
            String mediumText = "Focussed";
            xTextPaint.getTextBounds(mediumText, 0, mediumText.length(), xTextBounds);
            canvas.drawText(mediumText, mWith - rightWith - xTextBounds.width() - dip2px(5), (medium + high) / 2 + xTextBounds.height() / 2f, xTextPaint);
            String lowText = "Calm";
            xTextPaint.getTextBounds(lowText, 0, lowText.length(), xTextBounds);
            canvas.drawText(lowText, mWith - rightWith - xTextBounds.width() - dip2px(5), (medium + mHeight - bottomWith) / 2 + xTextBounds.height() / 2f, xTextPaint);
        }

    }

    private void drawContent(Canvas canvas) {
        if (null == list || list.size() == 0) {
            return;
        }

        int imageSize = dip2px(20);
        for (int i = 0; i < combineModel.getSections().size(); i++) {
            StressCombineModel.Section section = combineModel.getSections().get(i);
            rectF.left = section.getStart() * unitHLenth + leftWith;
            rectF.top = topWith;
            rectF.right = rectF.left + (section.getEnd() - section.getStart()) * unitHLenth;
            rectF.bottom = mHeight / 2f;
            chartLineFillPaint.setShader(resMap.get(i).first);
            canvas.drawRect(rectF, chartLineFillPaint);

            rectF.left = section.getStart() * unitHLenth + leftWith;
            rectF.top = topWith;
            rectF.right = rectF.left + (section.getEnd() - section.getStart()) * unitHLenth;
            rectF.bottom = topWith + dip2px(2);
            gridPaint.setColor(section.getColor());
            canvas.drawRect(rectF, gridPaint);


            rectF.left = (rectF.right + rectF.left) / 2 - imageSize / 2f;
            rectF.top = topWith - imageSize - dip2px(10);
            rectF.right = rectF.left + imageSize;
            rectF.bottom = rectF.top + imageSize;

            canvas.drawBitmap(resMap.get(i).second, null, rectF, null);
        }


        StressCombineModel.Item current, next;
        for (int i = 0; i < list.size(); i++) {
            current = list.get(i);
            float x = (mWith - leftWith - rightWith) + leftWith - i * unitHLenth;
            float y = mHeight - bottomWith - current.getValue() * (mHeight - topWith - bottomWith) / (xMax - xMin);
            path.reset();
            path.moveTo(x, y);
            if (i < list.size() - 1) {
                next = list.get(i + 1);
                if (current.getValue() > 0) {

                    if (next.getValue() > 0) {
                        float x1 = (mWith - leftWith - rightWith) + leftWith - (i + 1) * unitHLenth;
                        float y1 = mHeight - bottomWith - next.getValue() * (mHeight - topWith - bottomWith) / (xMax - xMin);
                        path.cubicTo(x1 + (x - x1) / 4, y, x - (x - x1) / 4, y1, x1, y1);
                        if (highlightIndexs.contains(list.size() - 1 - i)) {
                            fillPath.addPath(path);
                            //draw fill first
                            fillPath.lineTo(x1, mHeight - bottomWith);
                            fillPath.lineTo(x, mHeight - bottomWith);
                            chartLineFillPaint.setShader(linearGradient);
                            canvas.drawPath(fillPath, chartLineFillPaint);
                            chartLinePaint.setColor(highlightColor);
                            fillPath.reset();
                        } else {
                            if (highlightIndexs.isEmpty()) {
                                chartLinePaint.setShader(chartLineGradient);
                            } else {
                                chartLinePaint.setShader(null);
                                chartLinePaint.setColor(restLineColor);
                            }
                        }
                        //draw chart line second, need to cover fill color
                        canvas.drawPath(path, chartLinePaint);
                    } else {
                        if (highlightIndexs.contains(list.size() - 1 - i)) {
                            chartLinePaint.setColor(highlightColor);
                        } else {
                            if (highlightIndexs.isEmpty()) {
                                chartLinePaint.setShader(chartLineGradient);
//                                chartLinePaint.setColor(chartLineColor);
                            } else {
                                chartLinePaint.setShader(null);
                                chartLinePaint.setColor(restLineColor);
                            }
                        }
                        canvas.drawPoint(x, y, chartLinePaint);
                    }

                }
            }

            /*if (showXAxis && i % interval == 0 && i > 0 && i < 4 * interval) {
                String xText = String.valueOf(list.get(i).getIndex());
                xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
                xTextPaint.setColor(xTextColor & 0x80ffffff);
                canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 4, xTextPaint);
            }*/
        }

    }

    private int dip2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
