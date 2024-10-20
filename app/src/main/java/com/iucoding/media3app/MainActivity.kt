package com.iucoding.media3app

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.Circle


class MainActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var forwardButton: ImageView
    private lateinit var backwardButton: ImageView
    private lateinit var playPauseButton: ImageView
    private lateinit var fullscreenButton: ImageView
    private lateinit var subtitlesButton: ImageView
    private lateinit var speedButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var bufferingProgressBar: ProgressBar
    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private var isFullScreen: Boolean = false
    private var speed = listOf(
        "0.5x" to 0.5f,
        "0.75x" to 0.75f,
        "1.0x" to 1.0f,
        "1.25x" to 1.25f,
        "1.5x" to 1.5f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        val exoPlayerBuilder = ExoPlayer.Builder(this)
        exoPlayer = exoPlayerBuilder.build()
        val mediaItem = MediaItem.fromUri(VIDEO_URI)
        playerView.player = exoPlayer
        exoPlayer.run {
            setMediaItem(mediaItem)
            prepare()
            setMediaItem(mediaItem)
            playWhenReady = true
            play()
        }

        backButton = playerView.findViewById(R.id.exo_back)
        forwardButton = playerView.findViewById(R.id.exo_forward)
        backwardButton = playerView.findViewById(R.id.exo_backward)
        playPauseButton = playerView.findViewById(R.id.exo_play_pause_button)
        fullscreenButton = playerView.findViewById(R.id.exo_full_screen)
        subtitlesButton = playerView.findViewById(R.id.exo_caption)
        titleText = playerView.findViewById(R.id.exo_title)
        bufferingProgressBar = findViewById(R.id.exo_spin_kit)
        speedButton = playerView.findViewById(R.id.exo_speed)
        titleText.text = "Big Buck Bunny"
        val circle: Sprite = Circle()
        bufferingProgressBar.indeterminateDrawable = circle

        playPauseButton.run {
            setOnClickListener {
                exoPlayer.playWhenReady = !exoPlayer.playWhenReady
                setImageResource(
                    if (exoPlayer.playWhenReady) R.drawable.pause
                    else R.drawable.play
                )
            }
        }
        forwardButton.setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition + 10000)
        }
        backwardButton.setOnClickListener {
            val num = exoPlayer.currentPosition - 10000
            if (num < 0) exoPlayer.seekTo(0)
            else exoPlayer.seekTo(num)
        }
        backButton.setOnClickListener {
            finish()
        }
        subtitlesButton.setOnClickListener {
            Toast.makeText(this, "Clicked on subtitles icon", Toast.LENGTH_SHORT).show()
        }
        fullscreenButton.setOnClickListener {
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
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                val params = playerView.layoutParams as ConstraintLayout.LayoutParams
                params.width = MATCH_PARENT
                params.height = MATCH_PARENT
                playerView.layoutParams = params
                isFullScreen = true
            }
        }
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                val isBuffering = Player.STATE_BUFFERING == playbackState
                bufferingProgressBar.isGone = !isBuffering
                playPauseButton.isInvisible = isBuffering
                forwardButton.isInvisible = isBuffering
                backwardButton.isInvisible = isBuffering
            }
        })
        speedButton.setOnClickListener {
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

    companion object {
        private const val VIDEO_URI = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    }
}
