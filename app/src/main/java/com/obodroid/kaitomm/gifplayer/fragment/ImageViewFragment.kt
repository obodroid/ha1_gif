package com.obodroid.kaitomm.gifplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.obodroid.kaitomm.gifplayer.R

private const val ARG_RES_ID = "resId"

class ImageViewFragment : Fragment() {
    private var resId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            resId = it.getInt(ARG_RES_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view as ImageView).setImageResource(resId ?: R.color.black)
    }

    companion object {
        @JvmStatic
        fun newInstance(resId: Int = R.color.black) =
            ImageViewFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RES_ID, resId)
                }
            }
    }
}