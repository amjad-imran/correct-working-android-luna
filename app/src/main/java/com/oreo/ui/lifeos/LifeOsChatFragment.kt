package com.oreo.ui.lifeos

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsAnimationCompat
import com.google.android.material.transition.MaterialContainerTransform
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsChatBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsChatFragment :
    BaseFragment<FragmentLifeOsChatBinding>(FragmentLifeOsChatBinding::inflate) {

    private var previousSoftInputMode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure no relayout when IME shows; set early
        val window = requireActivity().window
        if (previousSoftInputMode == null) previousSoftInputMode = window.attributes.softInputMode
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.o_nav_host_fragment
            duration = 300
            scrimColor = Color.TRANSPARENT
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.o_nav_host_fragment
            duration = 250
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = binding.lytChatBox.chatEtx
        editText.requestFocus()

        setupImeAnimation()

        val showIme: () -> Unit = {
            // Prefer WindowInsetsController on newer APIs
            ViewCompat.getWindowInsetsController(view)?.show(WindowInsetsCompat.Type.ime())
                ?: run {
                    requireContext().getSystemService<InputMethodManager>()?.showSoftInput(
                        editText,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }
        }

        val transition = (sharedElementEnterTransition as? Transition)
        if (transition != null) {
            transition.addListener(object : TransitionListenerAdapter() {
                override fun onTransitionStart(transition: Transition) {
                    // IME parallel open with a tiny delay to avoid first-frame jump
                    view.postDelayed({
                        showIme()
                        kickstartImeTranslation()
                    }, 60)
                }
                override fun onTransitionEnd(transition: Transition) {
                    // Keep ADJUST_NOTHING; translation is handled by insets animation
                }
            })
        } else {
            // No transition, show immediately
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            view.post {
                showIme()
                kickstartImeTranslation()
            }
        }
    }

    

    private fun setupImeAnimation() {
        val inputContainer = binding.lytChatBox.root // translate only chat box, not full screen
        val root = requireView()

        // Apply base system bar padding; we drive IME with translation
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, sysBars.bottom)

            // Also apply final position for static states (IME shown/hidden)
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            inputContainer.translationY = -imeBottom.toFloat()
            insets
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            root,
            object : WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    inputContainer.translationY = -imeBottom.toFloat()
                    return insets
                }
            }
        )
    }

    // Fast-path to set translation as soon as IME insets become available
    private fun kickstartImeTranslation() {
        val root = view ?: return
        var attempts = 0
        val runnable = object : Runnable {
            override fun run() {
                val insets = ViewCompat.getRootWindowInsets(root)
                val ime = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
                if (ime > 0) {
                    binding.lytChatBox.root.translationY = -ime.toFloat()
                } else if (attempts++ < 12) { // ~200ms max @16ms
                    root.postDelayed(this, 16)
                }
            }
        }
        root.post(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Restore previous soft input behavior
        previousSoftInputMode?.let { requireActivity().window.setSoftInputMode(it) }
        previousSoftInputMode = null
    }
}
