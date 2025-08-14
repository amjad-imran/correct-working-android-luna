package com.oreo.ui.timelineScreen.addActivity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.animation.addListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.DialogActivitySelectorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import kotlin.math.roundToInt

@AndroidEntryPoint
class ActivitySelectorDialog : DialogFragment(){

    private var _binding: DialogActivitySelectorBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()

    private val activitiesListAdapter by lazy {
        ActivitiesListAdapter() {
            sharedViewModel.loadFragmentByType(it.type)
        }
    }

    companion object {
        fun newInstance(anchorY: Int): ActivitySelectorDialog {
            val fragment = ActivitySelectorDialog()
            val args = Bundle()
            args.putInt("anchorY", anchorY)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogActivitySelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setRecycler()

        val recyclerParent = binding.rvActivities.parent as? ViewGroup
        recyclerParent?.post {
            animateDown()
        }

    }

    private fun setRecycler() {
        binding.rvActivities.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = activitiesListAdapter
        }
        activitiesListAdapter.updateDataSet(sharedViewModel.getAllActivityListMap())
    }

    private fun animateDown() {
        val dialogWindow = dialog?.window ?: return

        val decorView = dialogWindow.decorView

        decorView.post {
            decorView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val finalHeight = decorView.measuredHeight

            val heightAnimator = ValueAnimator.ofInt(0, finalHeight)
            heightAnimator.addUpdateListener { animation ->
                val newHeight = animation.animatedValue as Int
                val layoutParams = dialogWindow.attributes
                layoutParams.height = newHeight
                dialogWindow.attributes = layoutParams
            }

            val fadeInAnimator = ObjectAnimator.ofFloat(decorView, "alpha", 1f)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(heightAnimator, fadeInAnimator)
            animatorSet.duration = 300
            animatorSet.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        val yOffset = arguments?.getInt("anchorY") ?: 0
        val dialogWindow = dialog?.window ?: return


        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        dialogWindow.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        val decorView = dialogWindow.decorView
        val params = dialogWindow.attributes
        params.y = yOffset
        dialogWindow.attributes = params

        dialogWindow.setBackgroundDrawableResource(R.drawable.inset_dialog_background)


        decorView.alpha = 0f

        decorView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val dialogView = dialog?.findViewById<View>(android.R.id.content)
                if (dialogView != null) {
                    val rect = Rect()
                    dialogView.getGlobalVisibleRect(rect)

                    if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        dismissWithSlideUp()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    fun dismissWithSlideUp() {
        val decorView = dialog?.window?.decorView ?: run {
            dismiss()
            return
        }

        decorView.post {
            decorView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val finalHeight = decorView.measuredHeight
            val dialogWindow = dialog?.window

            if (dialogWindow == null) {
                dismiss()
                return@post
            }

            val heightAnimator = ValueAnimator.ofInt(finalHeight, 0)
            heightAnimator.addUpdateListener { animation ->
                val newHeight = animation.animatedValue as Int
                val layoutParams = dialogWindow.attributes
                layoutParams.height = newHeight
                dialogWindow.attributes = layoutParams
            }

            val fadeInAnimator = ObjectAnimator.ofFloat(decorView, "alpha", 0f)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(heightAnimator, fadeInAnimator)
            animatorSet.duration = 300
            animatorSet.start()

            animatorSet.addListener(onEnd = {
                dismiss()
            })
        }
    }
}