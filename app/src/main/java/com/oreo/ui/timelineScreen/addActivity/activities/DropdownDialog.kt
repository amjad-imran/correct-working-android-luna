package com.oreo.ui.timelineScreen.addActivity.activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import androidx.core.graphics.drawable.toDrawable
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum

class DropdownDialog(
    private val context: Context,
    private val anchorView: View,
    private val items: List<AddActivityListTimelineModel>,
    private val onItemSelected: (AddActivityItemsEnum) -> Unit
) {

    private var dialog: Dialog? = null

    fun show() {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_dropdown)
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            window?.setDimAmount(0f)

            // ⬇️ Kill the default bottom-to-top window animation
            window?.setWindowAnimations(0)

            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)

            window?.attributes = window?.attributes?.apply {
                x = location[0]
                val gapInPx = (28 * context.resources.displayMetrics.density).toInt()
                y = location[1] + anchorView.height - gapInPx
                gravity = Gravity.TOP or Gravity.START
                width = anchorView.width
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            setupRecyclerView()
            show()                // ✅ show FIRST
            animateDropdownIn()   // ✅ then animate the content
        }
    }

    private fun Dialog.setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvDropdownItems)
        recyclerView.setBackgroundResource(R.drawable.bg_dropdown_add_log_bs_circadian)
        val adapter = DropdownAdapter(items) { selectedItem ->
            onItemSelected(selectedItem)
            animateSliderOut() // Close dialog on item selection
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun animateDropdownIn() {
        val container = dialog?.findViewById<View>(R.id.dropdownContainer) ?: return
        container.apply {
            alpha = 0f
            scaleY = 0f
            pivotY = 0f // expand from top edge like a dropdown
            animate()
                .alpha(1f)
                .scaleY(1f)
                .setDuration(220)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateSliderOut() {
        val container = dialog?.findViewById<View>(R.id.dropdownContainer) ?: return
        container.animate()
            .alpha(0f)
            .scaleY(0f)
            .setDuration(180)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { dialog?.dismiss() }
            .start()
    }
}
