package com.oreo.ui.circadianAlignment.onboarding

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.view.View
import androidx.core.graphics.toColorInt
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnboardingCarcadian3Binding
import com.noisefit_commans.ui.BaseFragment

class OnboardingCarcadianFragment3 : BaseFragment<FragmentOnboardingCarcadian3Binding>(FragmentOnboardingCarcadian3Binding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fullText = getString(R.string.text_what_is_circadian_mid_point)
        val gradientPart = getString(R.string.text_circadian_mid_point_quesmark)

        val spannable = SpannableString(fullText)

        val start = fullText.indexOf(gradientPart)
        val end = start + gradientPart.length

        val textView = binding.textView172
        val paint = textView.paint
        val textWidth = paint.measureText(gradientPart)
        val shader = LinearGradient(
            0f, 0f, textWidth, textView.textSize,
            intArrayOf(
                "#FAC8BA".toColorInt(),
                "#879ED6".toColorInt()
            ),
            null,
            Shader.TileMode.CLAMP
        )

        val span = object : CharacterStyle() {
            override fun updateDrawState(tp: TextPaint) {
                tp.shader = shader
            }
        }
        spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannable
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}