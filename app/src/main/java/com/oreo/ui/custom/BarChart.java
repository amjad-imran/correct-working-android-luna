package com.oreo.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.noisefit.luna.R;
import com.noisefit_commans.utils.LOGS;
import com.oreo.data.model.ChartModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Conways on 2017/4/19.
 */

public class BarChart extends View {

    private int bgColor;
    private int bgLeftColor;
    private int bgRightColor;
    private int bgTopColor;
    private int bgBottomColor;

    private int xTextColor;

    private int chartLineColor;
    private float chartLineWidth = 10f;

    private int gridColor;

    private int hCount;

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
    private Paint bgTopSelectedPaint;
    private Path selectedLinePath = new Path();
    private Paint bgBottomPaint;
    private boolean showSelectedIndicator = true;
    private Paint xTextPaint;
    private Paint gridPaint;
    private Paint centerLinePaint;
    private int centerLineColor;
    private int lineNormalColor;
    private int lineSelectColor;
    private float centerLineWidth;

    private Paint chartLinePaint;
    private RectF rectF;
    private ScrollListener onChartScrollChangedListener;
    //    private Path path = new Path();
//    private Path fillPath = new Path();
    private float unitHLenth;
    private float indicatorUnitLength;
    private boolean showAvgValueText = false;
    private int mWith;
    private int mHeight;

    private float offSet;
    private float indicatorOffSet;

    private Rect xTextBounds;

    private List<ChartModel> list = new ArrayList<>();
    private int prefixCount;
    private int suffixCount;
    private float titleWidth = 0f;
    private int maxValue;
    private int minValue;
    private int avgValue;

    public BarChart(Context context) {
        super(context);
        initPaint();
//        updateData();
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
//        updateData();
    }

