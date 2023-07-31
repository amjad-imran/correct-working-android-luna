package com.noisefit.ui.myDevice.camera

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.coroutineScope
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.ActivityCameraShutterBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.ImageUtil
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CameraShutterActivity : BaseActivity<ActivityCameraShutterBinding>() {

    private var imageCapture: ImageCapture? = null
    var mLastClickTime: Long? = null
    private lateinit var outputDirectory: File
    private var currentCamBack = true

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var imageUtil: ImageUtil

    private var lastSavedImage: Uri? = null
    private var lastSavedBitmapThumb: Bitmap? = null

    /**
     * true if taking a pic and saving it
     * false when image saved
     */
    private var isProcessingPic = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager.isCameraShutterActivityOpened = true
        binding.toolbar.tvTitle.text = getString(R.string.text_camera_shutter)

        outputDirectory = getOutputDirectory()

        if (allPermissionsGranted()) {
            startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            sessionManager.sendUpdateQueryAction(UpdateDeviceAction.StartCameraMode(true))
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun takePhoto() {
        if (isProcessingPic) {
            LOGS.d("Camera shutter call ignored")
            return
        }
        isProcessingPic = true
        binding.progressBar.root.visible()
        binding.progressBar.tvLoadingText.text = getString(R.string.text_capturing_wait)


        val imageCapture = imageCapture ?: return
        /* val photoFile = File(
             outputDirectory,
             SimpleDateFormat(
                 FILENAME_FORMAT, Locale.US
             ).format(System.currentTimeMillis()) + ".jpg"
         )*/
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        photoFile?.let {
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        exc.printStackTrace()
                        try {
                            isProcessingPic = false
                            binding.progressBar.root.gone()
                        } catch (exp: Exception) {
                        }
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        LOGS.d("Image Received, Saving")
//                        saveImage(photoFile)
                        lifecycle.coroutineScope.launch {

                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                rotateImageCorrectly(photoFile)
                            }
                            saveImage(photoFile)
                        }
                    }
                })
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        )
    }


    private fun setLastClickedImage(savedUri: Uri?) {
        savedUri?.let {
            lastSavedImage = it
            lastSavedBitmapThumb?.let { bitmap ->
                binding.ivPreviewLast.setImageBitmap(bitmap)
            }
        }
    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                //.setTargetResolution(Size(1280,720))
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exp: Exception) {
                //Camera unavailable catch
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                sessionManager.sendUpdateQueryAction(UpdateDeviceAction.StartCameraMode(true))
            } else {
                onApiErrorReceived(ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_permission_denial_camera_storage),
                        false,
                        getString(R.string.text_allow),
                        object : BinaryActionCallback {
                            override fun yes() {
                                ApplicationUtils.openAppSettings(this@CameraShutterActivity)
                                finish()
                            }

                            override fun no() {
                                finish()
                            }

                        }

                    )))

            }
        }
    }

    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, CameraShutterActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            }
        }

        private const val TAG = "CameraShutter"
        private const val REQUEST_CODE_PERMISSIONS = 55
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            finish()
        }
        binding.bSwitchCamera.setOnClickListener {
            if (currentCamBack) {
                startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            } else {
                startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            }
            currentCamBack = !currentCamBack
        }
        binding.bCapture.setOnClickListener { takePhoto() }

        binding.ivPreviewLast.setOnClickListener {

            mLastClickTime?.let {
                if (SystemClock.elapsedRealtime() - it < 1000) {
                    LOGS.d("Returning from watchface click")
                    return@setOnClickListener
                }
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                lastSavedImage?.let {
                    try {
                        startActivity(
                            Intent(Intent.ACTION_VIEW, it)
                        )
                    } catch (exp: ActivityNotFoundException) {
                        //No supported apps
                    } catch (exp: SecurityException) {
                        //Permission denial - system specific
                    }
                }
            } else {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.type = "image/*"
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    startActivity(intent)
                } catch (exp: ActivityNotFoundException) {
                    //No supported apps
                }
            }
        }
    }

    override fun observeSubscriber() {
        sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.ClickCameraImage -> {
                    LOGS.d("Capture image request received")
                    takePhoto()
                }
                is QueryCallback.CloseCameraShutterActivity -> {
                    LOGS.d("Close camera shutter activity")
                    if (it.finish) {
                        finish()
                    }
                }
                else -> {}
            }
        }
        sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { event ->
                when (event) {
                    is UpdateDeviceDataCallback.ClickCameraImage -> {
                        LOGS.d("Capture image request received")
                        takePhoto()
                    }
                    else -> {}
                }

            }

        }

    }

    override fun getViewBinding(): ActivityCameraShutterBinding =
        ActivityCameraShutterBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.isCameraShutterActivityOpened = false
        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.StartCameraMode(false))

    }

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {}

    private fun saveImage(photoFile: File) {
        val msg = "Photo capture succeeded: ${photoFile.path}"
        LOGS.d(TAG, msg)

        GlobalScope.launch(Dispatchers.IO) {
            val savedPicturesUri =
                imageUtil.saveFileToPictures(this@CameraShutterActivity, photoFile)

            withContext(Dispatchers.Main) {
                isProcessingPic = false
                binding.progressBar.root.gone()
                try {
                    if (savedPicturesUri != null) {
                        lastSavedBitmapThumb = imageUtil.getThumbBitmap(photoFile, 100, 100)
                        setLastClickedImage(savedPicturesUri)
                        baseContext.showShortToast(getString(R.string.text_image_saved_gallery))
                    } else {
                        baseContext.showShortToast(getString(R.string.text_error_saving_image))
                    }
                } catch (exp: Exception) {
                }
            }
            imageUtil.deleteTempImage(photoFile)
        }


    }

    private suspend fun rotateImageCorrectly(photoFile: File) = withContext(Dispatchers.IO) {
        val sourceBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoFile.toUri())

        val exif = ExifInterface(photoFile.inputStream())
        val rotation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotationInDegrees = when (rotation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            ExifInterface.ORIENTATION_TRANSVERSE -> -90
            ExifInterface.ORIENTATION_TRANSPOSE -> -270
            else -> 0
        }
        val matrix = Matrix().apply {
            if (rotation != 0) preRotate(rotationInDegrees.toFloat())
        }

        val rotatedBitmap =
            Bitmap.createBitmap(
                sourceBitmap,
                0,
                0,
                sourceBitmap.width,
                sourceBitmap.height,
                matrix,
                true
            )

        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(photoFile))

        sourceBitmap.recycle()
        rotatedBitmap.recycle()
    }
}