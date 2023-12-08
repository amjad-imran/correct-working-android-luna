package com.oreo.ui.custom;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.noisefit.luna.R;
import com.noisefit_commans.utils.DistanceUtil;
import com.noisefit_commans.utils.LOGS;
import com.oreo.data.model.ChartModel;

import java.util.ArrayList;
import java.util.List;

public class LineChart extends View {

    private int bgColor;
    private int bgLeftColor;
    private int bgRightColor;
    private int bgTopColor;
    private int bgBottomColor;
    private boolean isDistanceGraph;
    private int xTextColor;
    private int yTextColor;
    private int scaleColor;
    private int xLineColor;
    private int yLineColor;

    private int innerCircleColor;
    private int outCircleColor;

    private int chartLineColor;
    private float chartLineWidth = 10f;

    private int scaleNodeColor;

    private int gridColor;

    private int hCount;

    private int xMax;
    private int xMin;

    private float leftWith;
    private float rightWith;
    private float bottomWith;
    private float topWith;

    private float xTextSize;
    private float yTextSize;

    private float innerCircleRadius;
    private float outCircleRadius;
    private float scaleNodeRadius;

    private boolean showLastCircle;
    private Paint bgPaint;
    private Paint bgLeftPaint;
    private Paint bgRightPaint;
    private Paint bgTopPaint;
    private Paint bgTopSelectedPaint;
    private Path selectedLinePath = new Path();
    private Paint bgBottomPaint;

    private Paint xTextPaint;
    private Paint xLinePaint;
    private Paint gridPaint;
    private Paint centerLinePaint;
    private int centerLineColor;
    private int fillColorStart;
    private int fillColorEnd;
    private float centerLineWidth;
    private Paint innerCirclePaint;
    private Paint outCirclePaint;
    private Paint chartLinePaint;
    private Paint chartLineFillPaint;
    private Paint scaleNodePaint;
    private Bitmap glowDotBitmap;
    private ScrollListener onChartScrollChangedListener;
    private Path path = new Path();
    private Path fillPath = new Path();
    private float unitHLenth;
    private float indicatorUnitLength;

    private boolean showAvgValueText = false;
    private boolean showExtremeLine = false;

    private boolean startFromRight = false;
    private boolean alwaysShowCircle = true;
    private int mWith;
    private int mHeight;

    private float offSet;
    private float indicatorOffSet;

    private Rect xTextBounds;

    private List<ChartModel> list = new ArrayList<>();
    private int prefixCount;
    private int mCurrentPos = -1;
    private int suffixCount;
    private boolean canScroll = true;
    private boolean showXAxis = true;
    private boolean showSelectedIndicator = true;
    private float titleWidth = 0f;
    //    private int maxValue;
//    private int minValue;
    private int avgValue;


    private LinearGradient linearGradient;

    public LineChart(Context context) {
        super(context);
        initPaint();
//        updateData();
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
//        updateData();
    }

