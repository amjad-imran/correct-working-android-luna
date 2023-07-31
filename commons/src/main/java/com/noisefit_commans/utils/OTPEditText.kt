package com.noisefit_commans.utils


import android.content.ClipboardManager
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import com.noisefit_commans.R


/**
 * This class handles otp input in multiple edittexts.
 * It will move focus to next edittext, if available, when user enters otp.
 * And it will move focus to the previous edittext, if available, when user deletes otp.
 * It will also delegate the paste option, if user long presses and pastes a string into the otp input.
 *
 * **XML attributes**
 *
 * @attr ref your_package_name.R.styleable#OTPView_nextView
 * @attr ref your_package_name.R.styleable#OTPView_prevView
 *
 * @author $|-|!˅@M
 */
class OTPEditText : AppCompatEditText {
    private var nextView: View? = null
        private get() {
            if (field != null) {
                return field
            }
            if (nextViewId != NO_ID && parent is View) {
                field = (parent as View).findViewById(nextViewId)
                return field
            }
            return null
        }
    private var previousView: View? = null
        private get() {
            if (field != null) {
                return field
            }
            if (previousViewId != NO_ID && parent is View) {
                field = (parent as View).findViewById(previousViewId)
                return field
            }
            return null
        }

    // Unfortunately getParent returns null inside the constructor. So we need to store the IDs.
    private var nextViewId = 0
    private var previousViewId = 0
    private var listener: Listener? = null

    interface Listener {
        fun onPaste(s: String?)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    /**
     * Called when a context menu option for the text view is selected.  Currently
     * this will be one of [android.R.id.selectAll], [android.R.id.cut],
     * [android.R.id.copy], [android.R.id.paste] or [android.R.id.shareText].
     *
     * @return true if the context menu item action was performed.
     */
    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id ==  android.R.id.paste) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Examines the item on the clipboard. If getText() does not return null, the clip item contains the
            // text. Assumes that this application can only handle one item at a time.
            val item = clipboard.primaryClip!!.getItemAt(0)

            // Gets the clipboard as text.
            val pasteData = item.text
            if (listener != null && pasteData != null) {
                listener!!.onPaste(pasteData.toString())
                return true
            }
        }
        return super.onTextContextMenuItem(id)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        // If we've gotten focus here
        if (focused && this.text != null) {
            this.setSelection(this.text!!.length)
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.OTPView, 0, 0)
        nextViewId = typedArray.getResourceId(R.styleable.OTPView_nextView, NO_ID)
        previousViewId = typedArray.getResourceId(R.styleable.OTPView_prevView, NO_ID)
        typedArray.recycle()
        setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
            if (event.action != KeyEvent.ACTION_DOWN) {
                return@setOnKeyListener true
            }
            //You can identify which key pressed by checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                this.text = null
                // Back pressed. If we have a previous view. Go to it.
                if (previousView != null) {
                    previousView!!.requestFocus()
                    return@setOnKeyListener true
                }
            }
            false
        }
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1 && nextView != null) {
                    nextView!!.requestFocus()
                } else if (s.isEmpty() && previousView != null) {
                    previousView!!.requestFocus()
                }
            }
        })

        // Android 3rd party keyboards show the copied text into the suggestion box for the user.
        // Users can then simply tap on that suggestion to paste the text on the edittext.
        // But I don't know of any API that allows handling of those paste actions.
        // Below code will try to tell those keyboards to stop showing those suggestion.
        this.inputType = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS or EditorInfo.TYPE_CLASS_NUMBER
    }

    companion object {
        private const val NO_ID = -1
    }
}