    public BarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
//        updateData();
    }


    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.BarChart);
        bgColor = ta.getColor(R.styleable.BarChart_bgColor, 0xffffffff);
        bgLeftColor = ta.getColor(R.styleable.BarChart_bgLeftColor, 0xffffffff);
        bgRightColor = ta.getColor(R.styleable.BarChart_bgRightColor, 0xfffffff);
        bgTopColor = ta.getColor(R.styleable.BarChart_bgTopColor, 0xffffffff);
        bgBottomColor = ta.getColor(R.styleable.BarChart_bgBottomColor, 0xffffffff);
        xTextColor = ta.getColor(R.styleable.BarChart_xTextColor, 0xff000000);
        gridColor = ta.getColor(R.styleable.BarChart_gridColor, 0xffff00ff);
        hCount = ta.getInt(R.styleable.BarChart_hCount, 5);
        xMax = ta.getInt(R.styleable.BarChart_xMax, 10);
        xMin = ta.getInt(R.styleable.BarChart_xMin, 0);
        xTextSize = ta.getDimension(R.styleable.BarChart_xTextSize, 8f);
        leftWith = ta.getDimension(R.styleable.BarChart_leftWith, 16f);
        rightWith = ta.getDimension(R.styleable.BarChart_rightWith, 8f);
        bottomWith = ta.getDimension(R.styleable.BarChart_bottomWith, 16f);
        topWith = ta.getDimension(R.styleable.BarChart_topWith, 8f);
        chartLineColor = ta.getColor(R.styleable.BarChart_chartLineColor, 0xff000000);
        chartLineWidth = ta.getDimension(R.styleable.BarChart_chartLineWidth, 10f);
        centerLineWidth = ta.getDimension(R.styleable.BarChart_centerLineWidth, 10f);
        centerLineColor = ta.getColor(R.styleable.BarChart_centerLineColor, 0xff000000);
        lineNormalColor = ta.getColor(R.styleable.BarChart_lineNormalColor, 0x80ffffff);
        lineSelectColor = ta.getColor(R.styleable.BarChart_lineSelectColor, 0x00000000);
        titleWidth = ta.getDimension(R.styleable.BarChart_titleWidth, 0f);
        showSelectedIndicator = ta.getBoolean(R.styleable.BarChart_showSelectedIndicator, true);
        showAvgValueText = ta.getBoolean(R.styleable.LineChart_showAvgValue, false);
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

        bgBottomPaint = new Paint();
        bgBottomPaint.setColor(bgBottomColor);

        xTextPaint = new Paint();
        xTextPaint.setTextSize(xTextSize);
        xTextPaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(gridColor);

        centerLinePaint = new Paint();
        centerLinePaint.setColor(centerLineColor);
        centerLinePaint.setStrokeWidth(centerLineWidth);
        centerLinePaint.setStyle(Paint.Style.STROKE);
        centerLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));

        chartLinePaint = new Paint();
        chartLinePaint.setStrokeWidth(chartLineWidth);
        chartLinePaint.setColor(chartLineColor);
        chartLinePaint.setAntiAlias(true);
        chartLinePaint.setStyle(Paint.Style.STROKE);

        rectF = new RectF();

        xTextBounds = new Rect();
    }



    public void updateDataWithMax(List<ChartModel> datas, List<ChartModel> prefixList, List<ChartModel> suffixList,
                                  int xMax1, int lineNormalColor, int lineSelectColor) {

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
            noneZeroValueCount += 1;

            if (xMax == 0 ) {
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


        if (xMax < 20) {
            xMax += 5;
        } else if (xMax < 100) {
            xMax += 50;
        } else if (xMax < 1000) {
            xMax += 100;
        }




        this.lineNormalColor = lineNormalColor;
        this.lineSelectColor = lineSelectColor;

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWith = w;
        mHeight = h;
        unitHLenth = (mWith - leftWith - rightWith) / hCount;
        offSet = prefixCount * unitHLenth;

        if (titleWidth == 0) {
            indicatorUnitLength = mWith / 3f;
        } else {
            indicatorUnitLength = titleWidth;
        }
        indicatorOffSet = prefixCount * indicatorUnitLength;

        selectedLinePath.moveTo(leftWith + (mWith - leftWith - rightWith) / 2f - 2 * unitHLenth, topWith);
        selectedLinePath.lineTo(leftWith + (mWith - leftWith - rightWith) / 2f - unitHLenth / 5f, topWith);
        selectedLinePath.lineTo(leftWith + (mWith - leftWith - rightWith) / 2f, topWith + unitHLenth / 5f);
        selectedLinePath.lineTo(leftWith + (mWith - leftWith - rightWith) / 2f + unitHLenth / 5f, topWith);
        selectedLinePath.lineTo(leftWith + (mWith - leftWith - rightWith) / 2f + 2 * unitHLenth, topWith);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawTop(canvas);
        drawBottom(canvas);
        drawContent(canvas);
        drawRight(canvas);
        drawLeft(canvas);
    }


    private void drawBg(Canvas canvas) {
        canvas.drawRect(0, 0, mWith, mHeight, bgPaint);
    }

    private void drawTop(Canvas canvas) {
        if (showSelectedIndicator) {
            canvas.drawRect(0, 0, mWith, topWith, bgTopPaint);
            bgTopSelectedPaint.setStyle(Paint.Style.FILL);
            bgTopSelectedPaint.setColor(bgTopColor);
            canvas.drawPath(selectedLinePath, bgTopSelectedPaint);
            bgTopSelectedPaint.setStyle(Paint.Style.STROKE);
            bgTopSelectedPaint.setColor(Color.WHITE);
            bgTopSelectedPaint.setStrokeWidth(dip2px(2));
            canvas.drawPath(selectedLinePath, bgTopSelectedPaint);
        }

    }

    private void drawRight(Canvas canvas) {
        canvas.drawRect(mWith - rightWith, 0, mWith, mHeight, bgRightPaint);
    }

    private void drawBottom(Canvas canvas) {
        canvas.drawRect(0, mHeight - bottomWith, mWith, mHeight, bgBottomPaint);
//        canvas.drawLine(leftWith, mHeight - bottomWith, mWith - rightWith, mHeight - bottomWith,
//                xLinePaint);
    }


    private void drawLeft(Canvas canvas) {
        canvas.drawRect(0, 0, leftWith, mHeight, bgLeftPaint);

        maxValue = xMax;
        minValue = 0;
        String maxStr = String.valueOf(maxValue);
        String minStr = String.valueOf(minValue);
        String avgStr = String.valueOf(avgValue);
        xTextPaint.setColor(xTextColor & 0x80ffffff);
        float max = mHeight - bottomWith - xMax * (mHeight - topWith - bottomWith) / (xMax - xMin);
        canvas.drawLine(leftWith, max, mWith - rightWith, max, gridPaint);
        xTextPaint.getTextBounds(maxStr, 0, maxStr.length(), xTextBounds);
        canvas.drawText(maxStr, mWith - rightWith + dip2px(10), max + xTextBounds.height() / 2f, xTextPaint);

        float min = mHeight - bottomWith - 0 * (mHeight - topWith - bottomWith) / (xMax - xMin);
        canvas.drawLine(leftWith, min, mWith - rightWith, min, gridPaint);
        xTextPaint.getTextBounds(maxStr, 0, maxStr.length(), xTextBounds);
        canvas.drawText(minStr, mWith - rightWith + dip2px(10), min + xTextBounds.height() / 2f, xTextPaint);


        float avg = mHeight - bottomWith - avgValue * (mHeight - topWith - bottomWith) / (xMax - xMin);
        if (avgValue > 0) {
            canvas.drawLine(leftWith, avg, mWith - rightWith, avg, centerLinePaint);
        }

        if (showAvgValueText) {
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
//        float unitHLenth = (mWith - leftWith - rightWith) / hCount;
        int firstPosition = 0;
        float tempOffset = offSet + moveOffSet;
        if (tempOffset > (mWith - leftWith - rightWith) / 2 + unitHLenth) {
            firstPosition = (int) ((tempOffset - (mWith - leftWith - rightWith) / 2) / unitHLenth);
        }
        int lastPosition = Math.min(firstPosition + hCount + 2, list.size());
        ChartModel current;
        for (int i = firstPosition; i < lastPosition; i++) {
            current = list.get(i);
            float x = offSet + moveOffSet + (mWith - leftWith - rightWith) / 2 + leftWith - i * unitHLenth;
            float y = mHeight - bottomWith - current.getValue() * (mHeight - topWith - bottomWith) / (xMax - xMin);
            if (current.getValue() > 0) {
                rectF.left = x - chartLineWidth / 2f;
                rectF.top = y;
                rectF.right = x + chartLineWidth / 2f;
                rectF.bottom = mHeight - bottomWith - chartLineWidth / 2f;
                chartLinePaint.setColor(lineNormalColor);
                canvas.drawRoundRect(rectF, chartLineWidth / 2f, chartLineWidth / 2f, chartLinePaint);
            }
            String xText = list.get(i).getIndex();
            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
            xTextPaint.setColor(xTextColor & 0x80ffffff);
            canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 4, xTextPaint);

            if(showSelectedIndicator) {
                String title = list.get(i).getDate();
                float xInd = indicatorOffSet + (moveOffSet * list.size() * indicatorUnitLength / (list.size() * unitHLenth)) + (mWith - leftWith - rightWith) / 2 + leftWith - i * indicatorUnitLength;
                xTextPaint.getTextBounds(title, 0, title.length(), xTextBounds);
                canvas.drawText(title, xInd - xTextBounds.width() / 2f, topWith / 2 + xTextBounds.height() / 2f, xTextPaint);
            }  }
        if ((offSet + moveOffSet) < 0 || (offSet + moveOffSet) > (list.size() - 1) * unitHLenth) {
            return;
        }
        int position = Math.round((offSet + moveOffSet) / unitHLenth);

        float x = (mWith - leftWith - rightWith) / 2 + leftWith;
        float y;
        float temp = (offSet + moveOffSet) % unitHLenth;

        float y1 = mHeight - bottomWith - list.get(position).getValue() * (mHeight - topWith - bottomWith) / (xMax - xMin);
        if (moveOffSet == 0 || (offSet + moveOffSet) == (list.size() - 1) * unitHLenth) {
            y = y1;
        } else {
            float y2 = mHeight - bottomWith - list.get(position + 1).getValue() * (mHeight - topWith - bottomWith) / (xMax - xMin);
            y = y1 + temp * (y2 - y1) / unitHLenth;
        }
//        canvas.drawCircle(x, y, scaleNodeRadius, scaleNodePaint);
        if (moveOffSet == 0) {
            String xText = list.get(position).getIndex();
            xTextPaint.setColor(xTextColor);
            xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
            canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 4, xTextPaint);

            if(showSelectedIndicator) {
                String title = list.get(position).getDate();
                float xInd = indicatorOffSet + (moveOffSet * list.size() * indicatorUnitLength / (list.size() * unitHLenth)) + (mWith - leftWith - rightWith) / 2 + leftWith - position * indicatorUnitLength;
                xTextPaint.getTextBounds(title, 0, title.length(), xTextBounds);
                canvas.drawText(title, xInd - xTextBounds.width() / 2f, topWith / 2 + xTextBounds.height() / 2f, xTextPaint);
            }
            if (list.get(position).getValue() > 0) {
                rectF.left = x - chartLineWidth / 2f;
                rectF.top = y;
                rectF.right = x + chartLineWidth / 2f;
                rectF.bottom = mHeight - bottomWith - chartLineWidth / 2f;
                chartLinePaint.setColor(lineSelectColor);
                canvas.drawRoundRect(rectF, chartLineWidth / 2f, chartLineWidth / 2f, chartLinePaint);
            }
        }
    }


    private float xDown;
    private float moveOffSet;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                xDown = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                moveOffSet = event.getX() - xDown;
                callBack(true);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                offSet += event.getX() - xDown;
                resetData();
                setToUnit();
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
            tempPosition = (int) ((offSet + moveOffSet) / unitH);
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
        if (offSet > (list.size() - 1 - suffixCount) * unitHLenth) {
            offSet = (list.size() - 1 - suffixCount) * unitHLenth;
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
