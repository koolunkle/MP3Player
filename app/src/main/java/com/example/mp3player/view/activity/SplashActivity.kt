package com.example.mp3player.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mp3player.R
import com.example.mp3player.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.apply { initView() }
    }

    private fun initView() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            startActivity(Intent(this, ListActivity::class.java))
            finish()
        }, 3500)
    }

}