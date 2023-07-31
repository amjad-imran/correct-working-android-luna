package com.noisefit_commans.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.noisefit_commans.R
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class AboutRow
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var tvSettingsTitle: TextView? = null
    var ivSettingsArrow: ImageView? = null
    var ivBadge: ImageView? = null
    var topLine: View? = null
    var view: View? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.row_about, this)
        tvSettingsTitle = this.findViewById(R.id.tvSettingsTitle)
        ivSettingsArrow = this.findViewById(R.id.ivSettingsArrow)
        topLine = this.findViewById(R.id.topLine)
        ivBadge = this.findViewById(R.id.ivBadge)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CustomViewAbout)
        try {
            val text = ta.getString(R.styleable.CustomViewAbout_textAbout)

            tvSettingsTitle?.text = text
            val showArrow = ta.getBoolean(R.styleable.CustomViewAbout_showArrowAbout, true)
            if (!showArrow) {
                ivSettingsArrow?.gone()
            }
            val hideTopLine = ta.getBoolean(R.styleable.CustomViewSettings_hideTopLine, false)
            if (hideTopLine) {
                topLine?.gone()
            }

            val showBadge = ta.getBoolean(R.styleable.CustomViewAbout_showBadge, false)
            if (showBadge) {
                ivBadge?.visible()
            }
            view = this.findViewById(R.id.rowMain)

        } finally {
            ta.recycle()
        }
    }

    fun setText(value: String?) {
        tvSettingsTitle?.text = value
        invalidate()
    }


    fun dpToPx(px: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }


    override fun setOnClickListener(l: OnClickListener?) {
        view?.setOnClickListener(l)
    }

    /**
     * color -> Resource ID
     */
    fun showBadge(showBadge: Boolean, color: Int) {
        if (showBadge) {
            ivBadge?.setBackgroundResource(color)
            ivBadge?.visible()
        } else {
            ivBadge?.gone()
        }
        invalidate()
    }
}