package com.obodroid.kaitomm.gifplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmotionAdapter(
    private val emotions: List<Emotion>,
    private val onClick: (Emotion) -> Unit
) : RecyclerView.Adapter<EmotionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.emotionName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emotion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val emotion = emotions[position]
        holder.textView.text = "${position}: ${emotion.emotion}"
        holder.itemView.setOnClickListener { onClick(emotion) }
    }

    override fun getItemCount() = emotions.size
}
