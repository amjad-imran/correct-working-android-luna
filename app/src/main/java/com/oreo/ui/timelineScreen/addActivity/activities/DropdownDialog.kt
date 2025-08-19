package com.oreo.ui.timelineScreen.addActivity.activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R

class DropdownDialog(
    private val context: Context,
    private val anchorView: View,
    private val items: List<String>,
    private val onItemSelected: (String) -> Unit
) {

    private var dialog: Dialog? = null

    fun show() {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_dropdown)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)

            window?.attributes?.apply {
                x = location[0]
                y = location[1] + anchorView.height
                gravity = Gravity.TOP or Gravity.START
                width = anchorView.width
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            setupRecyclerView()
            animateIn()
            show()
        }
    }

    private fun Dialog.setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvDropdownItems)
        val adapter = DropdownAdapter(items) { selectedItem ->
            onItemSelected(selectedItem)
            animateOut()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun animateIn() {
        val container = dialog?.findViewById<LinearLayout>(R.id.dropdownContainer)
        container?.apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateOut() {
        val container = dialog?.findViewById<LinearLayout>(R.id.dropdownContainer)
        container?.animate()
            ?.alpha(0f)
            ?.translationY(-50f)
            ?.setDuration(150)
            ?.setInterpolator(AccelerateInterpolator())
            ?.withEndAction {
                dialog?.dismiss()
            }
            ?.start()
    }
}
