package com.obodroid.kaitomm.gifplayer.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.obodroid.kaitomm.gifplayer.MainViewModel
import com.obodroid.kaitomm.gifplayer.R
import com.obodroid.kaitomm.gifplayer.data.model.EmotionData

class FaceFragment : Fragment() {

    private lateinit var faceImage: ImageView

    private val mapBitmap: MutableMap<String, GifDrawable> = mutableMapOf()
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_face, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        faceImage = view.findViewById(R.id.faceImage)

        loadGif(
            "connecting_3rd_party",
            R.raw.connecting_3rd_party_39,
            faceImage,
            isLoop = true,
            isCache = false
        )

        viewModel.emotion.observe(viewLifecycleOwner) {
            displayEmotion(it)
        }
    }

    override fun onDestroy() {
        faceImage?.let { Glide.with(this).clear(it) }
        super.onDestroy()
    }

    private fun loadGif(
        key: String,
        path: Any? = null,
        imageView: ImageView? = null,
        isLoop: Boolean = true,
        isCache: Boolean = false
    ) {
        if (isCache) {
            val bitmap = mapBitmap[key]
            if (bitmap != null) {
                setImageView(imageView, bitmap, isLoop)
                return
            }
        }
        Glide.with(this)
            .asGif()
            .load(path)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .fitCenter()
            .into(
                object : CustomTarget<GifDrawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(
                        resource: GifDrawable,
                        transition: Transition<in GifDrawable>?
                    ) {
                        if (isCache) {
                            mapBitmap[key] = resource
                        }
                        setImageView(imageView, resource, isLoop)
                    }

                })
    }

    private fun setImageView(imageView: ImageView?, drawable: GifDrawable, isLoop: Boolean) {
        imageView?.run {
            (imageView.drawable as GifDrawable?)?.stop()
            if (isLoop) {
                drawable.setLoopCount(GifDrawable.LOOP_INTRINSIC)
            } else {
                drawable.setLoopCount(1)
            }
            setImageDrawable(drawable)
            drawable.start()
        }
    }

    private fun displayEmotion(emotion: EmotionData) {
        if (emotion.emotionFilePath.isNullOrBlank()) {
            Glide.with(this).clear(faceImage)
            return
        }

        when (emotion.emotionText) {
            "wakeup",
            "idle" -> {
                viewModel.bottomText.postValue("")
                loadGif("idle", R.raw.idle_8, faceImage, isCache = true)
            }
            "sleep" -> {
                viewModel.bottomText.postValue("")
                loadGif(
                    emotion.emotionText, R.raw.sleep_9, faceImage,
                    isLoop = false,
                    isCache = true
                )
            }
            else -> loadGif(emotion.emotionText, emotion.emotionFilePath, faceImage)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FaceFragment()
    }
}
