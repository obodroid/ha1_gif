package com.obodroid.kaitomm.gifplayer.fragment

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.obodroid.kaitomm.gifplayer.R

class SlideshowFragment : Fragment() {

    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_slideshow, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.imageView)

        Log.e("SlideshowFragment", "onActivityCreated")
        imageView.setBackgroundResource(R.drawable.image_slideshow)
        val animationDrawable = imageView.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(500)
        animationDrawable.setExitFadeDuration(500)
        animationDrawable.start()
    }

    companion object {
        @JvmStatic
        fun newInstance() = SlideshowFragment()
    }
}