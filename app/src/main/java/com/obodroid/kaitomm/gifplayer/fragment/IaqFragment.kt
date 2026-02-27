package com.obodroid.kaitomm.gifplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.obodroid.kaitomm.gifplayer.R

private const val ARG_PM25 = "pm25"
private const val ARG_PM10 = "pm10"
private const val ARG_TVOC = "tvoc"
private const val ARG_TEMP = "temp"
private const val ARG_CO2 = "co2"

class IaqFragment : Fragment() {

    private lateinit var pm25: TextView
    private lateinit var pm10: TextView
    private lateinit var tvoc: TextView
    private lateinit var temp: TextView
    private lateinit var co2: TextView
    private lateinit var imageView: ImageView

    private var pm25Val: String? = null
    private var pm10Val: String? = null
    private var tvocVal: String? = null
    private var tempVal: String? = null
    private var co2Val: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pm25Val = it.getString(ARG_PM25)
            pm10Val = it.getString(ARG_PM10)
            tvocVal = it.getString(ARG_TVOC)
            tempVal = it.getString(ARG_TEMP)
            co2Val = it.getString(ARG_CO2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_iaq, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pm25 = view.findViewById(R.id.pm25)
        pm10 = view.findViewById(R.id.pm10)
        tvoc = view.findViewById(R.id.tvoc)
        temp = view.findViewById(R.id.temp)
        co2 = view.findViewById(R.id.co2)
        imageView = view.findViewById(R.id.imageView)

        pm25.text = pm25Val
        pm10.text = pm10Val
        tvoc.text = tvocVal
        temp.text = tempVal
        co2.text = co2Val

        Glide.with(this)
            .asGif()
            .load(R.raw.iaq_info_30)
            .fitCenter()
            .into(imageView)
    }

    companion object {
        @JvmStatic
        fun newInstance(pm25: String, pm10: String, tvoc: String, temp: String, co2: String) =
            IaqFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PM25, pm25)
                    putString(ARG_PM10, pm10)
                    putString(ARG_TVOC, tvoc)
                    putString(ARG_TEMP, temp)
                    putString(ARG_CO2, co2)
                }
            }
    }
}
