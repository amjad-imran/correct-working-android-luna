package com.oreo.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.noisefit.luna.R;
import com.noisefit_commans.utils.LOGS;
import com.oreo.data.model.ChartModel;
import com.oreo.data.model.GraphDummyModel;
import com.oreo.data.model.SleepChartModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SleepLineChart extends View {

    private boolean showLowCircle;

    private boolean showHighCircle;


    private int yTextColor;
    private int bgColor;
    private int bgLeftColor;
    private int bgRightColor;
    private int bgTopColor;
    private int bgBottomColor;
    private int xTextColor;
    private int chartLineColor;
    private float chartLineWidth = 10f;

    private int scaleNodeColor;

    private Paint outCirclePaint;
    private int gridColor;
    private int xMax;
    private int xMin;

    private float leftWith;
    private float rightWith;
    private float bottomWith;
    private float topWith;
    private float xTextSize;
    private float noDataSize;
    private float scaleNodeRadius;
    private Paint bgPaint;
    private Paint bgLeftPaint;
    private Paint bgRightPaint;
    private Paint bgTopPaint;
    private Paint bgBottomPaint;

    private Paint xTextPaint;

    private Paint noDataPaint;
    private Paint gridPaint;
    private Paint centerLinePaint;
    private int centerLineColor;
    private int fillColorStart;
    private int fillColorEnd;
    private float centerLineWidth;

    private Paint chartLinePaint;
    private Paint chartLineFillPaint;

    private Paint avgBackPaint;


    private Paint scaleNodePaint;

    private ScrollListener onChartScrollChangedListener;
    private Path path = new Path();
    private Path fillPath = new Path();
    private float unitHLenth;

    private int mWith;
    private int mHeight;

    private Rect xTextBounds;
    private SleepChartModel sleepModel;
    private boolean mHasDummyData = true;
    private List<ChartModel> list = new ArrayList<>();
    private boolean showXAxis = true;

    //    private int xMax;
//    private int xMin;
    private int avgValue;
    private int lastMinValueIndex;
    private int lastMaxValueIndex;

    private int maxValue;
    private int minValue;
    private LinearGradient linearGradient;

    float leftTextEndPos = 0;
    float rightTextStartPos = 0;

    public SleepLineChart(Context context) {
        super(context);
        initPaint();
//        updateData();
    }

    public SleepLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
//        updateData();
    }

    public SleepLineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
