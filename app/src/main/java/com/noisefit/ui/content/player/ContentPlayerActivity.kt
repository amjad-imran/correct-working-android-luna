package com.noisefit.ui.content.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.gson.JsonObject
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.luna.R
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.ContentRepository
import com.noisefit.luna.databinding.ActivityContentPlayerBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.summary.RING_ANIMATION
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@AndroidEntryPoint
class ContentPlayerActivity : BaseActivity<ActivityContentPlayerBinding>(), Player.Listener {
    private var timer: Timer? = null
    private var mPlayer: ExoPlayer? = null
    private var mPlayDuration: Long? = null
    private var playbackPosition: Long? = null
    private val mViewModel: ContentPlayerViewModel by viewModels()
    private val job = Job()
    val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var contentRepository: ContentRepository

    companion object {
        fun getStartIntent(
            context: Context,
            videoUrl: String,
            title: String,
            id: Int,
            progress: Int
        ): Intent {
            return Intent(context, ContentPlayerActivity::class.java).apply {
                this.putExtra("videoUrl", videoUrl)
                this.putExtra("title", title)
                this.putExtra("id", id)
                this.putExtra("progress", progress)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentData()

        binding.lytToolbar.tvTitle.text = mViewModel.title
        setRing()
        mViewModel.getHealthOverviewData()
        initializePlayer()
        playVideo()

    }

    private fun setRing() {
        binding.dynamicArcView.apply {
            addSeries(
                ApplicationUtils.seriesItemWithInset(
                    this@ContentPlayerActivity, 100f, 100f, R.color.distance_arc_bg, 40f, 32f
                )
            )

            addSeries(
                ApplicationUtils.seriesItemWithoutInset(
                    this@ContentPlayerActivity, 100f, 100f, R.color.calories_arc_bg, 16f
                )
            )
            addSeries(
                ApplicationUtils.seriesItemWithInset(
                    this@ContentPlayerActivity, 100f, 100f, R.color.steps_arc_bg, 20f, 32f
                )
            )
        }
    }

    private fun initializePlayer() {
        mPlayer = ExoPlayer.Builder(binding.playerView.context)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build().also { exoPlayer ->
                binding.playerView.player = exoPlayer
                exoPlayer.playWhenReady = false
                exoPlayer.addListener(this)

            }

        binding.playerView.keepScreenOn = true

        binding.playerView.setControllerVisibilityListener { visibility ->

            if (visibility == View.VISIBLE) {
                LOGS.d("Visible")
                binding.lytToolbar.root.visible()
                binding.dynamicArcView.visible()
            } else {
                LOGS.d("Hide")
                binding.lytToolbar.root.gone()
                binding.dynamicArcView.gone()
            }
        }


    }

    private fun syncData() {

        scope.launch {
            mViewModel.sessionManager.forceSyncDataWithServer = true
            val status = ApplicationUtils.startSyncScheduler(this@ContentPlayerActivity)
            withContext(Dispatchers.Main) {
                if (status) {
                    //   uiController.onDisplayError("Syncing")
                    LOGS.d("Sync Data Status $status")
                    mViewModel.getHealthOverviewData()
                } else {
                    //uiController.onDisplayError("Job is already running please wait")
                }
            }

        }
    }

    private fun playVideo() {
        tryCatch {
            val mediaItem: MediaItem = MediaItem.fromUri(Uri.parse(mViewModel.videoUrl))
            mPlayer?.setMediaItem(mediaItem)
            mPlayer?.prepare()
            mPlayer?.play()

            if (mViewModel.progress != null) {
                val lastProgressValue = (mViewModel.progress ?: 0) * 1000L
                mPlayDuration = lastProgressValue
                mPlayer?.seekTo(lastProgressValue)
            }
        }
    }

    private fun getIntentData() {
        mViewModel.id = intent.getIntExtra("id", -1)
        mViewModel.title = intent.getStringExtra("title")
        mViewModel.videoUrl = intent.getStringExtra("videoUrl")
        mViewModel.progress = intent.getIntExtra("progress", 0)
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            updatePlayTimeProgress()
            onBackPressed()
        }

    }

    private fun showHideToolbar() {
        binding.lytToolbar.root.visible()
        binding.dynamicArcView.visible()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            binding.lytToolbar.root.gone()
            binding.dynamicArcView.gone()
        }, 2000)
    }


    private fun updatePlayTimeProgress() {
        GlobalScope.launch(Dispatchers.IO) {
            val playSeconds = if (mPlayDuration == null) {
                0
            } else {
                mPlayDuration!! / 1000
            }

            val requestObject = JsonObject().apply {
                this.addProperty("video_id", mViewModel.id)
                this.addProperty("progress", playSeconds)
            }

            contentRepository.updateProgressVideo(requestObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {
                            LOGS.d("Done")
                            tryCatch {
//                                mViewModel.localDataStore.setContentViewCount(1)
                            }
                        }
                    }
                    else -> {}
                }
            }

        }

        mViewModel.videoUrl
    }

    private fun releasePlayer() {
        mPlayer?.run {
            playbackPosition = this.currentPosition
            playWhenReady = this.playWhenReady
            release()
        }
        mPlayer = null
    }

    override fun observeSubscriber() {
        mViewModel.healthOverviewData.observe(this) { healthOverviewData ->
            if (healthOverviewData != null) {
                binding.dynamicArcView.apply {
                    val distanceValue = healthOverviewData.distanceGoalProgress ?: 0f
                    if (mViewModel.distanceProgressCompleted != distanceValue) {
                        mViewModel.distanceProgressCompleted = distanceValue
                        LOGS.d("update_distance_value $distanceValue")
                        val distanceIndex: Int = addSeries(
                            ApplicationUtils.seriesItemWithInset(
                                this@ContentPlayerActivity, 0f, 100f, R.color.distance_arc, 25f, 16f
                            )
                        )
                        addEvent(
                            DecoEvent.Builder(distanceValue)
                                .setIndex(distanceIndex)
                                .setDuration(RING_ANIMATION)
                                .build()
                        )
                    }


                    val caloriesValue = healthOverviewData.caloriesGoalProgress ?: 0f

                    if (mViewModel.caloriesProgressCompleted != caloriesValue) {
                        mViewModel.caloriesProgressCompleted = caloriesValue

                        val caloriesIndex: Int = addSeries(
                            ApplicationUtils.seriesItemWithoutInset(
                                this@ContentPlayerActivity,
                                0f,
                                100f,
                                R.color.calories_arc,
                                16f
                            )
                        )
                        addEvent(
                            DecoEvent.Builder(caloriesValue).setIndex(caloriesIndex)
                                .setDuration(RING_ANIMATION).build()
                        )
                    }


                    val stepsValue = healthOverviewData.stepsGoalProgress ?: 0f
                    if (mViewModel.stepsProgressCompleted != stepsValue) {
                        mViewModel.stepsProgressCompleted = stepsValue

                        val stepIndex: Int = addSeries(
                            ApplicationUtils.seriesItemWithInset(
                                this@ContentPlayerActivity, 0f, 100f, R.color.steps_arc, 20f, 16f
                            )
                        )
                        addEvent(
                            DecoEvent.Builder(stepsValue).setIndex(stepIndex).setDuration(
                                RING_ANIMATION
                            )
                                .build()
                        )
                    }
                }
            }
        }
    }

    override fun getViewBinding() = ActivityContentPlayerBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
        } else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
            timer=Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    syncData()
                }
            }, 0, AppConstants.CONTENT_SYNC_DATA_INTERVAL)
        }

    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            LOGS.d("Playing")
            showHideToolbar()
        } else {
            binding.lytToolbar.root.visible()
            binding.dynamicArcView.visible()
            LOGS.d("Pause")
        }
    }


    override fun onPause() {
        super.onPause()
        mPlayer?.pause()
        mPlayDuration = mPlayer?.currentPosition
        updatePlayTimeProgress()
    }

    override fun onResume() {
        super.onResume()
        if (mPlayer != null)
            mPlayer?.play()
    }

    override fun onBackPressed() {
        mPlayDuration = mPlayer?.currentPosition
        updatePlayTimeProgress()
        if (timer != null) {
            timer?.cancel()
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
        if (timer != null) {
            timer?.cancel()
        }
    }


}