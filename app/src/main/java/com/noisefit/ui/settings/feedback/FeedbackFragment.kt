package com.noisefit.ui.settings.feedback

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentFeedbackBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.profile.BottomSheetImagePicker
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.utils.LOGS
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_LAUNCH_LIBRARY = 384
const val REQUEST_LAUNCH_IMAGE_CAPTURE = 664

@AndroidEntryPoint
class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>(FragmentFeedbackBinding::inflate) {

    private val viewModel: FeedbackViewModel by viewModels()
    private lateinit var feedbackImageAdapter: FeedbackImageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //as per discussion with Akhil, as of now we skip it from here
        /*if (!viewModel.canSubmitFeedback()) {
            context.showShortToast("Please login and connect a device to submit feedback")
            navigateUpSafe()
        }*/


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        binding.textViewTitle.text = getString(R.string.text_raise_complaint)
        setRecycler()

    }

    private fun showAttachLogsBtn() {
        binding.logsCheckbox.visible()
        binding.logsCheckbox.isChecked=true

        binding.tvShareLogsLabel.visible()
        binding.tvShareLogsLabelMsg.visible()
    }

    private fun setRecycler() {
        feedbackImageAdapter = FeedbackImageAdapter(object : FeedbackAction {
            override fun onFeedbackRemoved() {
//                updatedSelectedImages()
            }
        })
        with(binding.attachRecycler) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = feedbackImageAdapter
        }
    }


    override fun initListener() {

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvHelp.setOnClickListener {
            //navigate(R.id.helpAndSupportFragment)
        }
        binding.ivAddAttachment.setOnClickListener {
            if ((viewModel.feedbackUriList.value?.size ?: 0) < 5) {
                showImagePicker()
            } else {
                val alertMessage = getString(R.string.text_max_screenshot_message, 5)
                uiController.onApiErrorReceived(
                    ErrorResponse(
                        UIComponentType.InfoAlertDialog(
                            getString(
                                R.string.text_max_alarm_reached
                            ), alertMessage, getString(R.string.text_close)
                        )
                    )
                )
            }
        }
        binding.bAcceptContinue.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEEDBACK_SUBMIT_CLICK)
            val appSuggestion = binding.appSuggestionEtv.text.toString()

            val ids: List<Int> = binding.tagGroup.checkedChipIds
            if (viewModel.problemTypeList.isNotEmpty())
                viewModel.problemTypeList.clear()
            for (id in ids) {
                val chip: Chip = binding.tagGroup.findViewById(id)
                viewModel.problemTypeList.add(chip.text.toString())
            }
            val problemType = viewModel.problemTypeList.joinToString { it }

            if (viewModel.problemTypeList.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_choose_one_problem_validation_msg),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else if (appSuggestion.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enter_description_validation_msg),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {

                uiController.hideSoftKeyboard()
                if (binding.logsCheckbox.isChecked) {
                    viewModel.submitReview(
                        viewModel.provideFeedbackData(
                            problemType,
                            appSuggestion,
                            viewModel.feedbackUriList.value ?: ArrayList(),
                            viewModel.appLogFile,
                            viewModel.watchLogFile
                        )

                    )

                } else {
                    viewModel.submitReview(
                        viewModel.provideFeedbackData(
                            problemType,
                            appSuggestion,
                            viewModel.feedbackUriList.value ?: ArrayList(),
                            null,
                            null
                        )

                    )
                }
            }
        }
    }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }

        viewModel.showAttachLogButton.observe(this) {
            if (it != null && it) {
                showAttachLogsBtn()
            }
        }

        viewModel.submittedSuccessfully.observe(this) {
            if (it) {
                uiController.onDisplayError(getString(R.string.text_feedback_successful))
                navigateUpSafe()
            }
        }


       /* viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.FirmwareLogObtained -> {
                    viewModel.setLoading(false)
                    viewModel.mightyLogPath = it.fileName
                    viewModel.getLogsPath()
                }
                else -> {}
            }
        }*/

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.feedbackUriList.observe(this) {
            feedbackImageAdapter.setDataSet(it)
        }
    }

    private fun showImagePicker() {
        setFragmentResultListener(BottomSheetImagePicker.IMAGE_PICKER_RESULT) { key, bundle ->
            val value = bundle.getInt("selectedValue")
            if (value == 0) {
                val libraryIntent = Intent(Intent.ACTION_PICK)
                libraryIntent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(libraryIntent, null),
                    REQUEST_LAUNCH_LIBRARY
                )
            } else {
                checkCameraPermission {
                    dispatchTakePictureIntent()
                }
            }
        }

        navigate(FeedbackFragmentDirections.actionNavigationFeedbackFragmentToBottomSheetImagePicker())

    }


    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.uploadingFile) {
                    uiController.onDisplayError(getString(R.string.text_submitting_feedback))
                    return
                }
                navigateUpSafe()
            }
        }

    private fun checkCameraPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            cameraPermissionResult.launch(Manifest.permission.CAMERA)
        }
    }

    private val cameraPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            dispatchTakePictureIntent()
        } else {
            context.showShortToast(getString(R.string.text_permission_required))
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                viewModel.photoFile = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                LOGS.d("IMAGE PATH 12 ${viewModel.photoFile?.name}")
                viewModel.photoFile?.also {
                    viewModel.photoURI = FileProvider.getUriForFile(
                        requireContext(), CommonConstants.FILE_PROVIDER, it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_LAUNCH_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        )
    }

    private fun getFileName(): String {
        return "${UUID.randomUUID()}.png"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LAUNCH_IMAGE_CAPTURE -> {
                    if (viewModel.photoURI == null) return
                    viewModel.outputUri = Uri.fromFile(File(context?.cacheDir, getFileName()))
                    UCrop.of(viewModel.photoURI!!, viewModel.outputUri!!)
                        .withOptions(uCropOptionsWithCompress)
                        .withMaxResultSize(1000, 1000)
                        .start(requireActivity(), this)
                }
                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return
                    viewModel.outputUri = Uri.fromFile(File(context?.cacheDir, getFileName()))
                    UCrop.of(selectedImageUri, viewModel.outputUri!!)
                        .withOptions(uCropOptionsWithCompress)
                        .withMaxResultSize(1000, 1000)
                        .start(requireActivity(), this)
                }
                UCrop.REQUEST_CROP -> {
                    LOGS.d("IMAGE PATH ${viewModel.outputUri} ${viewModel.photoURI} ${viewModel.photoFile?.name}")
                    viewModel.outputUri = null
                    viewModel.photoURI = null
                    viewModel.photoFile = null

                    val resultUri = data?.let { UCrop.getOutput(it) }
                    resultUri?.let {
                        viewModel.addToScreenshot(it)
                    }

//                    updatedSelectedImages()
                }
            }
        }
    }

//    fun updatedSelectedImages() {
//        binding.tvImagesAttachedCount.text = "${viewModel.feedbackUriList.size}/5 Image Selected"
//    }
}