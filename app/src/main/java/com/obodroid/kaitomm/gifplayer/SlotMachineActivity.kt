package com.obodroid.kaitomm.gifplayer

import android.app.ActionBar.LayoutParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewFlipper
import com.obodroid.kaitomm.gifplayer.common.intents.ActionIntent
import kotlin.random.Random

class SlotMachineActivity : BaseActivity() {

    private lateinit var text: TextView
    private lateinit var bckgrndViewFlipper1: ViewFlipper
    private lateinit var bckgrndViewFlipper2: ViewFlipper
    private lateinit var bckgrndViewFlipper3: ViewFlipper

    val START_SLOT = "com.obodroid.slotMachine.SlotMachineBroadCastReceiver.START_SLOT"

    private val duration1 = 100
    private val duration2 = 100
    private val duration3 = 100
    private val handler = Handler()

    var mediaPlayer: MediaPlayer? = null
    var mAudioManager: AudioManager? = null

    private val picArray = intArrayOf(
        R.drawable.alarm,
        R.drawable.camera,
        R.drawable.cloud,
        R.drawable.home_automation,
        R.drawable.kaitomm,
        R.drawable.mail,
        R.drawable.mantra,
        R.drawable.music,
        R.drawable.obodroid,
        R.drawable.pm25,
        R.drawable.reminder,
        R.drawable.storytale,
        R.drawable.traffic,
        R.drawable.wifi,
        R.drawable.sun,
        R.drawable.podcast
    )

    private fun displayTextOrStartSlotMachine(extraText: String?) {
        logger.debug("******")
        if(extraText=="START_SLOT") {
            playSlotMachine()
        }
        else {
            text.text = extraText
            text.visibility = View.VISIBLE

            Handler().postDelayed({
                text.visibility = View.GONE
            }, 2000)
        }
    }

    fun playSlotMachine() {
        val intent = Intent(this, SlotMachineActivity::class.java)
        intent.putExtra("start",true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        logger.debug("intent $intent")
        startActivity(intent)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {

        var mediaPlayer: MediaPlayer? = null

        override fun onReceive(context: Context, intent: Intent) {
            logger.debug("onReceive ${intent.action}")
            when (intent.action) {

                ActionIntent.ACTION_ON_DISPLAY_TEXT -> {
                    val extraText = intent.getStringExtra(BaseMainActivity.EXTRA_TEXT)
                    displayTextOrStartSlotMachine(extraText)
                }
                ActionIntent.ACTION_FINISH -> {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slot_machine)

        text = findViewById(R.id.text)
        bckgrndViewFlipper1 = findViewById(R.id.bckgrndViewFlipper1)
        bckgrndViewFlipper2 = findViewById(R.id.bckgrndViewFlipper2)
        bckgrndViewFlipper3 = findViewById(R.id.bckgrndViewFlipper3)

        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.slot_sound)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (intent.getBooleanExtra("start", true)) {
            startSlot()
        }

    }

    fun startSlot() {
        mediaPlayer?.start()
        startSlotExternal()
    }

    fun addImageViewAndSetFlip(viewFlipper: ViewFlipper?, flipInterval: Int) {
        picArray.shuffle(Random(0))
        viewFlipper?.removeAllViews()
        for (element in picArray) {
            val imageView = ImageView(this)
            imageView.layoutParams =
                LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.setImageResource(element)
            viewFlipper?.addView(imageView)
        }
        viewFlipper?.setFlipInterval(flipInterval)
        viewFlipper?.setInAnimation(applicationContext, R.anim.slide_down_in)
        viewFlipper?.setOutAnimation(applicationContext, R.anim.slide_down_out)
        viewFlipper?.isAutoStart = true
        viewFlipper?.startFlipping()
    }

    private fun IntArray.shuffle(rnd: Random) {
        // Fisher-Yates shuffle algorithm
        for (i in this.size - 1 downTo 1) {
            val j = rnd.nextInt(i + 1)
            val temp = this[i]
            this[i] = this[j]
            this[j] = temp
        }
    }

    fun startSlotExternal() {
        addImageViewAndSetFlip(bckgrndViewFlipper1, duration1)
        addImageViewAndSetFlip(bckgrndViewFlipper2, duration2)
        addImageViewAndSetFlip(bckgrndViewFlipper3, duration3)

        handler.postDelayed({
            bckgrndViewFlipper1.stopFlipping()
        }, 5_000)
        handler.postDelayed({
            bckgrndViewFlipper2.stopFlipping()
        }, 6_000)
        handler.postDelayed({
            bckgrndViewFlipper3.stopFlipping()
//                startFadeOut()
        }, 7_000)
    }
}