//        updateData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawTop(canvas);
        drawBottom(canvas);
        drawRight(canvas);
        drawContent(canvas);
        drawLeft(canvas);
    }


    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SleepLineChart);
        bgColor = ta.getColor(R.styleable.SleepLineChart_bgColor, 0xffffffff);
        bgLeftColor = ta.getColor(R.styleable.SleepLineChart_bgLeftColor, 0xffffffff);
        bgRightColor = ta.getColor(R.styleable.SleepLineChart_bgRightColor, 0xfffffff);
        bgTopColor = ta.getColor(R.styleable.SleepLineChart_bgTopColor, 0xffffffff);
        bgBottomColor = ta.getColor(R.styleable.SleepLineChart_bgBottomColor, 0xffffffff);
        xTextColor = ta.getColor(R.styleable.SleepLineChart_xTextColor, 0xff000000);
        gridColor = ta.getColor(R.styleable.SleepLineChart_gridColor, 0xffff00ff);
        xMax = ta.getInt(R.styleable.SleepLineChart_xMax, 10);
        xMin = ta.getInt(R.styleable.SleepLineChart_xMin, 0);
        xTextSize = ta.getDimension(R.styleable.SleepLineChart_xTextSize, 8f);
        noDataSize = ta.getDimension(R.styleable.SleepLineChart_noDataSize, 12f);
        leftWith = ta.getDimension(R.styleable.SleepLineChart_leftWith, 16f);
        rightWith = ta.getDimension(R.styleable.SleepLineChart_rightWith, 8f);
        bottomWith = ta.getDimension(R.styleable.SleepLineChart_bottomWith, 16f);
        topWith = ta.getDimension(R.styleable.SleepLineChart_topWith, 8f);
        chartLineColor = ta.getColor(R.styleable.SleepLineChart_chartLineColor, 0xff000000);
        chartLineWidth = ta.getDimension(R.styleable.SleepLineChart_chartLineWidth, 10f);
        scaleNodeColor = ta.getColor(R.styleable.SleepLineChart_scaleNodeColor, 0xff000000);
        scaleNodeRadius = ta.getDimension(R.styleable.SleepLineChart_scaleNodeRadius, 3f);
        centerLineWidth = ta.getDimension(R.styleable.SleepLineChart_centerLineWidth, 2f);
        centerLineColor = ta.getColor(R.styleable.SleepLineChart_centerLineColor, 0xff000000);
        fillColorStart = ta.getColor(R.styleable.SleepLineChart_fillColorStart, 0x80ffffff);
        fillColorEnd = ta.getColor(R.styleable.SleepLineChart_fillColorEnd, 0x00000000);
        showXAxis = ta.getBoolean(R.styleable.SleepLineChart_showXAxis, true);
        yTextColor = ta.getColor(R.styleable.LineChart_yTextColor, 0xff000000);
        ta.recycle();
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


        Typeface fontGilroy = ResourcesCompat.getFont(this.getContext(), com.noisefit_commans.R.font.gilroy_medium);
        xTextPaint = new Paint();
        xTextPaint.setTextSize(xTextSize);
        xTextPaint.setTypeface(fontGilroy);
        xTextPaint.setAntiAlias(true);

        noDataPaint = new Paint();
        noDataPaint.setTextSize(noDataSize);
        noDataPaint.setColor(getResources().getColor(R.color.white));
        noDataPaint.setTypeface(fontGilroy);
        noDataPaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(gridColor);

        centerLinePaint = new Paint();
        centerLinePaint.setColor(centerLineColor);
        centerLinePaint.setStrokeWidth(centerLineWidth);
        centerLinePaint.setStyle(Paint.Style.STROKE);
        centerLinePaint.setPathEffect(new DashPathEffect(new float[]{1, 4}, 0));

        chartLinePaint = new Paint();
        chartLinePaint.setStrokeWidth(chartLineWidth);
        chartLinePaint.setColor(chartLineColor);
        chartLinePaint.setAntiAlias(true);
        chartLinePaint.setStyle(Paint.Style.STROKE);

        chartLineFillPaint = new Paint();
//        chartLineFillPaint.setColor(Color.GRAY);
        chartLineFillPaint.setStyle(Paint.Style.FILL);
        chartLineFillPaint.setAntiAlias(true);


        avgBackPaint = new Paint();
        avgBackPaint.setStyle(Paint.Style.FILL);
        avgBackPaint.setColor(Color.parseColor("#07121e"));
        avgBackPaint.setAntiAlias(true);


        scaleNodePaint = new Paint();
        scaleNodePaint.setColor(scaleNodeColor);
        scaleNodePaint.setAntiAlias(true);

        xTextBounds = new Rect();
    }


    public void updateGraphColor(int chartLineColor, int fillColorStart, int fillColorEnd) {
        this.chartLineColor = chartLineColor;
        this.fillColorEnd = fillColorEnd;
        this.fillColorStart = fillColorStart;

        chartLinePaint = new Paint();
        chartLinePaint.setStrokeWidth(chartLineWidth);
        chartLinePaint.setColor(chartLineColor);
        chartLinePaint.setAntiAlias(true);
        chartLinePaint.setStyle(Paint.Style.STROKE);

        outCirclePaint = new Paint();
        outCirclePaint.setColor(chartLineColor);
        outCirclePaint.setAntiAlias(true);

    }

    public void updateDataWithMax(SleepChartModel datas, int maxOffset,
                                  boolean showHighCircle, boolean showLowCircle, GraphDummyModel dummy, Integer averageValue) {
        sleepModel = datas;
        mHasDummyData = dummy.getHasDummyData();
        list.clear();
        list.addAll(sleepModel.getList());

        this.showLowCircle = showLowCircle;
        this.showHighCircle = showHighCircle;
        maxValue = 0;
        minValue = 0;
        Collections.reverse(list);

        ChartModel item;
        int sum = 0;
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            item = list.get(i);
            if (item.getValue() == 0) {
                continue;
            }
            sum += item.getValue();
            count += 1;
            if (maxValue == 0 && minValue == 0) {
                lastMinValueIndex = i;
                lastMaxValueIndex = i;
                maxValue = item.getValue();
                minValue = item.getValue();
            }
            if (item.getValue() > maxValue) {
                lastMaxValueIndex = i;
                maxValue = item.getValue();
            }
            if (item.getValue() > 0 && item.getValue() < minValue) {
                lastMinValueIndex = i;
                minValue = item.getValue();
//                LOGS.INSTANCE.d("updateDataminValue " + xMax + " " + xMin);
            }
        }
        if (averageValue == null) {
            if (count > 0) {
                avgValue = sum / count;
            }
        } else {
            avgValue = averageValue;
        }


