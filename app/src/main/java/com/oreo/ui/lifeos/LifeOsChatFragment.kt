package com.oreo.ui.lifeos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.oreo.ui.chatGpt.ChatGptAdapter
import com.oreo.data.model.ChatGptOverview
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsChatBinding
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.chatGpt.ATTACHMENT_KEY
import com.oreo.ui.chatGpt.ChatGptViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class LifeOsChatFragment :
    BaseFragment<FragmentLifeOsChatBinding>(FragmentLifeOsChatBinding::inflate) {

    private var previousSoftInputMode: Int? = null
    private val viewModel: ChatGptViewModel by viewModels()
    private val mAdapter: ChatGptAdapter by lazy { ChatGptAdapter() }

    private var cameraUri: Uri? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var pickDocumentLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = requireActivity().window
        if (previousSoftInputMode == null) previousSoftInputMode = window.attributes.softInputMode
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun initListener() {
        binding.lytChatBox.chatEtx.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val message = v.text?.toString()?.trim().orEmpty()
                if (message.isNotEmpty()) sendMessage(message)
                true
            } else false
        }

        binding.lytChatBox.ivAddAttachment.setOnClickListener {
            setFragmentResultListener(ATTACHMENT_KEY) { _, bundle ->
                when (bundle.getString("type")) {
                    "camera" -> launchCameraPicker()
                    "photo" -> pickImageLauncher.launch("image/*")
                    "file" -> pickDocumentLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                    )
                }
            }
            navigate(R.id.bottomSheetAttachmentPicker)
        }

        binding.lytChatBox.chatEtx.addTextChangedListener(afterTextChanged = {
            setActionButtonState(it?.toString().orEmpty())
        })

        binding.lytChatBox.btnAction.setOnClickListener {
            val text = binding.lytChatBox.chatEtx.text?.toString().orEmpty().trim()
            if (text.isEmpty() && viewModel.pendingAttachment == null) {
                navigate(R.id.chatGptAudioFragment)
            } else {
                val message = if (text.isEmpty()) getString(R.string.text_analyse_this) else text
                sendMessage(message)
            }
        }

        binding.lytChatBox.ivRemoveAttachment.setOnClickListener {
            viewModel.clearPendingAttachment()
        }
    }

    override fun subscribeObservers() {
        viewModel.attachmentPreview.observe(this) { data ->
            val enteredText = binding.lytChatBox.chatEtx.text?.toString().orEmpty()
            setActionButtonState(enteredText)
            if (data == null) {
                binding.lytChatBox.lytAttachment.gone()
                return@observe
            }

            binding.lytChatBox.lytAttachment.visible()
            val isImage = data.mimeType.startsWith("image/")
            if (isImage) {
                binding.lytChatBox.ivAttachmentImage.visible()
                binding.lytChatBox.lytAttachmentDoc.gone()
                try {
                    com.bumptech.glide.Glide.with(binding.root.context)
                        .load(data.uri)
                        .into(binding.lytChatBox.ivAttachmentImage)
                } catch (_: Exception) {}
            } else {
                binding.lytChatBox.ivAttachmentImage.gone()
                binding.lytChatBox.lytAttachmentDoc.visible()
                binding.lytChatBox.tvDocType.text =
                    if (data.mimeType == "application/pdf") "PDF" else "DOC"
                binding.lytChatBox.tvDocName.text = data.fileName
            }
            // layout will remeasure due to visibility change; no manual padding needed
        }

        viewModel.scrollToBottom.observe(this) {
            it.getContent()?.let {
                binding.rvChats.smoothScrollToPosition((binding.rvChats.adapter?.itemCount ?: 1) - 1)
            }
        }

        viewModel.chatGptOverview.observe(this) { list ->
            list?.let {
                mAdapter.setDataSet(it)
                binding.rvChats.post {
                    binding.rvChats.smoothScrollToPosition(mAdapter.itemCount - 1)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = binding.lytChatBox.chatEtx
        editText.requestFocus()

        setupImeAnimation()

        registerAttachmentPickers()
        setActionButtonState(editText.text?.toString().orEmpty())
        viewModel.generateThreadId()

        // Setup chat list
        binding.rvChats.apply {
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        mAdapter.itemClickListener = { item, _ ->
            if (item is ChatGptOverview.RetryMessage) {
                viewModel.retryApi()
            }
        }

        val showIme: () -> Unit = {
            ViewCompat.getWindowInsetsController(view)?.show(WindowInsetsCompat.Type.ime())
                ?: run {
                    requireContext().getSystemService<InputMethodManager>()?.showSoftInput(
                        editText,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }
        }

        // No shared element transition; show IME immediately
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        view.post {
            showIme()
            kickstartImeTranslation()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun setActionButtonState(text: String) {
        val hasAttachment = viewModel.pendingAttachment != null
        val showSend = text.isNotEmpty() || hasAttachment
        val res = if (showSend) R.drawable.ic_ai_send_message_2 else R.drawable.image_ai_mic
        binding.lytChatBox.btnAction.setImageResource(res)
    }

    private fun setupImeAnimation() {
        val root = requireView()

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, sysBars.bottom + imeBottom)
            insets
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            root,
            object : WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    root.setPadding(root.paddingLeft, root.paddingTop, root.paddingRight, sysBars.bottom + imeBottom)
                    return insets
                }
            }
        )
    }

    private fun kickstartImeTranslation() {
        val root = view ?: return
        root.post { root.requestLayout() }
    }


    private fun registerAttachmentPickers() {
        requestCameraPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    launchCameraPicker()
                } else {
                    val showRationale = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    if (showRationale) {
                        context.showShortToast(getString(R.string.text_camera_permission))
                    } else {
                        showCameraPermissionSettingsDialog()
                    }
                }
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                val uri = cameraUri
                if (success && uri != null) {
                    handlePickedUri(uri, "image/jpeg")
                }
            }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val mime = viewModel.getMimeType(requireContext(), it) ?: "image/*"
                val isHeic = mime.equals("image/heic", true) || mime.equals("image/heif", true)
                if (isHeic) {
                    val converted = convertHeicToJpeg(it, viewModel.DEFAULT_IMAGE_QUALITY)
                    if (converted == null) {
                        context.showShortToast(getString(R.string.text_something_went_wrong_single))
                        return@let
                    }

                    val size = viewModel.getFileSize(requireContext(), converted)
                    if (size < 0L) {
                        context.showShortToast(getString(R.string.text_something_went_wrong_single))
                        return@let
                    }
                    if (size > viewModel.IMAGE_MAX_BYTES) {
                        context.showShortToast(getString(R.string.text_file_too_large_max_5_mb))
                        return@let
                    }

                    val originalName = viewModel.getDisplayName(requireContext(), it) ?: "image"
                    val jpgName = if (originalName.contains('.')) {
                        originalName.substringBeforeLast('.') + ".jpg"
                    } else {
                        "$originalName.jpg"
                    }

                    viewModel.setPendingAttachment(converted, "image/jpeg", jpgName, size)
                } else {
                    handlePickedUri(it, mime)
                }
            }
        }

        pickDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let {
                    handlePickedUri(it, viewModel.getMimeType(requireContext(), it) ?: "application/octet-stream")
                }
            }
    }

    private fun showCameraPermissionSettingsDialog() {
        try {
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_camera_perssision_message),
                        false,
                        getString(R.string.text_go_to_settings),
                        object : com.noisefit_commans.data.BinaryActionCallback {
                            override fun yes() { openAppSettings() }
                            override fun no() {}
                        }
                    )
                )
            )
        } catch (_: Exception) {
            context.showShortToast(getString(R.string.text_camera_permission))
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) { }
    }

    private fun launchCameraPicker() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
            return
        }
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val cacheDir = requireContext().externalCacheDir ?: requireContext().cacheDir
        val imageFile = File(cacheDir, fileName)
        imageFile.parentFile?.mkdirs()

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.noisefit.luna.fileprovider",
            imageFile
        )
        cameraUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun handlePickedUri(uri: Uri, fallbackMime: String) {
        val mime = viewModel.getMimeType(requireContext(), uri) ?: fallbackMime
        val name = viewModel.getDisplayName(requireContext(), uri) ?: "file"
        val isImage = mime.startsWith("image/")
        val isSupportedDoc = mime == "application/pdf"

        if (!(isImage || isSupportedDoc)) {
            context.showShortToast(getString(R.string.text_unsupported_file_type))
            return
        }

        if (isImage) {
            val compressedUri = viewModel.compressImage(
                requireContext(),
                uri,
                quality = viewModel.DEFAULT_IMAGE_QUALITY
            )
            if (compressedUri == null) {
                context.showShortToast(getString(R.string.text_something_went_wrong_single))
                return
            }
            val compressedSize = viewModel.getFileSize(requireContext(), compressedUri)
            if (compressedSize < 0L) {
                context.showShortToast(getString(R.string.text_something_went_wrong_single))
                return
            }
            if (compressedSize > viewModel.IMAGE_MAX_BYTES) {
                context.showShortToast(getString(R.string.text_file_too_large_max_5_mb))
                return
            }
            viewModel.setPendingAttachment(compressedUri, "image/jpeg", name, compressedSize)
            LOGS.d("Image compressed -> $compressedUri image/jpeg $name $compressedSize")
        } else {
            val size = viewModel.getFileSize(requireContext(), uri)
            if (size < 0L) {
                context.showShortToast(getString(R.string.text_something_went_wrong_single))
                return
            }
            if (size > viewModel.IMAGE_MAX_BYTES) {
                context.showShortToast(getString(R.string.text_file_too_large_max_5_mb))
                return
            }
            viewModel.setPendingAttachment(uri, mime, name, size)
            LOGS.d("Doc picked -> $uri $mime $name $size")
        }
    }

    private fun convertHeicToJpeg(sourceUri: Uri, quality: Int): Uri? {
        return try {
            val resolver = requireContext().contentResolver
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(resolver, sourceUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                resolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
            }

            if (bitmap == null) return null

            val outFile = File(requireContext().cacheDir, "heic_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(0, 100), fos)
            }
            bitmap.recycle()

            FileProvider.getUriForFile(
                requireContext(),
                "com.noisefit.luna.fileprovider",
                outFile
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun sendMessage(message: String) {
        hideKeyboard()
        if (message.isNotEmpty()) {
            viewModel.addSentMessageWithPendingAttachment(message)
            viewModel.addThinkingMessage()
            binding.lytChatBox.chatEtx.setText("")
            viewModel.askQuestionStream(message.replace("\n", ""))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        previousSoftInputMode?.let { requireActivity().window.setSoftInputMode(it) }
        previousSoftInputMode = null
    }
}
