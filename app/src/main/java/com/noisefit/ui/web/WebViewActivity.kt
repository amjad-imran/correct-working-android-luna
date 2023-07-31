package com.noisefit.ui.web

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.noisefit.R
import com.noisefit_commans.ui.displayToast
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.databinding.ActivityWebViewBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.ui.common.*
import com.noisefit.util.ApplicationUtils


class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {

    private var url: String? = null

    val INPUT_FILE_REQUEST_CODE = 1
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath: String? = null

    companion object {

        const val DEFAULT_TITLE = "gonoise"
        fun getStartIntent(context: Context, title: String, url: String): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                this.putExtra("title", title)
                this.putExtra("url", url)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.url = intent.getStringExtra("url")
        binding.toolbar.tvTitle.text = intent.getStringExtra("title")

        if(!ApplicationUtils.isInternetConnected()){
            displayToast(getString(R.string.text_check_your_internet_connection))
            finish()
        }
        setWebView()
    }

    private fun setWebView() {
        binding.webViewMain.apply {
            //settings.builtInZoomControls = true
            //settings.setSupportZoom(true)
            //settings.displayZoomControls = true
            settings.allowFileAccess = true
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.webChromeClient = chromeClient
            }
        }.loadUrl(url ?: "https://www.gonoise.com/")
    }

    inner class WebViewClient : android.webkit.WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.progressBar.visible()
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val currentUrl = request?.url.toString()
            if (!currentUrl.startsWith("http") || !currentUrl.startsWith("https")) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(currentUrl))
                    startActivity(intent)
                    return true
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    return false
                }
            } else {
                return false
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.progressBar.gone()
        }
    }

    private val chromeClient = object : WebChromeClient() {

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            mFilePathCallback?.onReceiveValue(null)
            mFilePathCallback = filePathCallback
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "image/*"
            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select File")

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)

            return true
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri>? = null
        if (resultCode == RESULT_OK) {
            if (data == null) {
                // If there is not data, then we may have taken a photo
                if (mCameraPhotoPath != null) {
                    results = arrayOf(Uri.parse(mCameraPhotoPath))
                }
            } else {
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        mFilePathCallback?.onReceiveValue(results)
        mFilePathCallback = null
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun observeSubscriber() {
    }

    override fun getViewBinding(): ActivityWebViewBinding =
        ActivityWebViewBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? {
        return null
    }

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {
    }
}