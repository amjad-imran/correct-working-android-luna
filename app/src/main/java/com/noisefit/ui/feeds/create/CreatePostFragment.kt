package com.noisefit.ui.feeds.create

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.HandlerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit.data.model.PostBackground
import com.noisefit.databinding.FragmentCreatePostBinding
import com.noisefit.ui.common.PostEditText
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.custom.MentionTextWatcher
import com.noisefit.ui.profile.BottomSheetImagePicker
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.ImageUtil
import com.noisefit_commans.utils.LOGS
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

val AUTO_COMPLETE_PATTERN =
    Pattern.compile("(?<!\\w)(?:@([a-zA-Z0-9_]+)(@[a-zA-Z0-9_.-]+)?|#([^\\s.]+)|:([a-zA-Z0-9_]+))")

const val CREATE_POST_KEY = "CREATE_POST_KEY"

@AndroidEntryPoint
class CreatePostFragment :
    BaseFragment<FragmentCreatePostBinding>(FragmentCreatePostBinding::inflate),
    PostEditText.SelectionListener {
    private val TAG = "CreatePostActivity"

    private val viewModel: CreatePostViewModel by viewModels()

    private val args: CreatePostFragmentArgs by navArgs()


    private val REQUEST_LAUNCH_LIBRARY = 384
    private val REQUEST_LAUNCH_IMAGE_CAPTURE = 664
    var outputUri: Uri? = null
    var photoURI: Uri? = null

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

    private val imagesAdapter: ImagesAdapter by lazy {
        ImagesAdapter { postBackground, position ->

            if (viewModel.selectedContentAction == null) return@ImagesAdapter

            if (viewModel.selectedContentAction == PostContentAction.BACKGROUND) {
                if (postBackground.title.equals("upload", true)) {
                    openImagePicker()
                    return@ImagesAdapter
                }
                setImage(postBackground)
                imagesAdapter.setSelected(position)
                viewModel.selectedBackgroundPosition = position

            } else {
                viewModel.selectedStylePosition = position
                imagesAdapter.setSelected(position)

                val frag = viewModel.getStyleFragment(position, viewModel.selectedAction)
                if (frag != null) {
                    loadFragment(frag)
                }
            }
        }
    }

    private var progressBottomSheet: BottomSheetUploadPost? = null

    private fun showProgressDialog(
        image: Bitmap?
    ) {
        progressBottomSheet = BottomSheetUploadPost.getInstance(image)
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager, "DownloadBottomSheet"
        )
    }


    private fun setImage(
        postBackground: PostBackground
    ) {
        binding.lytPostImage.vBackImage.setImageURI(null)
        if (postBackground.imageUri != null) {
            binding.lytPostImage.vBackImage.setImageURI(postBackground.imageUri.toUri())
            viewModel.hasSelectedBackImage.value = true
            return
        }
        if (postBackground.resourceId != null) {
            binding.lytPostImage.vBackImage.setImageResource(postBackground.resourceId)
            viewModel.hasSelectedBackImage.value = true
            return
        }

        if (!postBackground.imageUrl.isNullOrEmpty()) {
            binding.lytPostImage.vBackImage.loadImage(requireContext(), postBackground.imageUrl)
            viewModel.hasSelectedBackImage.value = true
            return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.text_new_post)



        binding.ivUserImage.loadImage(
            requireContext(), viewModel.getUserImage(), R.drawable.ic_default_profile_image
        )
        setRecycler()

        when (args.shareContent) {
            PostContent.RINGS -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    nullableBinding?.lytPostAddContent?.viewRings?.performClick()
                }, 500)
            }

            PostContent.WORKOUTS -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    args.workout?.let { setWorkoutDefault(it) }
                }, 500)
            }

            PostContent.CHALLENGES -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    args.challenge?.let { setChallengeDefault(it) }
                }, 500)
            }

            else -> {
                binding.etTextCreator.requestFocus()
            }
        }

    }

    private fun setRecycler() {
        with(binding.rvSuggestions) {
            layoutManager = LinearLayoutManager(context)
            adapter = suggestionAdapter
        }
        with(binding.lytContentActions.rvImages) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = imagesAdapter
        }
    }

    /**
     * IMAGES AND STYLES
     */
    private fun setContentActionDataSet() {
        if (nullableBinding == null) return
        val currentAction = viewModel.selectedAction ?: return

        binding.lytPostAddContent.root.gone()
        binding.lytContentActions.root.visible()

        if (viewModel.selectedContentAction == null) {
            val frag = viewModel.getStyleFragment(0, viewModel.selectedAction)
            if (frag != null) {
                loadFragment(frag)
            }
            viewModel.selectedContentAction = PostContentAction.BACKGROUND
        }


        binding.ivEditImage.gone()


        if (viewModel.selectedContentAction == PostContentAction.BACKGROUND) {
            val images = viewModel.getBackgroundImagesList()
            imagesAdapter.setDataSet(
                images, viewModel.getSelectedBackgroundPos()
            )

            if (viewModel.hasSelectedBackImage.value == false) {
                binding.lytPostImage.vBackImage.visible()
                binding.lytPostImage.vBackImage.setImageURI(null)

                if (viewModel.templateImages.value?.isEmpty() != true) {
                    viewModel.templateImages.value?.first()?.image_url?.let {
                        binding.lytPostImage.vBackImage.loadImage(
                            requireContext(), it
                        )
                        viewModel.hasSelectedBackImage.value = true
                    }
                }
            }

            return
        }


        imagesAdapter.setDataSet(
            viewModel.getStyles(currentAction), viewModel.getSelectedLayoutPos()
        )
        tryCatch {
            nullableBinding?.cvMain?.post {
                nullableBinding?.cvMain?.fullScroll(View.FOCUS_DOWN)
            }
        }
    }


    override fun initListener() {

        binding.etTextCreator.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val imm: InputMethodManager? =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(binding.etTextCreator, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        binding.btnPost.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.POSTUPLOAD_CLICK)
            postImage()
        }

        binding.lytPostAddContent.viewUpload.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.UPLOAD_CLICK)
            viewModel.showImagePicker.postValue(Event(true))
        }
        binding.lytPostAddContent.viewRings.setOnClickListener {
            viewModel.selectedAction = PostContent.RINGS
            viewModel.selectedBackgroundPosition = -1
            viewModel.selectedStylePosition = -1
            setContentActionDataSet()
        }
        binding.lytPostAddContent.viewWorkouts.setOnClickListener {
            setFragmentResultListener(BottomSheetWorkoutPicker.WORKOUT_PICKER_RESULT) { key, bundle ->
                val selected = bundle.getParcelable("workout") as? SportsModeResponse
                if (selected != null) {
                    setWorkoutDefault(selected)
                }
            }

            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WORKOUTUPLOAD_CLICK)
            navigate(CreatePostFragmentDirections.actionCreatePostFragmentToBottomSheetWorkoutPicker())
        }

        binding.lytPostAddContent.viewChallenges.setOnClickListener {
            setFragmentResultListener(BottomSheetChallengePicker.CHALLENGE_PICKER_RESULT) { key, bundle ->
                val selected = bundle.getParcelable("challenge") as? ChallengeModel
                if (selected != null) {
                    setChallengeDefault(selected)
                }
            }

            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGESUPLOAD_CLICK)
            navigate(CreatePostFragmentDirections.actionCreatePostFragmentToBottomSheetChallengePicker())
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytContentActions.btnBackground.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NEWPOST_BACKGROUND_CLICK)
            viewModel.selectedContentAction = PostContentAction.BACKGROUND
            binding.lytContentActions.btnBackground.setTextColor(requireContext().getColor(R.color.accent_color_purple))
            binding.lytContentActions.btnLayout.setTextColor(requireContext().getColor(R.color.white_64))
            setContentActionDataSet()
        }

        binding.lytContentActions.btnLayout.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NEWPOST_LAYOUT_CLICK)
            viewModel.selectedContentAction = PostContentAction.LAYOUT
            binding.lytContentActions.btnBackground.setTextColor(requireContext().getColor(R.color.white_64))
            binding.lytContentActions.btnLayout.setTextColor(requireContext().getColor(R.color.accent_color_purple))
            setContentActionDataSet()
        }

        binding.ivEditImage.setOnClickListener {
            setFragmentResultListener(BottomSheetEditImage.IMAGE_EDIT_RESULT) { _, bundle ->
                val selectedValue = bundle.getInt("selectedValue")
                if (selectedValue == 1) {
                    viewModel.showImagePicker.postValue(Event(true))
                    openImagePicker()
                } else {
                    viewModel.userSelectedBackImage.postValue(null)
                    viewModel.hasSelectedBackImage.postValue(false)
                }
            }
            viewModel.userSelectedBackImage.value?.let {
                navigate(
                    CreatePostFragmentDirections.actionCreatePostFragmentToBottomSheetEditImage(
                        it.toString()
                    )
                )
            }

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

    private fun setChallengeDefault(selected: ChallengeModel) {
        viewModel.selectedChallenge = selected
        viewModel.selectedAction = PostContent.CHALLENGES
        viewModel.selectedBackgroundPosition = -1
        viewModel.selectedStylePosition = -1
        binding.ivEditImage.gone()
        setContentActionDataSet()
    }

    private fun setWorkoutDefault(selected: SportsModeResponse) {
        viewModel.selectedWorkout = selected
        viewModel.selectedAction = PostContent.WORKOUTS
        viewModel.selectedBackgroundPosition = -1
        viewModel.selectedStylePosition = -1
        binding.ivEditImage.gone()
        setContentActionDataSet()
    }

    fun openImagePicker() {
        navigate(CreatePostFragmentDirections.actionCreatePostFragmentToBottomSheetImagePicker())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LAUNCH_IMAGE_CAPTURE -> {
                    if (photoURI == null) return
                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(photoURI!!, outputUri!!).withAspectRatio(1f, 1f)
                        .withOptions(uCropOptions).withMaxResultSize(1024, 1024)
                        .start(requireActivity(), this)
                }

                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return

                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(selectedImageUri, outputUri!!).withAspectRatio(1f, 1f)
                        .withOptions(uCropOptions).withMaxResultSize(1024, 1024)
                        .start(requireActivity(), this)
                }

                UCrop.REQUEST_CROP -> {
                    val resultUri = data?.let { UCrop.getOutput(it) }
                    viewModel.userSelectedBackImage.value = resultUri
                }
            }
        }
    }

    private fun checkCameraPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
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
            context.showShortToast("Permission Required")
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        requireContext(), CommonConstants.FILE_PROVIDER, it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_LAUNCH_IMAGE_CAPTURE)
                }
            }
        }
    }

    lateinit var currentPhotoPath: String
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {

            currentPhotoPath = absolutePath
        }
    }

    private fun handlePostCta() {
        if (viewModel.hasSelectedBackImage.value == true) {
            nullableBinding?.lytPostAddContent?.textAdd?.visible()
            nullableBinding?.lytPostAddContent?.viewUpload?.gone()
            nullableBinding?.lytPostAddContent?.textUpload?.gone()
            nullableBinding?.btnPost?.visible()
        } else {
            nullableBinding?.lytPostAddContent?.textAdd?.gone()
            nullableBinding?.lytPostAddContent?.viewUpload?.visible()
            nullableBinding?.lytPostAddContent?.textUpload?.visible()
            if (viewModel.hasContent) {
                nullableBinding?.btnPost?.visible()
            } else {
                nullableBinding?.btnPost?.gone()
            }
        }
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

        viewModel.templateImages.observe(this) {
            setContentActionDataSet()
        }

        viewModel.hasSelectedBackImage.observe(this) {
            handlePostCta()
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.postSuccess.observe(this) {
            it.getContent()?.let {
                progressBottomSheet?.dismiss()

                requireActivity().supportFragmentManager.setFragmentResult(
                    CREATE_POST_KEY, bundleOf("updated" to true)
                )
                setFragmentResult(
                    CREATE_POST_KEY, bundleOf("updated" to true)
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

        viewModel.showImagePicker.observe(this) {
            it.getContent()?.let {
                openImagePicker()
            }
        }

        setFragmentResultListener(BottomSheetImagePicker.IMAGE_PICKER_RESULT) { key, bundle ->
            val value = bundle.getInt("selectedValue")
            if (value == 0) {
                val libraryIntent = Intent(Intent.ACTION_PICK)
                libraryIntent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(libraryIntent, null), REQUEST_LAUNCH_LIBRARY
                )
            } else {
                checkCameraPermission {
                    dispatchTakePictureIntent()
                }
            }
        }

        viewModel.userSelectedBackImage.observe(this) {
            binding.lytPostImage.vBackImage.setImageURI(null)
            if (it != null) {
                binding.ivEditImage.visible()
                binding.lytPostImage.vBackImage.visible()
                binding.lytPostImage.vBackImage.setImageURI(it)
                viewModel.hasSelectedBackImage.value = true

                if (viewModel.selectedContentAction != null) {
                    viewModel.selectedBackgroundPosition = -1
                    setContentActionDataSet()
                    binding.ivEditImage.gone()
                }
            } else {
                binding.lytPostImage.vBackImage.gone()
                binding.ivEditImage.gone()
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }


    private var currentAutocompleteSpan: PostAutocompleteSpan? = null
    override fun onSelectionChanged(start: Int, end: Int) {

        if (start == end && binding.etTextCreator.length() > 0) {
            val spans: Array<PostAutocompleteSpan> = binding.etTextCreator.text.getSpans(
                start, end, PostAutocompleteSpan::class.java
            )
            if (spans.isNotEmpty()) {
                val span = spans[0]
                if (currentAutocompleteSpan == null && end == binding.etTextCreator.text.getSpanEnd(
                        span
                    )
                ) {
                    LOGS.d(TAG, "onSelectionChanged Auto complete start $span")
                    startAutocomplete(span)
                } else if (currentAutocompleteSpan != null) {
                    val e: Editable = binding.etTextCreator.text
                    val spanText = e.toString().substring(e.getSpanStart(span), e.getSpanEnd(span))
                    LOGS.d(TAG, "onSelectionChanged Entered Text $spanText")
                    viewModel.searchText.value = spanText
                }
            } else if (currentAutocompleteSpan != null) {
                LOGS.d(TAG, "onSelectionChanged Auto complete finish 1")

                finishAutocomplete()
            }
        } else if (currentAutocompleteSpan != null) {
            LOGS.d(TAG, "onSelectionChanged Auto complete finish 2")
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

    private fun loadFragment(fragment: Fragment) {
        val fm: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.flFragment, fragment)
        fragmentTransaction.commit()

    }

    private fun postImage() {
        if (viewModel.hasSelectedBackImage.value == true) {
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
            val view = binding.lytPostImage.root
            executorService.execute {
                mainThreadHandler.post {
                    binding.progressBar.root.visible()
                }
                val shareBitmap = ImageUtil.getBitmapFromView(view)
                shareBitmap?.let {
                    val content = binding.etTextCreator.text.toString()
                    viewModel.createPost(it, content)
                    showProgressDialog(it)
                }
                mainThreadHandler.post {
                    binding.progressBar.root.gone()
                }
            }
        } else {
            val content = binding.etTextCreator.text.toString()
            viewModel.createPost(null, content)
            showProgressDialog(null)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBottomSheet?.dismissAllowingStateLoss()
    }


}

open class PostAutocompleteSpan : CharacterStyle() {
    override fun updateDrawState(tp: TextPaint) {

    }
}

class PostMentionSpan : PostAutocompleteSpan() {

    override fun updateDrawState(tp: TextPaint) {
        tp.color = Color.parseColor("#ca99ff")
        tp.typeface = MEDIUM_TYPEFACE
    }

    companion object {
        private val MEDIUM_TYPEFACE = Typeface.create("sans-serif-medium", 0)
    }
}

class PostHashtagSpan : PostAutocompleteSpan() {

    override fun updateDrawState(tp: TextPaint) {
        tp.color = Color.parseColor("#7affffff")
        tp.typeface = MEDIUM_TYPEFACE
    }

    companion object {
        private val MEDIUM_TYPEFACE = Typeface.create("sans-serif-medium", 0)
    }
}