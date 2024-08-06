package com.noisefit_commans.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noisefit_commans.R
import com.noisefit_commans.data.TrinaryActionCallback
import com.noisefit_commans.databinding.LayoutCustomAlertBinding
import com.noisefit_commans.utils.LOGS
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

const val FAB_ANIM_TIME = 500L
abstract class BaseFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {

    lateinit var uiController: UIController
    private var _binding: VB? = null
    val binding get() = _binding!!
    val nullableBinding get() = _binding
    private val job = Job()
    val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    val uCropOptions = UCrop.Options().apply {
        this.setStatusBarColor(Color.parseColor("#000000"))
        this.setToolbarColor(Color.parseColor("#000000"))
        this.setToolbarWidgetColor(Color.parseColor("#FFFFFFFF"))
        this.setToolbarTitle("Crop Image")
    }

    val uCropOptionsWithCompress = UCrop.Options().apply {
        this.setStatusBarColor(Color.parseColor("#000000"))
        this.setToolbarColor(Color.parseColor("#000000"))
        this.setToolbarWidgetColor(Color.parseColor("#FFFFFFFF"))
        this.setToolbarTitle("Crop Image")
        this.setCompressionQuality(50)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.let {
            LOGS.w("FRAGMENT_OPEN ${this.javaClass.simpleName}")
        }
        subscribeObservers()
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //job.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    abstract fun initListener()
    abstract fun subscribeObservers()


    fun navigate(request: NavDeepLinkRequest) {
        with(NavHostFragment.findNavController(this)) {
            try {
                navigate(request)

            } catch (e: Exception) {
                LOGS.w(
                    "BaseFragment",
                    "Navigation with id is in danger. Google please save us -> BaseFragment -> navigate method deeplink"
                )
            }
        }
    }

    fun navigate(destination: NavDirections) = with(NavHostFragment.findNavController(this)) {
        currentDestination?.getAction(destination.actionId)
            ?.let { navigate(destination) } ?: LOGS.w(
            "BaseFragment",
            "Navigation is in danger. Google please save us -> BaseFragment -> navigate method"
        )
    }

    fun navigate(destination: Int, bundle: Bundle? = null) =
        with(NavHostFragment.findNavController(this)) {
            try {
                navigate(destination, bundle)

            } catch (e: Exception) {
                LOGS.w(
                    "BaseFragment",
                    "Navigation with id is in danger. Google please save us -> BaseFragment -> navigate method"
                )
            }

        }

    fun navigateUpSafe() {
        try {
            findNavController().navigateUp()
        } catch (exp: Exception) {
            LOGS.d("Safe navigate up ${exp.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*enterTransition = com.google.android.material.transition.platform.MaterialFadeThrough()
        exitTransition = com.google.android.material.transition.platform.MaterialFadeThrough()*/
        setUIController() // null in production

    }


    fun showBleCallingDialog(
        title: String,
        message: String?,
        otherMessage: String?,
        doNotShow: Boolean,
        ctaText: String,
        callback: TrinaryActionCallback?
    ): AlertDialog {
        var alert: AlertDialog? = null
        val builder =
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
        val layoutCustomAlertBinding: LayoutCustomAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.layout_custom_alert, null, false
        )

        layoutCustomAlertBinding.apply {
            tvTitle.text = title
            tvDesc.text = message
            btnAllow.text = ctaText
            tvDescOther.text = otherMessage
            if (otherMessage.isNullOrEmpty()) {
                tvDescOther.gone()
            } else {
                tvDescOther.visible()
            }
            if (doNotShow) {
                btnDoNotShowAgain.visible()
            }

            btnDoNotShowAgain.setOnClickListener {
                alert?.dismiss()
                callback?.maybe()
            }
            btnAllow.setOnClickListener {
                alert?.dismiss()
                callback?.yes()
            }
            btnCancel.setOnClickListener {
                alert?.dismiss()
                callback?.no()
            }
        }


        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(true)

        alert = builder.create()
        alert.show()
        return alert
    }

    fun setUIController() {

        activity?.let {
//            if (it is OnBoardingActivity) {
            try {
                LOGS.i("ui controller interface")
                uiController = it as UIController
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
            //}
        }
    }

    fun animateItemsDown(view: View, closeView: ImageView) {
        val translateDown = ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            0f,
            closeView.y - view.y
        ).apply {
            this.duration = FAB_ANIM_TIME
        }

        val alpha =
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
                .apply {
                    this.duration = FAB_ANIM_TIME
                }

        val animatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playTogether(translateDown, alpha)
        animatorSet.start()
    }


    fun animateItemsUp(view: View, value: Float) {

        val translateUp = ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            value,
            0f
        ).apply {
            this.duration = FAB_ANIM_TIME
        }
        val alpha =
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                .apply {
                    this.duration = FAB_ANIM_TIME
                }

        val animatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playTogether(translateUp, alpha)
        animatorSet.start()
    }


    fun isFragmentInBackStack(destinationId: Int) =
        try {
            findNavController().getBackStackEntry(destinationId)
            true
        } catch (e: Exception) {
            false
        }
}