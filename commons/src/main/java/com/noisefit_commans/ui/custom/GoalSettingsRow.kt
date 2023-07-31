package com.noisefit_commans.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.imageview.ShapeableImageView
import com.noisefit_commans.R
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class GoalSettingsRow
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var ivShapeAble: ShapeableImageView? = null
    var ivSettingsImage: ImageView? = null
    var tvSettingsTitle: TextView? = null
    var ivSettingsArrow: ImageView? = null
    var tvStatus: TextView? = null
    var topLine: View? = null
    var view: View? = null

    init {
        init(attrs)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.row_goal_settings, this)
        ivSettingsImage = this.findViewById(R.id.ivSettingsImage)
        ivShapeAble = this.findViewById(R.id.ivBackGround)
        tvSettingsTitle = this.findViewById(R.id.tvSettingsTitle)
        ivSettingsArrow = this.findViewById(R.id.ivSettingsArrow)
        topLine = this.findViewById(R.id.topLine)
        tvStatus = this.findViewById(R.id.tvStatus)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CustomViewSettings)
        try {
            val text = ta.getString(R.styleable.CustomViewSettings_text)
            val drawableId = ta.getResourceId(R.styleable.CustomViewSettings_image, 0)
            if (drawableId != 0) {
                val drawable = AppCompatResources.getDrawable(context, drawableId)
                ivSettingsImage?.setImageDrawable(drawable)
            }
            tvSettingsTitle?.text = text
            val showArrow = ta.getBoolean(R.styleable.CustomViewSettings_showArrow, true)
            if (!showArrow) {
                ivSettingsArrow?.gone()
            }
            val hideLeftArrow = ta.getBoolean(R.styleable.CustomViewSettings_hideLeftIcon, false)
            if (hideLeftArrow) {
                ivSettingsImage?.gone()
                ivShapeAble?.gone()
            }
            val hideTopLine = ta.getBoolean(R.styleable.CustomViewSettings_hideTopLine, false)
            if (hideTopLine) {
                topLine?.gone()
            }

            val status = ta.getString(R.styleable.CustomViewSettings_status)
            tvStatus?.text = status
            view = this.findViewById(R.id.rowMain)

        } finally {
            ta.recycle()
        }
    }

    fun setText(value: String?) {
        tvSettingsTitle?.text = value
        invalidate()
    }


    fun setStatus(enabled: Boolean = false) {

        if (enabled) {
            tvStatus?.text = this.resources.getString(R.string.text_enabled)
            tvStatus?.setTextColor(this.resources.getColor(R.color.text_enabled))
        } else {
            tvStatus?.text = this.resources.getString(R.string.text_disabled)
            tvStatus?.setTextColor(this.resources.getColor(R.color.lightest_gray))
        }
        invalidate()
    }

    fun setStatus(isEdit: Boolean = false, text: String) {
        tvStatus?.text = text
        if (isEdit) {
            tvStatus?.setTextColor(this.resources.getColor(R.color.sleep_score_chart))
            ivSettingsArrow?.visible()
        } else {
            tvStatus?.setTextColor(this.resources.getColor(R.color.white))
            ivSettingsArrow?.gone()
        }
        invalidate()
    }
//    fun showTopLine(value: Boolean) {
//        topLine?.visibility = if (value) {
//            View.VISIBLE
//        } else {
//            View.GONE
//        }
//        invalidate()
//    }

//    fun showNotification(value: Boolean) {
//        ivNotification.visibility = if (value) {
//            View.VISIBLE
//        } else {
//            View.GONE
//        }
//        invalidate()
//    }

    fun dpToPx(px: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }


    override fun setOnClickListener(l: OnClickListener?) {
        view?.setOnClickListener(l)
    }
}