package com.noisefit_commans.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.noisefit_commans.R
import com.noisefit_commans.ui.gone


class FeatureRow
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var tvSettingsTitle: TextView? = null
    var tvSettingsDesc: TextView? = null
    var switch: SwitchCompat? = null

    var topLine: View? = null
    var view: View? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.row_title_desc_switch, this)
        tvSettingsTitle = this.findViewById(R.id.tvSmartNotificationLabel)
        switch = this.findViewById(R.id.switchSmartNotification)
        tvSettingsDesc = this.findViewById(R.id.tvSmartNotificationText)
        topLine = this.findViewById(R.id.topLine)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.CustomFeatureRow)
        try {
            val title = ta.getString(R.styleable.CustomFeatureRow_featureTitle)

            tvSettingsTitle?.text = title

            val desc = ta.getString(R.styleable.CustomFeatureRow_featureDesc)
            tvSettingsDesc?.text = desc

            val hideTopLine = ta.getBoolean(R.styleable.CustomFeatureRow_featureHideLine, false)

            if (hideTopLine) {
                topLine?.gone()
            }

            val switchChecked = ta.getBoolean(R.styleable.CustomFeatureRow_featureIsEnabled, false)
            switch?.isChecked = switchChecked

            view = this.findViewById(com.noisefit_commans.R.id.rowMain)

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


    fun setSwitchState(isChecked: Boolean) {
        switch?.isChecked = isChecked
        invalidate()
    }
}