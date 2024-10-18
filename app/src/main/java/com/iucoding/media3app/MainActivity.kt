package com.iucoding.media3app

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var forwardButton: ImageView
    private lateinit var backwardButton: ImageView
    private lateinit var playPauseButton: ImageView
    private lateinit var fullscreenButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var bufferingProgressBar: ProgressBar
    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private var isFullScreen: Boolean = false

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
        titleText = playerView.findViewById(R.id.exo_title)
        bufferingProgressBar = findViewById(R.id.exo_buffering_progress_bar)
        titleText.text = "Big Buck Bunny"

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
        fullscreenButton.setOnClickListener {
            if (isFullScreen) {
                //titleText.visibility = View.INVISIBLE
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
                //titleText.visibility = View.VISIBLE
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
                bufferingProgressBar.isGone = Player.STATE_BUFFERING != playbackState
            }
        })
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
