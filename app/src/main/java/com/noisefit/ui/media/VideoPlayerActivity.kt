package com.noisefit.ui.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit_commans.data.model.NoiseHealthVideo
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.databinding.ActivityVideoPlayerBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.models.SportsModeRequest
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
@AndroidEntryPoint
class VideoPlayerActivity : BaseActivity<ActivityVideoPlayerBinding>(),
    Player.Listener {

    val isFullScreen = false

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var sessionManager: SessionManager

    private var currentWindow: Int? = null
    private var playbackPosition: Long? = null
    private var player: SimpleExoPlayer? = null

    private var playDuration: Long? = null

    private var media: NoiseHealthVideo? = null
    var sportsModeRequest: SportsModeRequest? = null

    var startWatchActivity = false


    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, "noisefit")
    }

    companion object {
        fun getStartIntent(context: Context, video: NoiseHealthVideo, startWatchActivity:Boolean): Intent {
            return Intent(context, VideoPlayerActivity::class.java).apply {
                this.putExtra("video", video)
                this.putExtra("startWatchActivity",startWatchActivity)
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startWatchActivity = intent.getBooleanExtra("startWatchActivity",false)
        initializePlayer()

//        media = intent.getParcelableExtra<NoiseHealthVideo>("video")
        val url="https://media.geeksforgeeks.org/wp-content/uploads/20201217192146/Screenrecorder-2020-12-17-19-17-36-828.mp4?_=1".toString()
        val media=NoiseHealthVideo(
            duration = "5:00",
            image = "",
            categoryId = 1,
            subCategoryType = "video",
            mediaType = ".mp4",
            userPlaytime = 1,
            id = 1,
            source = url,
            title="Play Song",
            views = 10
        )

        media?.let {
            initUi(it)
            playVideo(it.source)
            setNoiseHealthContentView(it.id)


        }
    }


    private fun playVideo(source: String) {
        LOGS.d(source)
        val uri: Uri = Uri.parse(source)

        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
        player?.setMediaSource(videoSource)
        player?.prepare()
        player?.play()


        if (media?.userPlaytime != null) {
            player?.seekTo(media!!.userPlaytime * 1000L)
        }

        if(startWatchActivity){
            startYogaActivity()
        }
    }


    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                exoPlayer.playWhenReady = true
                exoPlayer.addListener(this)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            release()
        }
        player = null
    }

    private fun initUi(video: NoiseHealthVideo) {

        binding.tvVideoAbout.text = video.content
        binding.tvVideoTitle.text = video.title
        binding.tvVideoDuration.text = video.duration
        binding.tvVideoViews.text = "${video.views}"

        binding.tvVideoAbout.movementMethod = ScrollingMovementMethod()

    }

    override fun onBackPressed() {
        playDuration = player?.currentPosition
        setNoiseHealthContentPlayTime()
        super.onBackPressed()
    }

    override fun initListener() {
        binding.ivClose.setOnClickListener {
            playDuration = player?.currentPosition
            setNoiseHealthContentPlayTime()
            finish()
        }
        binding.ivPlay.setOnClickListener {
            if (player == null) return@setOnClickListener
            if (player!!.isPlaying) {
                binding.ivPlay.setImageResource(R.drawable.ic_video_play)
                player!!.pause()

                if(startWatchActivity){
                    pauseYogaActivity()
                }
            } else {
                binding.ivPlay.setImageResource(R.drawable.ic_video_pause)
                player!!.play()

                if(startWatchActivity){
                    resumeYogaActivity()
                }
            }

        }
        binding.ivBack.setOnClickListener {
            player?.seekTo(player!!.currentPosition - 10000)
        }
        binding.ivJump.setOnClickListener {
            player?.seekTo(player!!.currentPosition + 10000)
        }
        binding.ivFullScreen.setOnClickListener {
            if (media == null) return@setOnClickListener

            media!!.userPlaytime = try {
                (player!!.currentPosition / 1000L).toInt()
            } catch (exp: Exception) {
                media!!.userPlaytime
            }
//comment here for testing purpose
//            startActivityForResult(
//                LandscapeVideoPlayerActivity.getStartIntent(
//                    this,
//                    video = media!!
//                ), 444
//            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 444) {
            if (resultCode == RESULT_OK) {
                val duration = data?.getLongExtra("playDuration", player?.currentPosition ?: 0L)
                if (duration != null) {
                    player?.seekTo(duration)
                }
                player?.play()

                if (player == null) {
                    LOGS.d("Player is null")
                }
            }
        }
    }

    private fun setNoiseHealthContentPlayTime() {


        if(startWatchActivity){
            stopYogaActivity()
        }
        GlobalScope.launch(Dispatchers.IO) {

            val playSeconds = if (playDuration == null) {
                0
            } else {
                playDuration!! / 1000
            }

            val requestObject = JsonObject().apply {
                this.addProperty("sub_category_id", media?.id)
                this.addProperty("playtime_in_seconds", playSeconds)
            }
            //{"sub_category_id":51,"playtime_in_seconds":503.526}

            userRepository.setNoiseHealthContentPlayTime(requestObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {
                            LOGS.d("Done")
                        }
                    }
                    else -> {}
                }
            }

        }
    }

    private fun setNoiseHealthContentView(mediaId: Int) {
        GlobalScope.launch(Dispatchers.IO) {

            val requestObject = JsonObject().apply {
                this.addProperty("sub_category_id", mediaId)
            }

            userRepository.setNoiseHealthContentView(requestObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {
                            LOGS.d("Done")
                        }
                    }
                    else -> {}
                }
            }

        }
    }


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING)
            binding.pbVideo.visible()
        else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
            binding.pbVideo.gone()
            binding.ivPlay.visible()
            binding.ivBack.visible()
            binding.ivJump.visible()
        }
    }

    override fun observeSubscriber() {

    }

    override fun getViewBinding() = ActivityVideoPlayerBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? {
        return null
    }

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {
    }

    fun startYogaActivity() {
        sportsModeRequest = getInitialStateRequest()
        sportsModeRequest?.let {
            sessionManager.sendUserActivityAction(
                UserActivityAction.UpdateSportsMode(
                    it
                )
            )
        }
    }

    fun stopYogaActivity() {
        sportsModeRequest?.status = "stop"
        sportsModeRequest?.let {
            sessionManager.sendUserActivityAction(UserActivityAction.UpdateSportsMode(it))
        }
    }
    fun pauseYogaActivity() {
        sportsModeRequest?.status = "pause"
        sportsModeRequest?.let {
            sessionManager.sendUserActivityAction(
                UserActivityAction.UpdateSportsMode(
                    it
                )
            )
        }
    }
    fun resumeYogaActivity() {
        sportsModeRequest?.status = "resume"
        sportsModeRequest?.let {
            sessionManager.sendUserActivityAction(
                UserActivityAction.UpdateSportsMode(
                    it
                )
            )
        }
    }

    private fun getInitialStateRequest(): SportsModeRequest {
        val now = Calendar.getInstance()
        val sportsModeRequest = SportsModeRequest()
        sportsModeRequest.activityType = "yoga"
        sportsModeRequest.status = "start"
        sportsModeRequest.day = now.get(Calendar.DAY_OF_MONTH)
        sportsModeRequest.hour = now.get(Calendar.HOUR_OF_DAY)
        sportsModeRequest.minute = now.get(Calendar.MINUTE)
        sportsModeRequest.month = now.get(Calendar.MONTH) + 1
        sportsModeRequest.year = now.get(Calendar.YEAR)
        sessionManager.setSportsModeRequest(sportsModeRequest)
        return sportsModeRequest
    }

}