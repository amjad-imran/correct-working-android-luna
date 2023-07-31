package com.noisefit.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText

@SuppressLint("AppCompatCustomView")
class PostEditText : EditText {
    private var selectionListener: SelectionListener? = null

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        selectionListener?.onSelectionChanged(selStart, selEnd)
    }

    fun setSelectionListener(selectionListener: SelectionListener?) {
        this.selectionListener = selectionListener
    }

    // Support pasting images
    override fun onTextContextMenuItem(id: Int): Boolean {
        /* if (id == R.id.paste) {
             val clipboard = context.getSystemService(
                 ClipboardManager::class.java
             )
             val clip = clipboard.primaryClip
             if (processClipData(clip)) return true
         }*/
        return super.onTextContextMenuItem(id)
    }

    interface SelectionListener {
        fun onSelectionChanged(start: Int, end: Int)
    }
}