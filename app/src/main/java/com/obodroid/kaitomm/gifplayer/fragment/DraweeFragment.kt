package com.obodroid.kaitomm.gifplayer.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.obodroid.kaitomm.gifplayer.R

private const val ARG_IMAGE_ID = "imageId"

class DraweeFragment : Fragment() {

    private lateinit var draweeView: SimpleDraweeView

    private var imageId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageId = it.getInt(ARG_IMAGE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drawee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        draweeView = view.findViewById(R.id.draweeView)

        imageId?.let {
            loadGif(it, draweeView)
        }
    }

    private fun loadGif(path: Any, draweeView: SimpleDraweeView) {
        val builder = when (path) {
            is String -> {
                Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(path))

            }

            else -> {
                Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse("android.resource://${context?.packageName}/$path"))
            }
        }
        val controller = builder
            .setAutoPlayAnimations(true)
            .setOldController(draweeView.controller)
            .build()
        draweeView.controller = controller
    }

    companion object {
        @JvmStatic
        fun newInstance(imageId: Int) =
            DraweeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_IMAGE_ID, imageId)
                }
            }
    }
}