    public LineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
//        updateData();
    }


    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LineChart);
        bgColor = ta.getColor(R.styleable.LineChart_bgColor, 0xffffffff);
        bgLeftColor = ta.getColor(R.styleable.LineChart_bgLeftColor, 0xffffffff);
        bgRightColor = ta.getColor(R.styleable.LineChart_bgRightColor, 0xfffffff);
        bgTopColor = ta.getColor(R.styleable.LineChart_bgTopColor, 0xffffffff);
        bgBottomColor = ta.getColor(R.styleable.LineChart_bgBottomColor, 0xffffffff);
        xTextColor = ta.getColor(R.styleable.LineChart_xTextColor, 0xff000000);
        yTextColor = ta.getColor(R.styleable.LineChart_yTextColor, 0xff000000);
        scaleColor = ta.getColor(R.styleable.LineChart_scaleColor, 0xff000000);
        xLineColor = ta.getColor(R.styleable.LineChart_xLineColor, 0xff000000);
        yLineColor = ta.getColor(R.styleable.LineChart_yTextColor, 0xff000000);
        gridColor = ta.getColor(R.styleable.LineChart_gridColor, 0xffff00ff);
        hCount = ta.getInt(R.styleable.LineChart_hCount, 5);
        xMax = ta.getInt(R.styleable.LineChart_xMax, 10);
        xMin = ta.getInt(R.styleable.LineChart_xMin, 0);
        xTextSize = ta.getDimension(R.styleable.LineChart_xTextSize, 8f);
        yTextSize = ta.getDimension(R.styleable.LineChart_yTextSize, 8f);
        leftWith = ta.getDimension(R.styleable.LineChart_leftWith, 16f);
        rightWith = ta.getDimension(R.styleable.LineChart_rightWith, 8f);
        bottomWith = ta.getDimension(R.styleable.LineChart_bottomWith, 16f);
        topWith = ta.getDimension(R.styleable.LineChart_topWith, 8f);
        innerCircleColor = ta.getColor(R.styleable.LineChart_innerCircleColor, 0xffffffff);
        outCircleColor = ta.getColor(R.styleable.LineChart_outCircleColor, 0xff000000);
        innerCircleRadius = ta.getDimension(R.styleable.LineChart_innerCircleRadius, 2f);
        outCircleRadius = ta.getDimension(R.styleable.LineChart_outCircleRadius, 0f);
        chartLineColor = ta.getColor(R.styleable.LineChart_chartLineColor, 0xff000000);
        chartLineWidth = ta.getDimension(R.styleable.LineChart_chartLineWidth, 10f);
        scaleNodeColor = ta.getColor(R.styleable.LineChart_scaleNodeColor, 0xff000000);
        scaleNodeRadius = ta.getDimension(R.styleable.LineChart_scaleNodeRadius, 3f);
        centerLineWidth = ta.getDimension(R.styleable.LineChart_centerLineWidth, 10f);
        centerLineColor = ta.getColor(R.styleable.LineChart_centerLineColor, 0xff000000);
        fillColorStart = ta.getColor(R.styleable.LineChart_fillColorStart, 0x80ffffff);
        fillColorEnd = ta.getColor(R.styleable.LineChart_fillColorEnd, 0x00000000);
        canScroll = ta.getBoolean(R.styleable.LineChart_canScroll, true);
        showXAxis = ta.getBoolean(R.styleable.LineChart_showXAxis, true);
        showSelectedIndicator = ta.getBoolean(R.styleable.LineChart_showSelectedIndicator, true);
        titleWidth = ta.getDimension(R.styleable.LineChart_titleWidth, 0f);
        showAvgValueText = ta.getBoolean(R.styleable.LineChart_showAvgValue, false);
        showExtremeLine = ta.getBoolean(R.styleable.LineChart_showExtremeLine, true);

        startFromRight = ta.getBoolean(R.styleable.LineChart_startFromRight, false);
        alwaysShowCircle = ta.getBoolean(R.styleable.LineChart_alwaysShowCircle, true);
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

        bgTopSelectedPaint = new Paint();
//        bgTopSelectedPaint.setColor(Color.WHITE);
//        bgTopSelectedPaint.setStyle(Paint.Style.STROKE);
//        bgTopSelectedPaint.setStrokeWidth(dip2px(2));

        bgBottomPaint = new Paint();
        bgBottomPaint.setColor(bgBottomColor);


        Typeface fontGilroy = ResourcesCompat.getFont(this.getContext(), com.noisefit_commans.R.font.gilroy_medium);
        xTextPaint = new Paint();
        xTextPaint.setTextSize(xTextSize);
        xTextPaint.setTypeface(fontGilroy);
        xTextPaint.setAntiAlias(true);

        xLinePaint = new Paint();
        xLinePaint.setColor(xLineColor);

        gridPaint = new Paint();
        gridPaint.setColor(gridColor);

        centerLinePaint = new Paint();
        centerLinePaint.setColor(centerLineColor);
        centerLinePaint.setAlpha(100);
        centerLinePaint.setStrokeWidth(centerLineWidth);
        centerLinePaint.setStyle(Paint.Style.STROKE);
        centerLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(innerCircleColor);
        innerCirclePaint.setAntiAlias(true);

        outCirclePaint = new Paint();
        outCirclePaint.setColor(outCircleColor);
        outCirclePaint.setAntiAlias(true);

        chartLinePaint = new Paint();
        chartLinePaint.setStrokeWidth(chartLineWidth);
        chartLinePaint.setColor(chartLineColor);
        chartLinePaint.setAntiAlias(true);
        chartLinePaint.setStyle(Paint.Style.STROKE);

        chartLineFillPaint = new Paint();
