package com.iucoding.media3app

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.Circle


class MainActivity : AppCompatActivity() {

    // region view refs
    private lateinit var backButton: ImageView
    private lateinit var forwardButton: ImageView
    private lateinit var backwardButton: ImageView
    private lateinit var playPauseButton: ImageView
    private lateinit var fullscreenButton: ImageView
    private lateinit var subtitlesButton: ImageView
    private lateinit var speedButton: ImageView
    private lateinit var resizeButton: ImageView
    private lateinit var qualityButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var bufferingProgressBar: ProgressBar
    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    // endregion

    // region listeners
    private val playbackStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            handlePlaybackStateChange(playbackState)
        }
    }
    // endregion

    // region variables
    private var isFullScreen: Boolean = false
    private var isShowingTrackSelection: Boolean = false
    private var resizeMode = 0
    // endregion

    // region Activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        setupExoPlayer()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        exoPlayer.play()
    }

    override fun onPause() {
        exoPlayer.pause()
        super.onPause()
    }

    override fun onDestroy() {
        exoPlayer.release()
        super.onDestroy()
    }
    // endregion

    // region private functions
    private fun setupViews() {
        playerView = findViewById(R.id.playerView)
        backButton = playerView.findViewById(R.id.exo_back)
        forwardButton = playerView.findViewById(R.id.exo_forward)
        backwardButton = playerView.findViewById(R.id.exo_backward)
        playPauseButton = playerView.findViewById(R.id.exo_play_pause_button)
        fullscreenButton = playerView.findViewById(R.id.exo_full_screen)
        subtitlesButton = playerView.findViewById(R.id.exo_caption)
        titleText = playerView.findViewById(R.id.exo_title)
        bufferingProgressBar = findViewById(R.id.exo_spin_kit)
        speedButton = playerView.findViewById(R.id.exo_speed)
        resizeButton = playerView.findViewById(R.id.exo_resize)
        qualityButton = playerView.findViewById(R.id.exo_quality_setting)

        titleText.text = "Big Buck Bunny"
        val circle: Sprite = Circle()
        bufferingProgressBar.indeterminateDrawable = circle
    }

    private fun setupExoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(VIDEO_URI)
        playerView.player = exoPlayer
        exoPlayer.run {
            setMediaItem(mediaItem)
            prepare()
            setMediaItem(mediaItem)
            playWhenReady = true
            play()
        }
    }

    private fun setupListeners() {
        exoPlayer.addListener(playbackStateListener)
        playPauseButton.setOnClickListener { handlePlayPauseClick() }
        forwardButton.setOnClickListener { handleForwardClick() }
        backwardButton.setOnClickListener { handleBackwardClick() }
        backButton.setOnClickListener { finish() }
        subtitlesButton.setOnClickListener { handleSubtitlesClick() }
        fullscreenButton.setOnClickListener { handleFullScreenClick() }
        speedButton.setOnClickListener { showSpeedSelectionDialog() }
        playerView.setControllerVisibilityListener(ControllerVisibilityListener {
            handleControllerVisibilityChange()
        })
        resizeButton.setOnClickListener { handleResizing() }
        qualityButton.setOnClickListener { showTrackSelectionDialog() }
    }

    private fun handlePlaybackStateChange(playbackState: Int) {
        val isBuffering = Player.STATE_BUFFERING == playbackState
        bufferingProgressBar.isGone = !isBuffering
        playPauseButton.isInvisible = isBuffering
        forwardButton.isInvisible = isBuffering
        backwardButton.isInvisible = isBuffering
    }
    // endregion

    // region click handlers
    private fun handlePlayPauseClick() {
        exoPlayer.playWhenReady = !exoPlayer.playWhenReady
        playPauseButton.setImageResource(
            if (exoPlayer.playWhenReady) R.drawable.pause
            else R.drawable.play
        )
    }

    private fun handleForwardClick() {
        exoPlayer.seekTo(exoPlayer.currentPosition + 10000)
    }

    private fun handleBackwardClick() {
        val num = exoPlayer.currentPosition - 10000
        if (num < 0) exoPlayer.seekTo(0)
        else exoPlayer.seekTo(num)
    }

    private fun handleSubtitlesClick() {
        Toast.makeText(this, "Clicked on subtitles icon", Toast.LENGTH_SHORT).show()
    }

    private fun handleFullScreenClick() {
        if (isFullScreen) {
            fullscreenButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.fullscreenclose)
            )
            supportActionBar?.show()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val params = playerView.layoutParams as ConstraintLayout.LayoutParams
            params.width = MATCH_PARENT
            params.height = (200 * resources.displayMetrics.density).toInt()
            playerView.layoutParams = params
            isFullScreen = false

        } else {
            fullscreenButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.fullscreenopen)
            )
            supportActionBar?.hide()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            val params = playerView.layoutParams as ConstraintLayout.LayoutParams
            params.width = MATCH_PARENT
            params.height = MATCH_PARENT
            playerView.layoutParams = params
            isFullScreen = true
        }
    }

    private fun showSpeedSelectionDialog() {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle("Set speed")
            setItems(speed.map { it.first }.toTypedArray()) { _, which ->
                speed.getOrNull(which)?.second?.let { selectedSpeed ->
                    val params = PlaybackParameters(selectedSpeed)
                    exoPlayer.playbackParameters = params
                }
            }
        }
        dialog.show()
    }

    private fun handleControllerVisibilityChange() {
        if (isFullScreen) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            isFullScreen = true
        }
    }

    @OptIn(UnstableApi::class)
    private fun handleResizing() {
        playerView.resizeMode = resize[resizeMode]
        resizeMode = (resizeMode + 1) % 4
        println("resizeMode: $resizeMode")
    }

    private fun showTrackSelectionDialog() {
        if (!isShowingTrackSelection && TrackSelectionDialog.willHaveContent(exoPlayer)) {
            isShowingTrackSelection = true
            val trackSelectionDialog = TrackSelectionDialog.createForPlayer(
                exoPlayer
            ) { _: DialogInterface? ->
                isShowingTrackSelection = false
            }
            trackSelectionDialog.show(supportFragmentManager, null)
        }
    }
    // endregion

    companion object {
        private const val VIDEO_URI = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        private var speed = listOf(
            "0.5x" to 0.5f, "0.75x" to 0.75f, "1.0x" to 1.0f, "1.25x" to 1.25f, "1.5x" to 1.5f
        )

        @SuppressLint("UnsafeOptInUsageError")
        private var resize = listOf(
            AspectRatioFrameLayout.RESIZE_MODE_FILL,
            AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT,
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
            AspectRatioFrameLayout.RESIZE_MODE_FIT
        )
    }
}
