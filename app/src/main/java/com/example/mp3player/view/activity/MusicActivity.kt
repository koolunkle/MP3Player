package com.example.mp3player.view.activity

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mp3player.R
import com.example.mp3player.data.Music
import com.example.mp3player.databinding.ActivityMusicBinding
import kotlinx.coroutines.*
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*

class MusicActivity : AppCompatActivity() {

    private var _binding: ActivityMusicBinding? = null

    private val binding get() = _binding!!

    private var position: Int = 0

    private var music: Music? = null

    private var musicList: MutableList<Parcelable>? = null

    private var mediaPlayer: MediaPlayer? = null

    private var mediaJob: Job? = null

    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_music)
        binding.apply {
            activity = this@MusicActivity
            setMusicWindow()
            setMusicVolume()
        }
    }

    private fun setMusicVolume() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.seekBarVolume.apply {
            max = maxVolume
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, flag: Boolean) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_SHOW_UI
                    )
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    Log.d("MusicActivity", "onStartTrackingTouch")
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    Log.d("MusicActivity", "onStopTrackingTouch")
                }
            })
        }
    }

    fun setPlay(view: View) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            binding.btnPlay.setImageResource(R.drawable.ic_play_24)
        } else {
            mediaPlayer?.start()
            binding.btnPlay.setImageResource(R.drawable.ic_pause_24)
        }
        val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
        var currentPosition: Int
        var currentDuration: String

        mediaJob = backgroundScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                currentPosition = mediaPlayer?.currentPosition!!
                binding.seekBarDuration.progress = currentPosition
                runOnUiThread {
                    currentDuration = SimpleDateFormat(
                        "mm:ss", Locale.getDefault()
                    ).format(mediaPlayer!!.currentPosition)
                    binding.tvDuration.text = currentDuration
                }
                try {
                    delay(1000)
                } catch (e: Exception) {
                    Log.d("MusicActivity", "${e.printStackTrace()}")
                }
                mediaPlayer?.setOnCompletionListener {
                    setNext(view)
                }
            }
            binding.btnPlay.setImageResource(R.drawable.ic_play_24)
        }
    }

    fun setStop(view: View) {
        mediaJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer = MediaPlayer.create(view.context, music?.getMusicUri())
        binding.tvDuration.text = resources.getString(R.string.tv_duration_start)
        binding.btnPlay.setImageResource(R.drawable.ic_play_24)
        binding.seekBarDuration.progress = 0
    }

    fun setPrevious(view: View) {
        mediaJob?.cancel()
        mediaPlayer?.stop()
        position = getPosition(PREVIOUS, position)
        music = musicList?.get(position) as Music
        backgroundService(view, setMusicWindow(music))
    }

    fun setNext(view: View) {
        mediaJob?.cancel()
        mediaPlayer?.stop()
        position = getPosition(NEXT, position)
        music = musicList?.get(position) as Music
        backgroundService(view, setMusicWindow(music))
    }

    fun setShuffle(view: View) {
        mediaJob?.cancel()
        mediaPlayer?.stop()
        position = SecureRandom().nextInt(musicList?.size!!)
        music = musicList?.get(position) as Music
        backgroundService(view, setMusicWindow(music))
    }

    private fun backgroundService(view: View, mediaPlayer: MediaPlayer?) {
        mediaPlayer?.start()
        binding.btnPlay.setImageResource(R.drawable.ic_pause_24)

        val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
        var currentPosition: Int
        var currentDuration: String

        mediaJob = backgroundScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                currentPosition = mediaPlayer.currentPosition
                binding.seekBarDuration.progress = currentPosition
                runOnUiThread {
                    currentDuration = SimpleDateFormat(
                        "mm:ss", Locale.getDefault()
                    ).format(mediaPlayer.currentPosition)
                    binding.tvDuration.text = currentDuration
                }
                try {
                    delay(1000)
                } catch (e: Exception) {
                    Log.d("MusicActivity", "${e.printStackTrace()}")
                }
                mediaPlayer.setOnCompletionListener {
                    setNext(view)
                }
            }
            binding.btnPlay.setImageResource(R.drawable.ic_play_24)
        }
    }

    private fun getPosition(option: Int, position: Int): Int {
        var newPosition = 0
        when (position) {
            FIRST_INDEX -> newPosition = if (option == PREVIOUS) musicList!!.size - 1
            else position + 1

            in 1 until (musicList!!.size - 1) -> newPosition = if (option == PREVIOUS) position - 1
            else position + 1

            musicList!!.size - 1 -> newPosition = if (option == PREVIOUS) position - 1
            else 0
        }
        return newPosition
    }

    private fun setMusicWindow(music: Music?): MediaPlayer? {
        mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
        binding.tvTitle.text = music?.title
        binding.tvArtist.text = music?.artist
        binding.tvDuration.text = resources.getString(R.string.tv_duration_start)
        binding.tvDurationTotal.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(music?.duration)

        val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
        if (bitmap != null) binding.ivAlbum.setImageBitmap(bitmap)
        else binding.ivAlbum.setImageResource(R.drawable.music_note)

        binding.seekBarDuration.apply {
            max = mediaPlayer?.duration!!
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, flag: Boolean) {
                    if (flag) mediaPlayer?.seekTo(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    Log.d("MusicActivity", "onStartTrackingTouch")
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    Log.d("MusicActivity", "onStopTrackingTouch")
                }
            })
        }
        return mediaPlayer
    }

    private fun setMusicWindow() {
        musicList = intent.getParcelableArrayListExtra("playlist")
        position = intent.getIntExtra("position", 0)
        music = musicList?.get(position) as Music
        mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())

        binding.tvTitle.text = music?.title
        binding.tvArtist.text = music?.artist
        binding.tvDuration.text = resources.getString(R.string.tv_duration_start)
        binding.tvDurationTotal.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(music?.duration)

        val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
        if (bitmap != null) binding.ivAlbum.setImageBitmap(bitmap)
        else binding.ivAlbum.setImageResource(R.drawable.music_note)

        binding.seekBarDuration.apply {
            max = mediaPlayer?.duration!!
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, flag: Boolean) {
                    if (flag) mediaPlayer?.seekTo(progress)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    Log.d("MusicActivity", "onStartTrackingTouch")
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    Log.d("MusicActivity", "onStopTrackingTouch")
                }
            })
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mediaJob?.cancel()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        const val ALBUM_SIZE = 700
        const val PREVIOUS = 1001
        const val NEXT = 9009
        const val FIRST_INDEX = 0
    }

}