//        chartLineFillPaint.setColor(Color.GRAY);
        chartLineFillPaint.setStyle(Paint.Style.FILL);
        chartLineFillPaint.setAntiAlias(true);

        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, com.noisefit_commans.R.drawable.ic_glow_graph);
        glowDotBitmap = Bitmap.createScaledBitmap(bitmap, dip2px(40), dip2px(40), true);

        scaleNodePaint = new Paint();
        scaleNodePaint.setColor(scaleNodeColor);
        scaleNodePaint.setAntiAlias(true);

        xTextBounds = new Rect();
    }

    /**
     * set data points
     *
     * @param datas      real data point
     * @param prefixList placeholder before real data point
     * @param suffixList placeholder after real data point
     */

    public void updateData(List<ChartModel> datas, List<ChartModel> prefixList, List<ChartModel> suffixList) {

        list.clear();
        list.addAll(prefixList);
        list.addAll(datas);
        list.addAll(suffixList);
        prefixCount = prefixList.size();
        suffixCount = suffixList.size();


        ChartModel item;
        int sum = 0;
        int count = 0;
        for (int i = 0; i < datas.size(); i++) {
            item = datas.get(i);
            if (item.getValue() == 0) {
                continue;
            }

            sum += item.getValue();
            count += 1;
        }
        if (count != 0) {
            avgValue = sum / count;
        }


        postInvalidate();
    }

    public void updateChartLineColor() {
        chartLinePaint = new Paint();
        chartLinePaint.setStrokeWidth(chartLineWidth);
        chartLinePaint.setColor(chartLineColor);
        chartLinePaint.setAntiAlias(true);
        chartLinePaint.setStyle(Paint.Style.STROKE);

    }

    public void updateDataWithMax(List<ChartModel> datas, List<ChartModel> prefixList,
                                  List<ChartModel> suffixList, int xMax1, int lineColor,
                                  int fillStartColor, int fillEndColor) {
        list.clear();
        list.addAll(prefixList);
        list.addAll(datas);
        list.addAll(suffixList);
        prefixCount = prefixList.size();
        suffixCount = suffixList.size();
        this.chartLineColor = lineColor;
        this.fillColorStart = fillStartColor;
        this.fillColorEnd = fillEndColor;
        updateChartLineColor();

        xMax = 0;
        ChartModel item;
        int sum = 0;
        int noneZeroValueCount = 0;
        int count = 0;
        for (int i = 0; i < datas.size(); i++) {
            item = datas.get(i);
            if (item.getValue() == 0) {
                continue;
            }
            noneZeroValueCount += 1;
            isDistanceGraph = datas.get(i).isDistanceGraph();
            if (xMax == 0) {
                xMax = item.getValue();

            }
            if (item.getValue() > xMax) {
                xMax = item.getValue();
            }

            sum += item.getValue();
            count += 1;
        }

        if (noneZeroValueCount <= datas.size() / 2) {
            avgValue = 0;
        } else {
            if (count != 0) {
                avgValue = sum / count;
            }
        }

        if (xMax1 == 100) {
            xMax = xMax1;
        } else {
            xMax = xMax + 5;
        }


        LOGS.INSTANCE.d("NonZeroValuesxMax = " + xMax);
        postInvalidate();
    }

    public void updateDataWithMax(List<ChartModel> datas, List<ChartModel> prefixList, List<ChartModel> suffixList,Integer currentPos) {

        list.clear();
        list.addAll(prefixList);
        list.addAll(datas);
        list.addAll(suffixList);
        prefixCount = prefixList.size();
        suffixCount = suffixList.size();

        int noneZeroValueCount = 0;
        xMax = 0;
        ChartModel item;
        int sum = 0;
        int count = 0;
        for (int i = 0; i < datas.size(); i++) {
            item = datas.get(i);
            if (item.getValue() == 0) {
                continue;
            }
            isDistanceGraph = datas.get(i).isDistanceGraph();
            noneZeroValueCount += 1;

            if (xMax == 0) {
                xMax = item.getValue();

            }
            if (item.getValue() > xMax) {
                xMax = item.getValue();
            }

            sum += item.getValue();
            count += 1;
        }

        if (noneZeroValueCount <= datas.size() / 2) {
            avgValue = 0;
        } else {
            if (count != 0) {
                avgValue = sum / count;
            }
        }


//        if (xMax < 20) {
//            xMax += 5;
//        } else if (xMax < 100) {
//            xMax += 50;
//        } else if (xMax < 1000) {
//            xMax += 500;
//        }

        xMax = 120;
        if(currentPos!=-1){
            mCurrentPos = currentPos;
            moveToPosition(currentPos);
        }

        postInvalidate();
    }


    public void updateDataWithMaxMin(List<ChartModel> datas, List<ChartModel> prefixList, List<ChartModel> suffixList, int offSet, boolean showLastCircle) {
        list.clear();
        list.addAll(prefixList);
        list.addAll(datas);
        list.addAll(suffixList);
        prefixCount = prefixList.size();
        suffixCount = suffixList.size();


        xMax = 0;
        ChartModel item;
        int sum = 0;
        int count = 0;
        for (int i = 0; i < datas.size(); i++) {
            item = datas.get(i);
            if (item.getValue() == 0) {
                continue;
            }


            if (xMax == 0) {
                xMax = item.getValue();
            }
            if (xMin == 0) {
                xMin = item.getValue();
            }
            if (item.getValue() > xMax) {
                xMax = item.getValue();
            }

            if (item.getValue() > 0 && item.getValue() < xMin) {
                xMin = item.getValue();
            }

            sum += item.getValue();
            count += 1;
        }
        if (count != 0) {
            avgValue = sum / count;
        }


        this.showLastCircle = showLastCircle;
        xMax += offSet;
        xMin -= offSet;
        if (xMin < 0) {
            xMin = 0;
        }
        LOGS.INSTANCE.d("dsasdasad " + avgValue + " " + xMax + " " + xMin);


        postInvalidate();
    }

    public int getMax() {
        return xMax;
    }

    public void setMax(int xMax) {
        this.xMax = xMax;
    }

    public void setOnChartScrollChangedListener(ScrollListener listener) {
        this.onChartScrollChangedListener = listener;
    }

    public void moveToPosition(int position) {
        LOGS.INSTANCE.w("moveToPosition "+position +"     "+list.size());
        if (position < 0 || position >= list.size()) {
            // Invalid position, do nothing or handle the error as needed
            return;
        }

        // Calculate the offset based on the desired position
        float desiredOffset = position * unitHLenth;
        float desiredIndicatorOffset = position * indicatorUnitLength;

        // Set the offset and indicator offset to move to the desired position
        offSet = desiredOffset;
        indicatorOffSet = desiredIndicatorOffset;

        // Trigger a redraw of the view to reflect the new position
        //invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        linearGradient = new LinearGradient(0, 0, 0, h, fillColorStart, fillColorEnd, Shader.TileMode.CLAMP);
        mWith = w;
        mHeight = h;

        unitHLenth = (mWith - leftWith - rightWith) / hCount;
        if(mCurrentPos!=-1){
           offSet = mCurrentPos* unitHLenth;
        }else {
            offSet = prefixCount * unitHLenth;
        }

        if (titleWidth == 0) {
            indicatorUnitLength = mWith / 2f;
        } else {
            indicatorUnitLength = titleWidth;
        }
        if(mCurrentPos!=-1){
            indicatorOffSet = mCurrentPos * indicatorUnitLength;
        }else {
            indicatorOffSet = prefixCount * indicatorUnitLength;
        }

        selectedLinePath.moveTo(leftWith + (mWith - leftWith - rightWith) / 2f - 2 * unitHLenth, topWith);
        selectedLinePath.lineTo(leftWith + (mWith - leftWith - rightWith) / 2f - unitHLenth / 5f, topWith);
        selectedLinePath.lineTo(leftWith + (mWith - leftWith - rightWith) / 2f, topWith + unitHLenth / 5f);
        selectedLinePath.lineTo(leftWith + (mWith - leftWith - rightWith) / 2f + unitHLenth / 5f, topWith);
        selectedLinePath.lineTo(leftWith + (mWith - leftWith - rightWith) / 2f + 2 * unitHLenth, topWith);
        LOGS.INSTANCE.w("moveToPosition onSizeChanged");

        //callBack(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawTop(canvas);
        drawBottom(canvas);
        drawLeft(canvas);
        drawContent(canvas);
        drawRight(canvas);
    }


    private void drawBg(Canvas canvas) {
        canvas.drawRect(0, 0, mWith, mHeight, bgPaint);
    }

    private void drawTop(Canvas canvas) {

        if (showSelectedIndicator) {
            canvas.drawRect(0, 0, mWith, topWith, bgTopPaint);
//            selectedLinePath.moveTo(mWith / 2f - 2 * unitHLenth, topWith);
//            selectedLinePath.lineTo(mWith / 2f - unitHLenth / 5f, topWith);
//            selectedLinePath.lineTo(mWith / 2f, topWith + unitHLenth / 5f );
//            selectedLinePath.lineTo(mWith / 2f + unitHLenth / 5f, topWith);
//            selectedLinePath.lineTo(mWith / 2f + 2* unitHLenth, topWith);
            bgTopSelectedPaint.setStyle(Paint.Style.FILL);
            bgTopSelectedPaint.setColor(bgTopColor);
            canvas.drawPath(selectedLinePath, bgTopSelectedPaint);
            bgTopSelectedPaint.setStyle(Paint.Style.STROKE);
            bgTopSelectedPaint.setColor(Color.WHITE);
            bgTopSelectedPaint.setStrokeWidth(dip2px(1));
            canvas.drawPath(selectedLinePath, bgTopSelectedPaint);
        }
    }

    private void drawRight(Canvas canvas) {
        canvas.drawRect(mWith - rightWith, 0, mWith, mHeight, bgRightPaint);
    }

    private void drawBottom(Canvas canvas) {
        canvas.drawRect(0, mHeight - bottomWith, mWith, mHeight, bgBottomPaint);
        if (showXAxis) {
            canvas.drawLine(leftWith, mHeight - bottomWith, mWith - rightWith, mHeight - bottomWith,
                    xLinePaint);
        }
    }


    private void drawLeft(Canvas canvas) {

        String maxStr;
        String avgStr;
        if (isDistanceGraph) {
            maxStr = DistanceUtil.INSTANCE.convertMeterToKm(xMax);
            avgStr = DistanceUtil.INSTANCE.convertMeterToKm(avgValue);
        } else {
            maxStr = String.valueOf(xMax);
            avgStr = String.valueOf(avgValue);
        }

        String minStr = String.valueOf(xMin);

        xTextPaint.setColor(xTextColor & 0x80ffffff);
        if (showExtremeLine) {


            float max = mHeight - bottomWith - (xMax - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);
            canvas.drawLine(leftWith, max, mWith, max, gridPaint);
            if (showAvgValueText) {
                xTextPaint.getTextBounds(maxStr, 0, maxStr.length(), xTextBounds);
                canvas.drawText(maxStr, mWith - rightWith - xTextBounds.width() + dip2px(10), max + xTextBounds.height() / 2f + dip2px(10), xTextPaint);

            }
            float min = mHeight - bottomWith - 0 * (mHeight - topWith - bottomWith) / (xMax - xMin);
            canvas.drawLine(leftWith, min, mWith, min, gridPaint);
            if (showAvgValueText) {
                xTextPaint.getTextBounds(maxStr, 0, maxStr.length(), xTextBounds);
                canvas.drawText(minStr, mWith - rightWith + dip2px(10), min + xTextBounds.height() / 2f - dip2px(10), xTextPaint);

            }
        }
        float avg = mHeight - bottomWith - (avgValue - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);

        if (avgValue > 0) {
            canvas.drawLine(leftWith, avg, mWith - rightWith, avg, centerLinePaint);
        }

        if (showAvgValueText && avgValue > 0) {
            xTextPaint.getTextBounds(avgStr, 0, avgStr.length(), xTextBounds);
            canvas.drawText(avgStr, leftWith + dip2px(5), avg - xTextBounds.height(), xTextPaint);
        }


    }

    private void drawContent(Canvas canvas) {
//        float unitVLenth = (mHeight - topWith - bottomWith) / vCount;
//        drawGrid(canvas, unitVLenth);
        if (null == list || list.size() <= 0) {
            return;
        }
        int firstPosition = 0;
        float tempOffset = offSet + moveOffSet;
        float divisor = 0.5f;
        if (!showSelectedIndicator && startFromRight) {
            divisor = (hCount - 1) * 1f / hCount;
        }
        if (tempOffset > (mWith - leftWith - rightWith) * divisor + unitHLenth) {
            firstPosition = (int) ((tempOffset - (mWith - leftWith - rightWith) * divisor) / unitHLenth);
        }
        int lastPosition = Math.min(firstPosition + hCount + 2, list.size());
        ChartModel current, next;
        for (int i = firstPosition; i < lastPosition; i++) {
            current = list.get(i);
            float x = offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - i * unitHLenth;
            float y = mHeight - bottomWith - (current.getValue() - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);
            path.reset();
            fillPath.reset();
            path.moveTo(x, y);
            if (i < list.size() - 1) {
                next = list.get(i + 1);
                if (current.getValue() > 0 && next.getValue() > 0) {
                    float x1 = offSet + moveOffSet + (mWith - leftWith - rightWith) * divisor + leftWith - (i + 1) * unitHLenth;
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
                if (alwaysShowCircle) {

                    canvas.drawCircle(x, y, outCircleRadius, outCirclePaint);
                    canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint);
                } else {
                    if (i == firstPosition) {
                        next = list.get(i + 1);
                        if (next.getValue() == 0) {
                            canvas.drawCircle(x, y, outCircleRadius, outCirclePaint);
                            canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint);
                        }
                    } else if (i == lastPosition - 1) {
                        ChartModel pre = list.get(i - 1);
                        if (pre.getValue() == 0) {
                            canvas.drawCircle(x, y, outCircleRadius, outCirclePaint);
                            canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint);
                        }
                    } else {
                        ChartModel pre = list.get(i - 1);
                        next = list.get(i + 1);
                        if (pre.getValue() == 0 && next.getValue() == 0) {
                            canvas.drawCircle(x, y, outCircleRadius, outCirclePaint);
                            canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint);
                        }
                    }
                }
            }

            if (showXAxis) {
                String xText = list.get(i).getIndex();

                xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
                xTextPaint.setColor(xTextColor & 0x80ffffff);
                canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 3, xTextPaint);
                if (showSelectedIndicator && list.get(i).getFormattedDate() != null) {
                    String title = list.get(i).getFormattedDate();
                    float xInd = indicatorOffSet + (moveOffSet * list.size() * indicatorUnitLength / (list.size() * unitHLenth)) + (mWith - leftWith - rightWith) * divisor + leftWith - i * indicatorUnitLength;
                    xTextPaint.getTextBounds(title, 0, title.length(), xTextBounds);
                    canvas.drawText(title, xInd - xTextBounds.width() / 2f, topWith / 2 + xTextBounds.height() / 2f, xTextPaint);
                }
            }
            canvas.drawLine(x, topWith, x, mHeight - bottomWith, gridPaint);

            if (showLastCircle) {

                if (i == 1 && current.getValue() > 0) {

                    float width = glowDotBitmap.getWidth() / 2;
                    float height = glowDotBitmap.getHeight() / 2;

                    canvas.drawBitmap(glowDotBitmap, x - width, y - height, scaleNodePaint);

                    //canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
                }
            }
        }