//        xMax += maxOffset;

        if (mHasDummyData) {
            xMax = dummy.getMax();
            xMin = dummy.getMin();
        } else {
            xMax = maxValue + maxOffset;
            xMin = minValue - maxOffset;
        }
        LOGS.INSTANCE.d("updateData " + xMax + " " + xMin + " " + minValue + " " + maxValue);
        if (xMin < 0) {
            xMin = 0;
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
        linearGradient = new LinearGradient(0, 0, 0, h, new int[]{fillColorStart, fillColorEnd}, new float[]{0.3f, 0.6f}, Shader.TileMode.CLAMP);
        mWith = w;
        mHeight = h;


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
            String endTime = "";
//            if(sleepModel!=null && sleepModel.getEndTime() != null){
//                endTime = sleepModel.getEndTime();
//            }
//            String xText = endTime;
//            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
//            xTextPaint.setColor(Color.parseColor("#ffffff"));
//            canvas.drawText(xText, mWith - rightWith - xTextBounds.width() - dip2px(5), mHeight - bottomWith / 4, xTextPaint);
//
//            String startTime = "";
//            if(sleepModel!=null && sleepModel.getStartTime() != null){
//                startTime = sleepModel.getStartTime();
//            }
//
//            xText = startTime;
//            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
//            canvas.drawText(xText, leftWith + dip2px(5), mHeight - bottomWith / 4, xTextPaint);
        }
    }


    private void drawLeft(Canvas canvas) {
        canvas.drawRect(0, 0, leftWith, mHeight, bgLeftPaint);
        String maxStr = String.valueOf(xMax);
        String minStr = String.valueOf(xMin);
        String avgStr = String.valueOf(avgValue);
        float max = mHeight - bottomWith - (xMax - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);
        canvas.drawLine(leftWith, max, mWith - rightWith, max, gridPaint);
        xTextPaint.setColor(Color.parseColor("#a3ffffff"));
        xTextPaint.getTextBounds(maxStr, 0, maxStr.length(), xTextBounds);
        canvas.drawText(maxStr, mWith - rightWith + dip2px(10), max + xTextBounds.height() / 2f, xTextPaint);

        float min = mHeight - bottomWith - (xMin - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);
        canvas.drawLine(leftWith, min, mWith - rightWith, min, gridPaint);
        xTextPaint.getTextBounds(maxStr, 0, maxStr.length(), xTextBounds);
        canvas.drawText(minStr, mWith - rightWith + dip2px(10), min + xTextBounds.height() / 2f, xTextPaint);


        if (!mHasDummyData) {
            float avg = mHeight - bottomWith - (avgValue - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);
            canvas.drawLine(leftWith, avg, mWith - rightWith, avg, centerLinePaint);
            xTextPaint.getTextBounds(avgStr, 0, avgStr.length(), xTextBounds);
            xTextPaint.setColor(Color.WHITE);

            float width = xTextPaint.measureText(avgStr);
            float padding = dip2px(2);
            canvas.drawRect(leftWith + dip2px(5) - padding, avg - dip2px(18),
                    leftWith + dip2px(5) + width + padding, avg - dip2px(4),
                    avgBackPaint);
            canvas.drawText(avgStr, leftWith + dip2px(5), avg - xTextBounds.height(), xTextPaint);

        } else {
            String noDataText = "No data available";
            float textWidth = noDataPaint.measureText(noDataText);

            float textX = (mWith - leftWith) / 2 - textWidth / 2;
            float textY = (mHeight / 2) + dip2px(4);
            canvas.drawText(noDataText, textX, textY, noDataPaint);

            canvas.drawLine(leftWith, mHeight / 2, textX - dip2px(8), mHeight / 2, gridPaint);
            canvas.drawLine(textX + textWidth + dip2px(8), mHeight / 2, mWith - rightWith, mHeight / 2, gridPaint);
        }

    }

    private void drawContent(Canvas canvas) {
        if (null == list || list.size() == 0) {
            return;
        }

        unitHLenth = (mWith - leftWith - rightWith) / (list.size() - 1);

//        Collections.reverse(list);


        int firstPosition = 0;

        int lastPosition = list.size();
        float leftTextEndPos = 0f;
        float endTextStartPos = 0f;

        ChartModel current, next;
        for (int i = 0; i < list.size(); i++) {

            current = list.get(i);
            float x = (mWith - leftWith - rightWith) + leftWith - i * unitHLenth;
            float y = mHeight - bottomWith - (current.getValue() - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);
            path.reset();
            fillPath.reset();
            path.moveTo(x, y);


            if (i < list.size() - 1) {
                next = list.get(i + 1);

                if (current.getValue() > 0 && next.getValue() > 0) {

                    float x1 = (mWith - leftWith - rightWith) + leftWith - (i + 1) * unitHLenth;
                    float y1 = mHeight - bottomWith - (next.getValue() - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);
                    path.cubicTo(x1 + (x - x1) / 4, y, x - (x - x1) / 4, y1, x1, y1);
                    fillPath.addPath(path);
                    //draw fill first
                    fillPath.lineTo(x1, mHeight - bottomWith);
                    fillPath.lineTo(x, mHeight - bottomWith);
                    chartLineFillPaint.setShader(linearGradient);
                    canvas.drawPath(fillPath, chartLineFillPaint);
                    //draw chart line second, need to cover fill color
                    canvas.drawPath(path, chartLinePaint);
                }


            }
            if (current.getValue() > 0) {
                if (i == firstPosition) {

                    next = list.get(i + 1);
                    if (next.getValue() == 0) {
                        canvas.drawCircle(x, y, 1f, outCirclePaint);
                    }
                } else if (i == lastPosition - 1) {
                    ChartModel pre = list.get(i - 1);
                    if (pre.getValue() == 0) {
                        canvas.drawCircle(x, y, 1f, outCirclePaint);

                    }
                } else {
                    ChartModel pre = list.get(i - 1);
                    next = list.get(i + 1);
                    if (pre.getValue() == 0 && next.getValue() == 0) {
                        canvas.drawCircle(x, y, 1f, outCirclePaint);
                    }
                }

            }


            if (showXAxis) {
                if (list.get(i) != null && list.get(i).getIndex() != null && !list.get(i).getIndex().isEmpty()) {
                    String xText = list.get(i).getIndex();
                    xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);


                    if (endTextStartPos == 0f) {
                        String text = list.get(0).getIndex();
                        xTextPaint.setColor(Color.parseColor("#ffffff"));
                        if(text!=null){
                            endTextStartPos = mWith - leftWith - xTextPaint.measureText(text);
                        }else {
                            endTextStartPos = mWith - leftWith - xTextPaint.measureText("00:00 am");
                        }
                    }

                    if(leftTextEndPos==0f){
                        String lastText = list.get(list.size()-1).getIndex();
                        xTextPaint.setColor(Color.parseColor("#ffffff"));
                        leftTextEndPos = leftWith+ xTextPaint.measureText(lastText);
                    }


                    if (i == 0) {
                        xTextPaint.setColor(Color.parseColor("#ffffff"));
                        canvas.drawText(xText, x - xTextBounds.width(), mHeight - bottomWith / 4, xTextPaint);
                        //leftTextEndPos = xTextPaint.measureText(xText);
                    } else if (i == list.size() - 1) {
                        xTextPaint.setColor(Color.parseColor("#ffffff"));
                        canvas.drawText(xText, x, mHeight - bottomWith / 4, xTextPaint);
                    } else {
                        if (leftTextEndPos < (x - (xTextBounds.width() / 2f) - dip2px(6))
                                && (x + xTextBounds.width()) < endTextStartPos) {
                            xTextPaint.setColor(xTextColor & 0x80ffffff);
                            canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 4, xTextPaint);
                        }
                    }


                }
            }


            if (showLowCircle && !mHasDummyData) {
                if (i == lastMinValueIndex) {
//
//                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dot_circle_graph);
//                    canvas.drawBitmap(bmp, x, y, null); // 24 is the height of image

                    canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
                }
            }

            if (showHighCircle && !mHasDummyData) {
                if (i == lastMaxValueIndex) {
//                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dot_circle_graph);
//                    canvas.drawBitmap(bmp, x, y , null); // 24 is the height of image
                    canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
                }
            }


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
