package com.noisefit.ui.feeds.create.update

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentUpdatePostBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.custom.MentionTextWatcher
import com.noisefit.ui.feeds.create.BottomSheetUploadPost
import com.noisefit.ui.feeds.create.CREATE_POST_KEY
import com.noisefit.ui.feeds.create.PostAutocompleteSpan
import com.noisefit.ui.feeds.create.UserSuggestionAdapter
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdatePostFragment :
    BaseFragment<FragmentUpdatePostBinding>(FragmentUpdatePostBinding::inflate),
    PostEditText.SelectionListener {
    val args: UpdatePostFragmentArgs by navArgs()
    private val viewModel: UpdatePostViewModel by viewModels()


    private val suggestionAdapter: UserSuggestionAdapter by lazy {
        UserSuggestionAdapter { user ->
            val e: Editable = binding.etTextCreator.text
            val start = e.getSpanStart(currentAutocompleteSpan)
            val end = e.getSpanEnd(currentAutocompleteSpan)
            val textToAdd = "@${user.getFirstName()} "
            e.replace(start, end, textToAdd)
            binding.etTextCreator.setSelection(start + textToAdd.length)
            finishAutocomplete()
            viewModel.addTagMapping(user, start)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_edit_post)
        val postData = args.timelineData

        binding.ivUserImage.loadImage(
            requireContext(),
            viewModel.getUserImage(),
            R.drawable.ic_default_profile_image
        )

        viewModel.postData = postData
        viewModel.mappedUser.clear()
        viewModel.mappedUser.addAll(postData.taggedUser ?: ArrayList())

        with(binding.rvSuggestions) {
            layoutManager = LinearLayoutManager(context)
            adapter = suggestionAdapter
        }

        binding.etTextCreator.setText(postData.caption)

        if (postData.mediaUrl?.first() != null) {
            binding.lytPostImage.vBackImage.loadImage(
                requireContext(),
                postData.mediaUrl?.first(),
                R.drawable.image_placeholder_voucher
            )
            binding.lytPostImage.vBackImage.visible()
        } else {
            binding.lytPostImage.vBackImage.gone()
        }

        binding.etTextCreator.post {
            showSoftKeyboard(binding.etTextCreator)
        }
    }

    private fun showSoftKeyboard(view: View) {
        val inputMethodManager =
            activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        inputMethodManager.showSoftInput(view, 0)
    }


    private var progressBottomSheet: BottomSheetUploadPost? = null

    private fun showProgressDialog(
        image: Bitmap?
    ) {
        progressBottomSheet = BottomSheetUploadPost.getInstance(image)
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )
    }


    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnPost.setOnClickListener {
            showProgressDialog(null)
            viewModel.updatePost(binding.etTextCreator.text.toString())
        }



        binding.etTextCreator.setSelectionListener(this)
        binding.etTextCreator.addTextChangedListener(MentionTextWatcher {
            if (it) {
                viewModel.hasContent = true
                handlePostCta()
            } else {
                viewModel.hasContent = false
                handlePostCta()
            }
        })

    }

    fun handlePostCta() {
        if (viewModel.hasContent) {
            binding.btnPost.visible()
        } else {
            binding.btnPost.gone()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBottomSheet?.dismissAllowingStateLoss()
    }

    override fun subscribeObservers() {

        viewModel.searchText.observe(this) {
            val searchedData = viewModel.getFriendsList(it ?: "")
            if (searchedData.isNullOrEmpty()) {
                binding.rvSuggestions.gone()
            } else {
                suggestionAdapter.setDataSet(searchedData)
                binding.rvSuggestions.visible()
            }
        }

        viewModel.postSuccess.observe(this) {
            it.getContent()?.let {
                progressBottomSheet?.dismiss()

                requireActivity().supportFragmentManager.setFragmentResult(
                    CREATE_POST_KEY,
                    bundleOf("updated" to true)
                )
                val count = viewModel.getFeedPosCount()
                val newCount = count + 1
                viewModel.updateFeedPostCount(newCount)
                navigateUpSafe()
            }
        }

        viewModel.uploadProgress.observe(this) {
            it.getContent()?.let { progress ->
                progressBottomSheet?.setProgress(progress)
            }
        }


    }

    private var currentAutocompleteSpan: PostAutocompleteSpan? = null
    override fun onSelectionChanged(start: Int, end: Int) {

        if (start == end && binding.etTextCreator.length() > 0) {
            val spans: Array<PostAutocompleteSpan> = binding.etTextCreator.text.getSpans(
                start, end,
                PostAutocompleteSpan::class.java
            )
            if (spans.isNotEmpty()) {
                val span = spans[0]
                if (currentAutocompleteSpan == null && end == binding.etTextCreator.text
                        .getSpanEnd(span)
                ) {
                    startAutocomplete(span)
                } else if (currentAutocompleteSpan != null) {
                    val e: Editable = binding.etTextCreator.text
                    val spanText = e.toString().substring(e.getSpanStart(span), e.getSpanEnd(span))
                    viewModel.searchText.value = spanText
                }
            } else if (currentAutocompleteSpan != null) {

                finishAutocomplete()
            }
        } else if (currentAutocompleteSpan != null) {
            finishAutocomplete()
        }
    }

    private fun startAutocomplete(span: PostAutocompleteSpan) {
        currentAutocompleteSpan = span
        val e: Editable = binding.etTextCreator.text
        val spanText = e.toString().substring(e.getSpanStart(span), e.getSpanEnd(span))
        viewModel.searchText.value = spanText
    }

    private fun finishAutocomplete() {
        if (currentAutocompleteSpan == null) return
        currentAutocompleteSpan = null
        viewModel.searchText.value = null
    }

}