package com.oreo.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.noisefit.R;
import com.noisefit_commans.utils.LOGS;
import com.oreo.data.model.CandleChartModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class CandleChart extends View {

    private int bgColor;
    private int bgLeftColor;
    private int bgRightColor;
    private int bgTopColor;
    private int bgBottomColor;

    private int xTextColor;
    private int yTextColor;
    private int xLineColor;
    private int yLineColor;

    private int chartLineColor;
    private float chartLineWidth = 10f;
    private boolean showGrid;
    private int gridColor;
    private int vCount;
    private int hCount;
    private int xMax;
    private int xMin;
    private float leftWith;
    private float rightWith;
    private float bottomWith;
    private float topWith;
    private float xTextSize;
    private float yTextSize;
    private Paint bgPaint;
    private Paint bgLeftPaint;
    private Paint bgRightPaint;
    private Paint bgTopPaint;
    private Paint bgBottomPaint;
    private Paint xTextPaint;
    private Paint yTextPaint;
    private Paint xLinePaint;
    private Paint yLinePaint;
    private Paint gridPaint;
    private Paint chartLinePaint;
    private Paint markPaint;
    private RectF rectF;
    private ScrollListener onChartScrollChangedListener;
    private int mWith;
    private int mHeight;

    private float offSet;
    private Rect yTextBounds;
    private Rect xTextBounds;

    private final List<CandleChartModel> list = new ArrayList<>();


    public CandleChart(Context context) {
        super(context);
        initPaint();
//        updateData();
    }

    public CandleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
//        updateData();
    }

    public CandleChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
//        updateData();
    }


    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CandleChart);
        bgColor = ta.getColor(R.styleable.CandleChart_bgColor, 0xffffffff);
        bgLeftColor = ta.getColor(R.styleable.CandleChart_bgLeftColor, 0xffffffff);
        bgRightColor = ta.getColor(R.styleable.CandleChart_bgRightColor, 0xfffffff);
        bgTopColor = ta.getColor(R.styleable.CandleChart_bgTopColor, 0xffffffff);
        bgBottomColor = ta.getColor(R.styleable.CandleChart_bgBottomColor, 0xffffffff);
        xTextColor = ta.getColor(R.styleable.CandleChart_xTextColor, 0xff000000);
        yTextColor = ta.getColor(R.styleable.CandleChart_yTextColor, 0xff000000);
        xLineColor = ta.getColor(R.styleable.CandleChart_xLineColor, 0xff000000);
        yLineColor = ta.getColor(R.styleable.CandleChart_yTextColor, 0xff000000);
        showGrid = ta.getBoolean(R.styleable.CandleChart_showGrid, false);
        gridColor = ta.getColor(R.styleable.CandleChart_gridColor, 0xffff00ff);
        vCount = ta.getInt(R.styleable.CandleChart_vCount, 8);
        hCount = ta.getInt(R.styleable.CandleChart_hCount, 5);
        xMax = ta.getInt(R.styleable.CandleChart_xMax, 10);
        xMin = ta.getInt(R.styleable.CandleChart_xMin, 0);
        xTextSize = ta.getDimension(R.styleable.CandleChart_xTextSize, 8f);
        yTextSize = ta.getDimension(R.styleable.CandleChart_yTextSize, 8f);
        leftWith = ta.getDimension(R.styleable.CandleChart_leftWith, 16f);
        rightWith = ta.getDimension(R.styleable.CandleChart_rightWith, 8f);
        bottomWith = ta.getDimension(R.styleable.CandleChart_bottomWith, 16f);
        topWith = ta.getDimension(R.styleable.CandleChart_topWith, 8f);
        chartLineColor = ta.getColor(R.styleable.CandleChart_chartLineColor, 0xff000000);
        chartLineWidth = ta.getDimension(R.styleable.CandleChart_chartLineWidth, 10f);
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

        xTextPaint = new Paint();
        xTextPaint.setTextSize(xTextSize);
        xTextPaint.setAntiAlias(true);

        yTextPaint = new Paint();
        yTextPaint.setColor(yTextColor);
        yTextPaint.setTextSize(yTextSize);
        yTextPaint.setAntiAlias(true);

        xLinePaint = new Paint();
        xLinePaint.setColor(xLineColor);
