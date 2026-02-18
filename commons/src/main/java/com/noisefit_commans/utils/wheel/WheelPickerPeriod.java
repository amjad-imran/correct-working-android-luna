package com.noisefit_commans.utils.wheel;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import com.noisefit_commans.R;


public class WheelPickerPeriod extends View implements Runnable {
    private static final String TAG = "WheelPicker";
    private static final boolean DEBUG = false;


    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SCROLLING = 2;

    private static final int QUICK_SCROLL_VELOCITY = 10000;
    private static final int TOUCH_SLOP = 4;

    private final Handler handler = new Handler();

    private final Paint textPaint;
    private final Paint selectedTextPaint;
    private final Paint suffixTextPaint;

    private Scroller scroller;
    private VelocityTracker tracker;

    /**
     * Determines whether the current scrolling animation is triggered by touchEvent or setSelectedItemPosition.
     * User added eventListeners will only be fired after touchEvents.
     */
    private boolean isTouchTriggered;

    private OnWheelChangeListener onWheelChangeListener;

    private final Rect rectItem = new Rect();
    private final Rect rectCurrentItem = new Rect();

    private final Rect rectContent = new Rect();
    //	private final Rect rectIcon = new Rect();
    private final Rect rectText = new Rect();

    private WheelAdapterPeriod<?> adapter;

    private String maxWidthText;

    private int visibleItemCount;
    private int drawnItemCount;

    private int textMaxWidth;
    private int textMaxHeight;

    private final int textColor;
    private final int textColorSelected;

    private final int drawableSize;
    private final int drawablePadding;

    private final int itemHeight;

    private int selectedItemPosition;
    private int currentItemPosition;

    private int minFlingY;
    private int maxFlingY;

    private int minimumVelocity;
    private int maximumVelocity;

    private int wheelCenterY;
    private int drawnCenterY;

    private int scrollOffsetY;

    private int textMaxWidthPosition;

    private int lastPointY;
    private int downPointY;

    private boolean hasSameWidth;
    private boolean hasAtmospheric;

    private boolean isClick;
    private boolean isForceFinishScroll;

    public WheelPickerPeriod(Context context) {
        this(context, null);
    }

    public WheelPickerPeriod(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WheelPicker);

        ColorStateList colorStateList = getColorStateList(typedArray, R.styleable.WheelPicker_android_textColor);
        if (colorStateList == null) {
            colorStateList = ColorStateList.valueOf(Color.BLACK);
        }

        Typeface font =
                ResourcesCompat.getFont(this.getContext(), com.noisefit_commans.R.font.google_sans_flex_medium);

        this.textColor = colorStateList.getColorForState(View.EMPTY_STATE_SET, Color.BLACK);
        this.textColorSelected = colorStateList.getColorForState(View.SELECTED_STATE_SET, Color.BLACK);

        int textSize = typedArray.getDimensionPixelSize(R.styleable.WheelPicker_android_textSize, spToPx(12));
        int textSizeSuffix = typedArray.getDimensionPixelSize(R.styleable.WheelPicker_textSizeSuffix, spToPx(18));

        itemHeight = typedArray.getDimensionPixelSize(R.styleable.WheelPicker_wheel_item_height, dpToPx(50));
        selectedItemPosition = typedArray.getInt(R.styleable.WheelPicker_wheel_selected_item_position, 0);
        hasSameWidth = typedArray.getBoolean(R.styleable.WheelPicker_wheel_same_width, false);
        textMaxWidthPosition = typedArray.getInt(R.styleable.WheelPicker_wheel_maximum_width_text_position, -1);
        maxWidthText = typedArray.getString(R.styleable.WheelPicker_wheel_maximum_width_text);
        drawableSize = typedArray.getDimensionPixelSize(R.styleable.WheelPicker_wheel_drawableSize, 0);
        drawablePadding = typedArray.getDimensionPixelOffset(R.styleable.WheelPicker_android_drawablePadding, 0);
        hasAtmospheric = typedArray.getBoolean(R.styleable.WheelPicker_wheel_atmospheric, false);
        typedArray.recycle();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColorFilter(new PorterDuffColorFilter(textColor, PorterDuff.Mode.SRC_IN));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(font);

        selectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        selectedTextPaint.setTextSize(textSize);
        selectedTextPaint.setTextAlign(Paint.Align.CENTER);
        selectedTextPaint.setColorFilter(new PorterDuffColorFilter(textColorSelected, PorterDuff.Mode.SRC_IN));
        selectedTextPaint.setStyle(Paint.Style.FILL);
        selectedTextPaint.setTypeface(font);


        suffixTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        suffixTextPaint.setTextSize(textSizeSuffix);
        suffixTextPaint.setTextAlign(Paint.Align.CENTER);
        suffixTextPaint.setColorFilter(new PorterDuffColorFilter(textColorSelected, PorterDuff.Mode.SRC_IN));
        suffixTextPaint.setStyle(Paint.Style.FILL);
        suffixTextPaint.setTypeface(font);


        // Correct sizes of text
        computeTextSize();

        scroller = new Scroller(getContext());

        ViewConfiguration conf = ViewConfiguration.get(getContext());
        minimumVelocity = conf.getScaledMinimumFlingVelocity();
        maximumVelocity = conf.getScaledMaximumFlingVelocity();
    }

    private void updateVisibleItemCount() {
        /*if (visibleItemCount < 2) {
            throw new ArithmeticException("Wheel's visible item count can not be less than 2!");
        }*/

        // Be sure count of visible item is odd number
        if (visibleItemCount % 2 == 0) {
            visibleItemCount += 1;
        }
        drawnItemCount = visibleItemCount + 2;
    }

    private void computeTextSize() {
        textMaxWidth = textMaxHeight = 0;
        if (adapter != null && adapter.getSize() != 0) {
            /*if (hasSameWidth) {
                textMaxWidth = (int) textPaint.measureText(String.valueOf(adapter.getData().get(0).getData()));
            } else if (isPosInRange(textMaxWidthPosition)) {
                textMaxWidth = (int) textPaint.measureText
                        (String.valueOf(adapter.getData().get(textMaxWidthPosition)));
            } else if (!TextUtils.isEmpty(maxWidthText)) {
                textMaxWidth = (int) textPaint.measureText(maxWidthText);
            } else {
                for (Object obj : adapter.getData()) {
                    String text = String.valueOf(obj);
                    int width = (int) textPaint.measureText(text);
                    textMaxWidth = Math.max(textMaxWidth, width);
                }
            }*/

            textMaxWidth = (int) textPaint.measureText("100");
        }

        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        textMaxHeight = (int) (metrics.bottom - metrics.top);

        updateContentPositions();
    }

    private void updateContentPositions() {
        int contentWidth = textMaxWidth + drawablePadding + drawableSize;

        rectContent.set(
                rectItem.centerX() - contentWidth / 2,
                rectItem.top,
                rectItem.centerX() + contentWidth / 2,
                rectItem.bottom
        );

//		rectIcon.set(
//				rectContent.left,
//				rectContent.top,
//				rectContent.left + drawableSize,
//				rectContent.bottom
//		);

        rectText.set(
                rectContent.right - textMaxWidth,
                rectContent.top,
                rectContent.right,
                rectContent.bottom
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // Correct sizes of original content
        int resultWidth = textMaxWidth + drawableSize;
        int resultHeight = textMaxHeight * visibleItemCount;

        if (DEBUG) {
            Log.i(TAG, "Wheel's content size is (" + resultWidth + ":" + resultHeight + ")");
        }

        // Consideration padding influence the view sizes
        resultWidth += getPaddingLeft() + getPaddingRight();
        resultHeight += getPaddingTop() + getPaddingBottom();

        if (DEBUG) {
            Log.i(TAG, "Wheel's size is (" + resultWidth + ":" + resultHeight + ")");
        }

        // Consideration sizes of parent can influence the view sizes
        resultWidth = measureSize(modeWidth, sizeWidth, resultWidth);
        resultHeight = measureSize(modeHeight, sizeHeight, resultHeight);

        setMeasuredDimension(resultWidth, resultHeight);
    }

    private int measureSize(int mode, int sizeExpect, int sizeActual) {
        int realSize;
        if (mode == MeasureSpec.EXACTLY) {
            realSize = sizeExpect;
        } else {
            realSize = sizeActual;
            if (mode == MeasureSpec.AT_MOST) {
                realSize = Math.min(realSize, sizeExpect);
            }
        }
        return realSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        // Set content region
        rectItem.set(
                getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom()
        );

        if (DEBUG) {
            Log.i(TAG, "Wheel's drawn rect size is (" + rectItem.width() + ":" + rectItem.height() + ") and location is (" + rectItem.left + ":" + rectItem.top + ")");
        }

        // Get the center coordinates of content region
        wheelCenterY = rectItem.centerY();

        // Correct item drawn center
        drawnCenterY = (int) (wheelCenterY - ((textPaint.ascent() + textPaint.descent()) / 2));

        visibleItemCount = rectItem.height() / itemHeight;
        updateVisibleItemCount();

        // Initialize fling max Y-coordinates
        computeFlingLimitY();

        // Correct region of current select item
        computeCurrentItemRect();

        updateContentPositions();
    }

    private void computeFlingLimitY() {
        int currentItemOffset = currentItemPosition * itemHeight;
        minFlingY = -itemHeight * (adapter.getSize() - 1) + currentItemOffset;
        maxFlingY = currentItemOffset;
    }

    private void computeCurrentItemRect() {
        if (textColor != textColorSelected) {
            rectCurrentItem.set(rectItem.left, wheelCenterY - itemHeight / 2, rectItem.right, wheelCenterY + itemHeight / 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int drawnDataStartPos = -scrollOffsetY / itemHeight - drawnItemCount / 2;
        for (int drawnDataPos = drawnDataStartPos + selectedItemPosition,
             drawnOffsetPos = -drawnItemCount / 2;
             drawnDataPos < drawnDataStartPos + selectedItemPosition + drawnItemCount;
             drawnDataPos++, drawnOffsetPos++) {

            String data = "";
//			Bitmap icon = null;

            if (isPosInRange(drawnDataPos)) {
                WheelItemPeriod item = adapter.getData().get(drawnDataPos);
                data = String.valueOf(item);
//				icon = item.getIcon();
            }


            int mDrawnItemCenterY = drawnCenterY + (drawnOffsetPos * itemHeight) +
                    scrollOffsetY % itemHeight;

            if (hasAtmospheric) {
                int alpha = (int) (Math.pow((drawnCenterY - Math.abs(drawnCenterY - mDrawnItemCenterY)) * 1.0F / drawnCenterY, 2) * 255);
                alpha = alpha < 0 ? 0 : alpha;
                textPaint.setAlpha(alpha);
                suffixTextPaint.setAlpha(alpha);
            }

            int iconTop = 0;

//			if (icon != null) {
//				iconTop = wheelCenterY + (drawnOffsetPos * itemHeight) + scrollOffsetY % itemHeight - icon.getHeight() / 2;
//			}

            // Judges need to draw different color for current item or not

            canvas.save();
            canvas.clipRect(rectItem);

//				if (icon != null) {
//					canvas.drawBitmap(icon, rectIcon.left, iconTop, textPaint);
//				}


            float textStartMain = (float) getWidth() / 2;

            canvas.drawText(data, rectText.centerX(), mDrawnItemCenterY, textPaint);
            //if (drawnDataPos == selectedItemPosition) {

            if (!data.isEmpty()) {
                float textWidth = textPaint.measureText(data);
                float textStart = rectText.centerX() + textWidth;

                canvas.drawText("days", textStart, mDrawnItemCenterY, suffixTextPaint);
            }


            //}
            canvas.restore();

            if (DEBUG) {
                canvas.save();
                canvas.clipRect(rectItem);
                textPaint.setColor(0xFFEE3333);
                int lineCenterY = wheelCenterY + (drawnOffsetPos * itemHeight);
                canvas.drawLine(rectItem.left, lineCenterY, rectItem.right, lineCenterY, textPaint);
                textPaint.setColor(0xFF3333EE);
                textPaint.setStyle(Paint.Style.STROKE);
                int top = lineCenterY - itemHeight / 2;
                canvas.drawRect(rectItem.left, top, rectItem.right, top + itemHeight, textPaint);
                canvas.restore();
            }
        }

        if (DEBUG) {
            textPaint.setColor(0x4433EE33);
            textPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, getPaddingLeft(), getHeight(), textPaint);
            canvas.drawRect(0, 0, getWidth(), getPaddingTop(), textPaint);
            canvas.drawRect(getWidth() - getPaddingRight(), 0, getWidth(), getHeight(), textPaint);
            canvas.drawRect(0, getHeight() - getPaddingBottom(), getWidth(), getHeight(), textPaint);
        }
    }

    private boolean isPosInRange(int position) {
        return position >= 0 && position < adapter.getSize();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouchTriggered = true;
                isClick = true;
                if (null != getParent()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (null == tracker) {
                    tracker = VelocityTracker.obtain();
                } else {
                    tracker.clear();
                }
                tracker.addMovement(event);
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                    isForceFinishScroll = true;
                }
                downPointY = lastPointY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(downPointY - event.getY()) < TOUCH_SLOP) {
                    break;
                }
                isClick = false;
                tracker.addMovement(event);
                if (null != onWheelChangeListener) {
                    onWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING);
                }

                // Scroll WheelPicker's content
                float move = event.getY() - lastPointY;
                if (Math.abs(move) < 1) {
                    break;
                }
                scrollOffsetY += move;
                lastPointY = (int) event.getY();
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                if (null != getParent()) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                if (isClick && !isForceFinishScroll) {
                    float clickY = event.getY();
                    float difference = clickY - drawnCenterY;
                    if (clickY > drawnCenterY) {
                        difference += itemHeight / 2.0;
                    } else {
                        difference -= itemHeight / 2.0;
                    }

                    int newSelectedPosition = (int) (difference / itemHeight) + currentItemPosition;
                    if (currentItemPosition != newSelectedPosition && newSelectedPosition >= 0 && newSelectedPosition < adapter.getSize()) {
                        setSelectedItemPosition(newSelectedPosition);
                    }
                    break;
                }
                tracker.addMovement(event);

                tracker.computeCurrentVelocity(1000, maximumVelocity);

                // Judges the WheelPicker is scroll or fling base on current velocity
                isForceFinishScroll = false;
                int velocity = (int) tracker.getYVelocity();

                if (Math.abs(velocity) > minimumVelocity && !(scrollOffsetY > maxFlingY) && !(scrollOffsetY < minFlingY)) {
                    if (Math.abs(velocity) > QUICK_SCROLL_VELOCITY) {
                        int dy = 0;

                        if (velocity > 0) {
                            dy = maxFlingY + computeDistanceToEndPoint(maxFlingY % itemHeight);
                        }
                        if (velocity < 0) {
                            dy = minFlingY + computeDistanceToEndPoint(minFlingY % itemHeight);
                        }

                        scroller.startScroll(0, scrollOffsetY, 0, dy, 300);
                    } else {
                        scroller.fling(0, scrollOffsetY, 0, velocity, 0, 0, minFlingY, maxFlingY);
                        scroller.setFinalY(scroller.getFinalY() +
                                computeDistanceToEndPoint(scroller.getFinalY() % itemHeight));
                    }
                } else {
                    scroller.startScroll(0, scrollOffsetY, 0,
                            computeDistanceToEndPoint(scrollOffsetY % itemHeight));
                }

                // Correct coordinates
                if (scroller.getFinalY() > maxFlingY) {
                    scroller.setFinalY(maxFlingY);
                } else if (scroller.getFinalY() < minFlingY) {
                    scroller.setFinalY(minFlingY);
                }

                handler.post(this);
                if (null != tracker) {
                    tracker.recycle();
                    tracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (null != getParent()) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                if (null != tracker) {
                    tracker.recycle();
                    tracker = null;
                }
                break;
        }
        return true;
    }

    private int computeDistanceToEndPoint(int remainder) {
        if (Math.abs(remainder) > itemHeight / 2) {
            if (scrollOffsetY < 0) {
                return -itemHeight - remainder;
            } else {
                return itemHeight - remainder;
            }
        } else {
            return -remainder;
        }
    }

    @Override
    public void run() {
        if (null == adapter || adapter.getSize() == 0) {
            return;
        }

        if (scroller.isFinished() && !isForceFinishScroll) {
            if (itemHeight == 0) {
                return;
            }

            int position = (-scrollOffsetY / itemHeight + selectedItemPosition) % adapter.getSize();
            position = position < 0 ? position + adapter.getSize() : position;
            int difference = scrollOffsetY % itemHeight;

            if (DEBUG) {
                Log.i(TAG, position + ":" + adapter.getData().get(position) + ":" + scrollOffsetY);
            }

            currentItemPosition = position;
            if (adapter != null) {
                adapter.onItemSelected(position);
            }
            if (null != onWheelChangeListener && isTouchTriggered) {
                onWheelChangeListener.onWheelSelected(position);
                onWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_IDLE);
            }

            if (difference != 0) {
                scroller.startScroll(0, scrollOffsetY, 0, -difference);
            } else {
                setSelectedItemPosition(position, false);
            }
        }
        if (scroller.computeScrollOffset()) {
            if (null != onWheelChangeListener) {
                onWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING);
            }
            scrollOffsetY = scroller.getCurrY();
            postInvalidate();
            handler.postDelayed(this, 16);
        }
    }

    /**
     * Get the count of current visible items in WheelPicker
     */
    public int getVisibleItemCount() {
        return visibleItemCount;
    }

    /**
     * Set the count of current visible items in WheelPicker
     * The count of current visible items in WheelPicker must greater than 1
     * Notice:count of current visible items in WheelPicker will always is an odd number, even you
     * can set an even number for it, it will be change to an odd number eventually
     * By default, the count of current visible items in WheelPicker is 7
     */
    public void setVisibleItemCount(int count) {
        visibleItemCount = count;
        updateVisibleItemCount();
        requestLayout();
    }

    int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    void setSelectedItemPosition(int position) {
        setSelectedItemPosition(position, true);
    }

    void setSelectedItemPosition(int position, final boolean animated) {
        isTouchTriggered = false;
        if (animated && scroller.isFinished()) { // We go non-animated regardless of "animated" parameter if scroller is in motion
            int itemDifference = position - currentItemPosition;
            if (itemDifference == 0) {
                return;
            }

            scroller.startScroll(0, scroller.getCurrY(), 0, (-itemDifference) * itemHeight);
            handler.post(this);
        } else {
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }
            position = Math.min(position, adapter.getSize() - 1);
            position = Math.max(position, 0);
            selectedItemPosition = position;
            currentItemPosition = position;
            scrollOffsetY = 0;
            scroller = newScroller(scrollOffsetY);
            computeFlingLimitY();
            requestLayout();
            invalidate();
        }
    }

    int getCurrentItemPosition() {
        return currentItemPosition;
    }

    void setAdapter(WheelAdapterPeriod adapter) {
        if (null == adapter) {
            throw new NullPointerException("WheelAdapter can not be null!");
        }
        this.adapter = adapter;

        if (adapter.getSize() > 0 && (selectedItemPosition > this.adapter.getSize() - 1 || currentItemPosition > this.adapter.getSize() - 1)) {
            selectedItemPosition = currentItemPosition = this.adapter.getSize() - 1;
        } else {
            selectedItemPosition = currentItemPosition;
        }
        scrollOffsetY = -itemHeight * selectedItemPosition;
        scroller = newScroller(scrollOffsetY);
        computeTextSize();
        computeFlingLimitY();
        requestLayout();
        invalidate();
    }

    /**
     * Set items of WheelPicker if has same width
     * WheelPicker will traverse the data source to calculate each data text width to find out the
     * maximum text width for the final view width, this process maybe spends a lot of time and
     * reduce efficiency when data source has large amount data, in most large amount data case,
     * data text always has same width, you can call this method tell to WheelPicker your data
     * source has same width to save time and improve efficiency.
     * Sometimes the data source you set is positively has different text width, but maybe you know
     * the maximum width text's position in data source, then you can call
     * {@link #setMaximumWidthTextPosition(int)} tell to WheelPicker where is the maximum width text
     * in data source, WheelPicker will calculate its width base on this text which found by
     * position. If you don't know the position of maximum width text in data source, but you have
     * maximum width text, you can call {@link #setMaximumWidthText} tell to WheelPicker
     * what maximum width text is directly, WheelPicker will calculate its width base on this text.
     */
    public void setSameWidth(boolean hasSameWidth) {
        this.hasSameWidth = hasSameWidth;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * Whether items has same width or not
     */
    public boolean hasSameWidth() {
        return hasSameWidth;
    }

    public void setOnWheelChangeListener(OnWheelChangeListener listener) {
        onWheelChangeListener = listener;
    }

    public String getMaximumWidthText() {
        return maxWidthText;
    }

    public void setMaximumWidthText(String text) {
        if (null == text) {
            throw new NullPointerException("Maximum width text can not be null!");
        }
        maxWidthText = text;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * Get the position of maximum width text in data source
     */
    public int getMaximumWidthTextPosition() {
        return textMaxWidthPosition;
    }

    /**
     * Set the position of maximum width text in data source
     */
    public void setMaximumWidthTextPosition(int position) {
        if (!isPosInRange(position)) {
            throw new ArrayIndexOutOfBoundsException("Maximum width text Position must in [0, " +
                    adapter.getSize() + "), but current is " + position);
        }
        textMaxWidthPosition = position;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * Set whether WheelPicker has atmospheric or not
     * WheelPicker's items will be transparent from center to ends if atmospheric display
     */
    public void setAtmospheric(boolean hasAtmospheric) {
        this.hasAtmospheric = hasAtmospheric;
        invalidate();
    }

    /**
     * Whether WheelPicker has atmospheric or not
     */
    public boolean hasAtmospheric() {
        return hasAtmospheric;
    }

    public Typeface getTypeface() {
        if (null != textPaint) {
            return textPaint.getTypeface();
        }
        return null;
    }

    public void setTypeface(Typeface tf) {
        if (null != textPaint) {
            textPaint.setTypeface(tf);
        }
        if (selectedTextPaint != null) {
            selectedTextPaint.setTypeface(tf);
        }
        computeTextSize();
        requestLayout();
        invalidate();
    }

    public interface OnWheelChangeListener {
        /**
         * Invoke when WheelPicker scroll stopped
         * WheelPicker will return a distance offset which between current scroll position and
         * initial position, this offset is a positive or a negative, positive means WheelPicker is
         * scrolling from bottom to top, negative means WheelPicker is scrolling from top to bottom
         *
         * @param offset Distance offset which between current scroll position and initial position
         */
        void onWheelScrolled(int offset);

        /**
         * Invoke when WheelPicker scroll stopped
         * This method will be called when WheelPicker stop and return current selected item data's
         * position in list
         *
         * @param position Current selected item data's position in list
         */
        void onWheelSelected(int position);

        /**
         * Invoke when WheelPicker's scroll state changed
         * The state of WheelPicker always between idle, dragging, and scrolling, this method will
         * be called when they switch
         *
         * @param state State of WheelPicker, only one of the following
         *              {@link WheelPickerPeriod#SCROLL_STATE_IDLE}
         *              Express WheelPicker in state of idle
         *              {@link WheelPickerPeriod#SCROLL_STATE_DRAGGING}
         *              Express WheelPicker in state of dragging
         *              {@link WheelPickerPeriod#SCROLL_STATE_SCROLLING}
         *              Express WheelPicker in state of scrolling
         */
        void onWheelScrollStateChanged(int state);
    }

    private int dpToPx(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getContext().getResources().getDisplayMetrics());
    }

    private int spToPx(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getContext().getResources().getDisplayMetrics());
    }

    private Scroller newScroller(int currentOffsetY) {
        Scroller scroller = new Scroller(getContext());
        scroller.setFinalY(currentOffsetY);
        scroller.abortAnimation();
        return scroller;
    }

    public ColorStateList getColorStateList(TypedArray typedArray, int index) {
        if (typedArray.hasValue(index)) {
            final int resourceId = typedArray.getResourceId(index, 0);
            if (resourceId != 0) {
                final ColorStateList value = AppCompatResources.getColorStateList(getContext(), resourceId);
                if (value != null) {
                    return value;
                }
            }
        }
        return typedArray.getColorStateList(index);
    }
}