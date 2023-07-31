package com.noisefit.ui.bindingadapter

import android.view.View
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import com.noisefit.R
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.net.Uri
import kotlin.math.roundToInt


object DataBindingAdapters {

    @JvmStatic
    @BindingAdapter("errorText")
    fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
        view.error = errorMessage
    }

    @JvmStatic
    @BindingAdapter("profileImage")
    fun setProfileImage(view: ShapeableImageView, imageUrl: String?) {
        Glide.with(view.context)
            .load(imageUrl)
            .error(R.drawable.ic_default_profile_image)
            .placeholder(R.drawable.ic_default_profile_image)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("profileImageEdit", "localImageUri")
    fun setProfileImageEdit(view: ShapeableImageView, profileImageEdit: String?, localImage: Uri?) {
        if (localImage!=null) {
            view.setImageURI(null)
            view.setImageURI(localImage)
            return
        }
        Glide.with(view.context)
            .load(profileImageEdit)
            .error(R.drawable.ic_default_profile_image)
            .placeholder(R.drawable.ic_default_profile_image)
            .into(view)
    }


    @JvmStatic
    @BindingAdapter("setFeatureVisibility")
    fun getFeatureVisibility(view: View, status: Int) {
        return if (status == 1) {
            view.visible()
        } else {
            view.gone()
        }
    }

    @JvmStatic
    @BindingAdapter("setVisibility")
    fun getVisibilityBoolean(view: View, isVisible: Boolean) {
        return if (isVisible) {
            view.visible()
        } else {
            view.gone()
        }
    }

    @JvmStatic
    @BindingAdapter("android:layout_marginTop")
    fun setMarginTop(view: View, topMargin: Float) {
        val layoutParams = view.layoutParams as MarginLayoutParams
        val scale = view.context.resources.displayMetrics.density

        layoutParams.setMargins(
            layoutParams.leftMargin, (scale * topMargin + 0.5f).roundToInt(),
            layoutParams.rightMargin, layoutParams.bottomMargin
        )
        view.layoutParams = layoutParams
    }

    @JvmStatic
    @BindingAdapter("setSize")
    fun setSize(view: View, value: Float) {
        val layoutParams = view.layoutParams
        val scale = view.context.resources.displayMetrics.density
        layoutParams.height = (scale * value + 0.5f).roundToInt()
        layoutParams.width = (scale * value + 0.5f).roundToInt()
        view.layoutParams = layoutParams
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageResource(imageView: ImageView, resource: Int) {
        imageView.setImageResource(resource)
    }

    @JvmStatic
    @BindingAdapter("android:background")
    fun setImageDrawable(view: View, resource: Int) {
        view.setBackgroundResource(resource)
    }


}