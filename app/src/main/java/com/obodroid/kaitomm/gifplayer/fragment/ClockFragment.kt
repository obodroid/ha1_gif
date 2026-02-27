package com.obodroid.kaitomm.gifplayer.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.obodroid.kaitomm.gifplayer.R
import java.text.SimpleDateFormat
import java.util.*


class ClockFragment : Fragment() {

    private lateinit var colonTextView: TextView
    private lateinit var hourTextView: TextView
    private lateinit var minuteTextView: TextView

    private var isShowColon = true

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setTime()
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            if (colonTextView != null) {
                colonTextView.text = if (isShowColon) "   " else " : "
                isShowColon = !isShowColon
                colonTextView.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colonTextView = view.findViewById(R.id.colonTextView)
        hourTextView = view.findViewById(R.id.hourTextView)
        minuteTextView = view.findViewById(R.id.minuteTextView)

        setTime()
    }

    override fun onStart() {
        super.onStart()
        colonTextView.post(runnable)
        context?.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onStop() {
        super.onStop()
        colonTextView.removeCallbacks(runnable)
        context?.unregisterReceiver(broadcastReceiver)
    }

    private fun setTime() {
        val date = Date()
        hourTextView.text = SimpleDateFormat("HH", Locale.getDefault()).format(date)
        minuteTextView.text = SimpleDateFormat("mm", Locale.getDefault()).format(date)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ClockFragment()
    }
}
