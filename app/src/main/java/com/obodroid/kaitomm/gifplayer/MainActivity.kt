package com.obodroid.kaitomm.gifplayer

import android.content.Intent
import android.os.Bundle

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var gifImageView: ImageView
    private lateinit var emotions: List<Emotion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gifImageView = findViewById(R.id.gifImageView)

        loadEmotions()

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }


    private fun loadEmotions() {
        try {
            val inputStream = resources.openRawResource(R.raw.local_emotion)
            val reader = InputStreamReader(inputStream)
            val emotionMap = Gson().fromJson(reader, EmotionMap::class.java)
            emotions = emotionMap.emotions.filter { it.filename.isNotBlank() }
        } catch (e: Exception) {
            emotions = emptyList()
        }
    }

    private fun showGif(emotion: Emotion) {
        val resourceId = resources.getIdentifier(emotion.filename, "raw", packageName)
        if (resourceId != 0) {
            Glide.with(this)
                .asGif()
                .load(resourceId)
                .into(gifImageView)
        } else {
            gifImageView.setImageDrawable(null)
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra("finish", false)) {
            finish()
            return
        }
        val index = intent.getIntExtra("index", -1)

        if (index >= 0 && index < emotions.size) {
            showGif(emotions[index])
            return
        }

        val name = intent.getStringExtra("name")
        if (!name.isNullOrBlank()) {
            val emotion = emotions.find { it.emotion.equals(name, ignoreCase = true) }
            if (emotion != null) {
                showGif(emotion)
            }
        }
    }
}