//        canvas.drawPath(fillPath, chartLineFillPaint);
        if ((offSet + moveOffSet) < 0 || (offSet + moveOffSet) > (list.size() - 1) * unitHLenth) {
            return;
        }
        //round
        int position = Math.round((offSet + moveOffSet) / unitHLenth);

        float x = (mWith - leftWith - rightWith) * divisor + leftWith;
        float y;

        float y1 = mHeight - bottomWith - (list.get(position).getValue() - xMin) * (mHeight - topWith - bottomWith) / (xMax - xMin);
        if (list.get(position).getValue() > 0 && (moveOffSet == 0 || (offSet + moveOffSet) == (list.size() - 1) * unitHLenth)) {
            y = y1;

            float width = glowDotBitmap.getWidth() / 2;
            float height = glowDotBitmap.getHeight() / 2;

            canvas.drawBitmap(glowDotBitmap, x - width, y - height, scaleNodePaint);
            //canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
        }
        if (moveOffSet == 0 && showXAxis) {
            String xText = list.get(position).getIndex();
            xTextPaint.setColor(xTextColor);
            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
            canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 3, xTextPaint);

            if (showSelectedIndicator && list.get(position).getFormattedDate() != null) {
                String title = list.get(position).getFormattedDate();
                float xInd = indicatorOffSet + (moveOffSet * list.size() * indicatorUnitLength / (list.size() * unitHLenth)) + (mWith - leftWith - rightWith) * divisor + leftWith - position * indicatorUnitLength;
                xTextPaint.getTextBounds(title, 0, title.length(), xTextBounds);
                canvas.drawText(title, xInd - xTextBounds.width() / 2f, topWith / 2 + xTextBounds.height() / 2f, xTextPaint);
            }
        }
    }


    private float xDown;
    private float moveOffSet;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canScroll) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                xDown = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                moveOffSet = event.getX() - xDown;
                LOGS.INSTANCE.w("moveToPosition MotionEvent.ACTION_MOVE");
                callBack(true);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                offSet += event.getX() - xDown;
                resetData();
                setToUnit();
                LOGS.INSTANCE.w("moveToPosition MotionEvent.ACTION_UP");
                callBack(true);
                invalidate();
                break;
            default:
                break;

        }
        return true;
    }


    private int scrollPosition = -1;

    private void callBack(boolean isSelected) {
        if (null == onChartScrollChangedListener || null == list || list.size() <= 0) {
            return;
        }
        float unitH = (mWith - leftWith - rightWith) / hCount;
        int tempPosition;
        if ((offSet + moveOffSet) >= (list.size() - 1) * unitH) {
            tempPosition = list.size() - 1;
        } else if ((offSet + moveOffSet) < 0) {
            tempPosition = 0;
        } else {
            tempPosition = Math.round(((offSet + moveOffSet) / unitH));
        }

        if (scrollPosition == tempPosition) {
            return;
        }


        scrollPosition = tempPosition;
        if (isSelected) {
            onChartScrollChangedListener.onPositionSelected(scrollPosition, list.get(scrollPosition));
        } else {
            onChartScrollChangedListener.onScrolling(scrollPosition, list.get(scrollPosition));
        }
    }


    private void resetData() {
        xDown = 0f;
        moveOffSet = 0f;
    }

    private void setToUnit() {
        if (offSet < prefixCount * unitHLenth) {
            offSet = prefixCount * unitHLenth;
            indicatorOffSet = prefixCount * indicatorUnitLength;
            return;
        }
        if (offSet > (list.size() - suffixCount - 1) * unitHLenth) {
            offSet = (list.size() - suffixCount - 1) * unitHLenth;
            indicatorOffSet = (list.size() - suffixCount - 1) * indicatorUnitLength;
            return;
        }

        float temp = offSet % unitHLenth;
        int position = (int) (temp < unitHLenth / 2 ? offSet / unitHLenth : offSet / unitHLenth + 1);
        offSet = unitHLenth * position;
        indicatorOffSet = indicatorUnitLength * position;
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
