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
import android.widget.LinearLayout
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

            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)

            window?.attributes?.apply {
                // Adjust position of the dialog to be aligned with the anchor view
                x = location[0]
                y = location[1] + anchorView.height
                gravity = Gravity.TOP or Gravity.START

                // Set dialog size to wrap content and take up only necessary space
                width = anchorView.width
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            // Adjusting the background of the fragment to be transparent
            window?.setDimAmount(0f)  // This ensures the background is transparent

            setupRecyclerView()
            animateSliderIn() // Change to slider animation
            show()
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

    private fun animateSliderIn() {
        val container = dialog?.findViewById<LinearLayout>(R.id.dropdownContainer)
        container?.apply {
            alpha = 0f
            translationY = 100f  // Start the container below the screen
            animate()
                .alpha(1f)
                .translationY(0f) // Slide up to its normal position
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateSliderOut() {
        val container = dialog?.findViewById<LinearLayout>(R.id.dropdownContainer)
        container?.animate()
            ?.alpha(0f)
            ?.translationY(100f) // Slide down the container out of view
            ?.setDuration(250)
            ?.setInterpolator(AccelerateInterpolator())
            ?.withEndAction {
                dialog?.dismiss()
            }
            ?.start()
    }
}