//        xLinePaint.setStrokeWidth(dip2px(1));

        yLinePaint = new Paint();
        yLinePaint.setColor(yLineColor);
//        yLinePaint.setStrokeWidth(dip2px(1f));

        gridPaint = new Paint();
        gridPaint.setColor(gridColor);

        chartLinePaint = new Paint();
        chartLinePaint.setStrokeWidth(chartLineWidth);
        chartLinePaint.setColor(chartLineColor);
        chartLinePaint.setAntiAlias(true);
        chartLinePaint.setStyle(Paint.Style.FILL);

        markPaint = new Paint();
        markPaint.setAntiAlias(true);
        markPaint.setTextSize(xTextSize);

        rectF = new RectF();

        yTextBounds = new Rect();
        xTextBounds = new Rect();
    }

    /**
     * set data points
     *
     * @param datas real data point
     */
    public void updateData(List<CandleChartModel> datas) {
        list.clear();
        list.addAll(datas);
        postInvalidate();
    }

    public int getMax() {
        return xMax;
    }

    public void setMax(int xMax) {
        this.xMax = xMax;
    }

    public void setScrollListener(ScrollListener listener) {
        this.onChartScrollChangedListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWith = w;
        mHeight = h;
        CandleChartModel model;
        boolean hasActivity = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            model = list.get(i);
            if (model.getType() != CandleChartModel.Type.INACTIVE) {
                offSet = (list.size() - 1 - i) * 3 * chartLineWidth;
                hasActivity = true;
                break;
            }
        }
        if (!hasActivity) {

            Calendar calendar = Calendar.getInstance();
            int minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            offSet = ((list.size() - minutes) / 5f) * 3 * chartLineWidth;
            LOGS.INSTANCE.d("DSAdsasdadasdas::12:::  " + offSet);
            if (offSet < 0) {
                offSet = ((list.size() / 2f) * 3 * chartLineWidth);
            }

            LOGS.INSTANCE.d("DSAdsasdadasdas:: " + offSet);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawBottom(canvas);
        drawContent(canvas);
        drawTop(canvas);
        drawRight(canvas);
        drawLeft(canvas);
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
//        canvas.drawLine(leftWith, mHeight - bottomWith, mWith - rightWith, mHeight - bottomWith,
//                xLinePaint);
    }


    private void drawLeft(Canvas canvas) {
        canvas.drawRect(0, 0, leftWith, mHeight, bgLeftPaint);
        canvas.drawLine(leftWith, topWith, leftWith, mHeight - bottomWith, yLinePaint);
        float unitLenth = (mHeight - topWith - bottomWith) / vCount;
        for (int i = 1; i <= vCount; i++) {
//            canvas.drawLine(leftWith, mHeight - bottomWith - i * unitLenth, ((xMin + i * (xMax - xMin) / vCount)) % 5 == 0 ? (leftWith + 2 * scaleLenth) :
//                    (leftWith + scaleLenth), mHeight - bottomWith - i * unitLenth, scalePaint);
            String temp = (xMin + i * (xMax - xMin) / vCount) + "";
            yTextPaint.getTextBounds(temp, 0, temp.length(), yTextBounds);
            canvas.drawText(temp, leftWith - yTextBounds.width() - dip2px(4), mHeight - (bottomWith
                    + i * unitLenth - yTextBounds.height() / 2f), yTextPaint);
        }
    }


    private void drawContent(Canvas canvas) {
        CandleChartModel model;
        for (int i = list.size() - 1; i >= 0; i--) {
            model = list.get(i);

            float x = offSet + moveOffSet + (mWith - leftWith - rightWith) + leftWith - ((list.size() - i - 1) * 3) * chartLineWidth - chartLineWidth;
            float y = (mHeight - bottomWith) * 2 / 3f - (model.getLength() * (mHeight - topWith - bottomWith) / (xMax - xMin)) / 2f;


            if (!model.getBottomLineText().isEmpty()) {

                String xText = model.getBottomLineText();
                xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
                xTextPaint.setColor(xTextColor);
                if (i == 0) {

                    canvas.drawText(xText, x + dip2px(5), mHeight - bottomWith / 4, xTextPaint);
                }else if (i == list.size() - 1) {

                    canvas.drawText(xText, x - xTextBounds.width() - dip2px(5), mHeight - bottomWith / 4, xTextPaint);
                } else {

                    canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 4, xTextPaint);
                }
                canvas.drawLine(x, topWith, x, mHeight - bottomWith, xTextPaint);
            }

//            if ((i + 1) % 48 == 0 || i == 0) {
//                String xText = model.getIndex();
//                xTextPaint.getTextBounds(xText, 0, xText.length(), xTextBounds);
//                xTextPaint.setColor(xTextColor & 0x80ffffff);
//                xTextPaint.setColor(xTextColor);
//                if (i == 0) {
//                    canvas.drawText(xText, x + dip2px(5), mHeight - bottomWith / 4, xTextPaint);
//                } if( i == list.size() - 1){
//                    canvas.drawText(xText, x - xTextBounds.width() - dip2px(5), mHeight - bottomWith / 4, xTextPaint);
//                }else {
//                    canvas.drawText(xText, x - xTextBounds.width() / 2f, mHeight - bottomWith / 4, xTextPaint);
//                }
//                xTextPaint.setColor(Color.parseColor("#FF7A7977"));
//                canvas.drawLine(x, topWith, x, mHeight - bottomWith, xTextPaint);
//            }


            rectF.left = x - chartLineWidth;
            rectF.top = y;
            rectF.right = x + chartLineWidth / 2;
            rectF.bottom = y + (model.getLength() * (mHeight - topWith - bottomWith) / (xMax - xMin));
            chartLinePaint.setColor(model.getColor());
            canvas.drawRoundRect(rectF, chartLineWidth, chartLineWidth, chartLinePaint);
//            if (!TextUtils.isEmpty(model.getMarkText())) {
//                markPaint.setColor(Color.parseColor("#80000000"));
//                markPaint.setStyle(Paint.Style.FILL);
//                canvas.drawCircle(x, y - (mHeight - bottomWith) * 0.2f, 40f, markPaint);
//                markPaint.setColor(Color.parseColor("#ffffff"));
//                markPaint.getTextBounds(model.getMarkText(), 0, model.getMarkText().length(), xTextBounds);
//                canvas.drawText(model.getMarkText(), x - xTextBounds.width() / 2f, y - (mHeight - bottomWith) * 0.2f + xTextBounds.height() / 2f, markPaint);
//            }

        }
    }

    private void drawGrid(Canvas canvas, float unitLenth) {
        if (!showGrid) {
            return;
        }
        for (int i = 1; i <= vCount; i++) {
            canvas.drawLine(leftWith, mHeight - bottomWith - i * unitLenth, mWith - rightWith,
                    mHeight - bottomWith - i * unitLenth, gridPaint);
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
                if (offSet + moveOffSet < 0) {
                    moveOffSet = 0 - offSet;
                } else if (offSet + moveOffSet > (list.size() - 1) * 3 * chartLineWidth - mWith + chartLineWidth * 3) {
                    moveOffSet = (list.size() - 1) * 3 * chartLineWidth - mWith + chartLineWidth * 3 - offSet;
                }
//                callBack();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                offSet += moveOffSet;
                resetData();
//                setToUnit();
//                callBack();
                invalidate();
                break;
            default:
                break;

        }
        return true;
    }


    private int scrollPosition = -1;

    private void callBack() {
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
        onChartScrollChangedListener.onPositionSelected(scrollPosition, list.get(scrollPosition));
    }


    private void resetData() {
        xDown = 0f;
        moveOffSet = 0f;
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
