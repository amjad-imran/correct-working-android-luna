package com.noisefit.ui.dashboard

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLogsDisplayBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppLogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class LogsDisplayFragment :
    BaseFragment<FragmentLogsDisplayBinding>(FragmentLogsDisplayBinding::inflate) {

    val job = Job()
    val uiScope = CoroutineScope(Dispatchers.IO + job)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.root.visible()
        uiScope.launch {
            displayLogs(false)
        }

    }

    override fun initListener() {
        binding.bClearData.setOnClickListener {
            AppLogs.deleteFile()
            context.showShortToast("File Deleted")
            navigateUpSafe()
        }
        binding.bShowRingLogs.setOnClickListener {
            /*binding.bShowRingLogs.gone()
            binding.tvContent.text = ""

            displayLogs(true)*/

            navigate(R.id.testDataFragment)


        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
    }

    suspend fun displayLogs(showRingData: Boolean) {
        val folder = File(requireActivity().externalCacheDir?.absolutePath, AppLogs.getLogsFolder())
        val file: File = File(folder, "logs.txt")

        val text = StringBuilder()
        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                if (showRingData) {
                    if (line?.contains("LUNA->", true) == true) {
                        text.append(line)
                        text.append('\n')
                    }
                } else {
                    text.append(line)
                    text.append('\n')
                }

            }
            br.close()
        } catch (e: IOException) {
            //You'll need to add proper error handling here
        }

        withContext(Dispatchers.Main) {
            binding.tvContent.text = text.toString()
            binding.tvContent.movementMethod = ScrollingMovementMethod()
            binding.progressBar.root.gone()
        }
    }
}