package com.obodroid.kaitomm.gifplayer

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var gifImageView: ImageView
    private lateinit var currentEmotionText: TextView
    private lateinit var emotions: List<Emotion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        gifImageView = findViewById(R.id.gifImageView)
        currentEmotionText = findViewById(R.id.currentEmotionText)

        loadEmotions()
        setupRecyclerView()

        handleIntent()
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

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EmotionAdapter(emotions) { emotion ->
            showGif(emotion)
        }
    }

    private fun showGif(emotion: Emotion) {
        currentEmotionText.text = emotion.emotion
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

    private fun handleIntent() {
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
