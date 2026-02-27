package com.obodroid.kaitomm.gifplayer

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.obodroid.kaitomm.gifplayer.common.ViewModelFactory
import com.obodroid.kaitomm.gifplayer.common.configurations.StatusCode
import com.obodroid.kaitomm.gifplayer.common.extensions.*
import com.obodroid.kaitomm.gifplayer.common.intents.ActionIntent
import com.obodroid.kaitomm.gifplayer.common.setting.BatteryStatus
import com.obodroid.kaitomm.gifplayer.common.setting.HomeAutomationStatus
import com.obodroid.kaitomm.gifplayer.common.setting.VolumeStatus
import com.obodroid.kaitomm.gifplayer.common.setting.WifiStatus
import com.obodroid.kaitomm.gifplayer.data.model.EmotionData
import com.obodroid.kaitomm.gifplayer.fragment.FaceFragment
import com.obodroid.kaitomm.gifplayer.fragment.IaqFragment
import com.obodroid.kaitomm.gifplayer.fragment.JooxWebViewFragment
import com.obodroid.kaitomm.gifplayer.service.DataPackDownloadBroadcastReceiver
import com.obodroid.kaitomm.gifplayer.service.GifPlayerService
import org.json.JSONObject
import timber.log.Timber
import java.util.*

abstract class BaseMainActivity : BaseActivity() {

    private lateinit var text: TextView
    private lateinit var emotion_text: TextView
    private lateinit var volumeStatus: ImageView
    private lateinit var wifiStatus: ImageView
    private lateinit var homeStatus: ImageView
    private lateinit var batteryStatus: ImageView

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory {
            MainViewModel(
                (applicationContext as MainApplication).deviceRepository,
                (applicationContext as MainApplication).pushNotificationApiController
            )
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("action: ${intent.action}")
            when (intent.action) {
                ActionIntent.ACTION_ON_DISPLAY_EMOTION -> {
                    val emotion = intent.getSerializableExtra(EXTRA_EMOTION) as EmotionData
                    val extraMessage = intent.getStringExtra(EXTRA_MESSAGE)

                    removeFragment()

                    if (!extraMessage.isNullOrEmpty()) {
                        val extraMessageObj = JSONObject(extraMessage)
                        val pm25 = extraMessageObj["pm25"].toString()
                        val pm10 = extraMessageObj["pm10"].toString()
                        val tvoc = extraMessageObj["tvoc"].toString()
                        val temp = extraMessageObj["temp"].toString()
                        val co2 = extraMessageObj["co2"].toString()
                        showIaqInfo(
                            pm10Val = pm10,
                            pm25Val = pm25,
                            tvocVal = tvoc,
                            tempVal = temp,
                            co2Val = co2
                        )
                        return
                    }

                    viewModel.emotion.postValue(emotion)

                    handler.removeCallbacks(showScreenSaverRunnable)
                    handler.postDelayed(showScreenSaverRunnable, showScreenSaverTimeout)
                }
                ActionIntent.ACTION_ON_DISPLAY_TEXT -> {
                    val extraText = intent.getStringExtra(EXTRA_TEXT)
                    logger.debug("extraText $extraText")
                    displayTextOrStartSlotMachine(extraText)
                }
                ActionIntent.ACTION_ON_DISPLAY_BOTTOM_TEXT -> {
                    intent.getStringExtra(EXTRA_TEXT)?.let {
                        viewModel.bottomText.postValue(it)
                    }
                }
                ActionIntent.ACTION_FINISH -> {
                    finish()
                }
                ActionIntent.ACTION_MEDIA_SETUP_PLAYER -> {
                    val token = intent.getStringExtra(EXTRA_MEDIA_TOKEN)
                    setupMediaPlayWebView(token)
                }
                ActionIntent.ACTION_PLAY_VIDEO -> {
                    startActivity<VideoActivity> {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.extras?.let { putExtras(it) }
                    }
                }
                ActionIntent.ACTION_SETTING -> {
                    intent.extras?.let {
                        updateSetting(it)
                    }
                }
            }
        }
    }

    private val handler = Handler(Looper.myLooper() ?: Looper.getMainLooper())
    private val showScreenSaverTimeout = 3 * 60 * 1000L

    abstract val showScreenSaverRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        text = findViewById(R.id.text)
        emotion_text = findViewById(R.id.emotion_text)
        volumeStatus = findViewById(R.id.volumeStatus)
        wifiStatus = findViewById(R.id.wifiStatus)
        homeStatus = findViewById(R.id.homeStatus)
        batteryStatus = findViewById(R.id.batteryStatus)

        setupObserver()

        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            text,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            emotion_text,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )

        val faceFragment = FaceFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_container, faceFragment, "FaceFragment")
            .commit()

        registerReceiverFilter(
            broadcastReceiver,
            listOf(
                ActionIntent.ACTION_ON_DISPLAY_EMOTION,
                ActionIntent.ACTION_ON_DISPLAY_TEXT,
                ActionIntent.ACTION_ON_DISPLAY_BOTTOM_TEXT,
                ActionIntent.ACTION_FINISH,
                ActionIntent.ACTION_MEDIA_SETUP_PLAYER,
                ActionIntent.ACTION_PLAY_VIDEO,
                ActionIntent.ACTION_SETTING
            )
        )

        scheduleDataPackDownload()

        // setBrightness
        val window = window
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 1f
        window.attributes = layoutParams

        // Start other application
        broadcast("com.obodroid.kaitomm.ACTION_START_LAUNCHER") {
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        }

        registerPushNotification()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.e("onLowMemory")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Timber.e("onTrimMemory level: $level")
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun setupObserver() {
        viewModel.bottomText.observe(this) {
            emotion_text.text = it
            emotion_text.visibility = VISIBLE
        }

        viewModel.showScreenSaver.observe(this) {
            if (it) {
                showScreenSaverRunnable.run()
            }
        }

        viewModel.volumeStatus.observe(this) {
            val drawable = when (VolumeStatus.valueOf(it)) {
                VolumeStatus.LOW -> R.drawable.ic_volume_down
                else -> R.drawable.ic_volume_up
            }
            volumeStatus.setImageResource(drawable)
        }

        viewModel.wifiStatus.observe(this) {
            val wifiStatusValue = WifiStatus.valueOf(it)
            if (wifiStatusValue.level > 0) {
                registerPushNotification()
            }
            val drawable = when (wifiStatusValue) {
                WifiStatus.LEVEL_1 -> R.drawable.ic_wifi_1_bar
                WifiStatus.LEVEL_2 -> R.drawable.ic_wifi_2_bar
                WifiStatus.LEVEL_3 -> R.drawable.ic_wifi
                else -> R.drawable.ic_wifi_off
            }
            wifiStatus.setImageResource(drawable)
        }

        viewModel.homeAutomationStatus.observe(this) {
            val drawable = when (HomeAutomationStatus.valueOf(it)) {
                HomeAutomationStatus.OFF -> R.drawable.ic_domain_disabled
                HomeAutomationStatus.ON -> R.drawable.ic_domain
                else -> android.R.color.transparent
            }
            homeStatus.setImageResource(drawable)
        }

        viewModel.batteryStatus.observe(this) {
            val drawable = when (BatteryStatus.valueOf(it)) {
                BatteryStatus.EMPTY -> R.drawable.ic_battery_0_bar
                BatteryStatus.LEVEL_1 -> R.drawable.ic_battery_1_bar
                BatteryStatus.LEVEL_2 -> R.drawable.ic_battery_2_bar
                BatteryStatus.LEVEL_3 -> R.drawable.ic_battery_3_bar
                BatteryStatus.LEVEL_4 -> R.drawable.ic_battery_4_bar
                BatteryStatus.LEVEL_5 -> R.drawable.ic_battery_5_bar
                BatteryStatus.LEVEL_6 -> R.drawable.ic_battery_6_bar
                BatteryStatus.FULL -> R.drawable.ic_battery_full
                BatteryStatus.CHARGING -> R.drawable.ic_battery_charging
                else -> android.R.color.transparent
            }
            batteryStatus.setImageResource(drawable)
        }

        viewModel.loadLocalData()
    }

    private fun registerPushNotification() {
        viewModel.fetchFirebaseInstanceId()
    }

    open fun onClick(view: View) {
    }

    fun showIaqInfo(
        pm25Val: String,
        pm10Val: String,
        tvocVal: String,
        tempVal: String,
        co2Val: String
    ) {
        try {
            supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(
                    R.id.fragment_container,
                    IaqFragment.newInstance(pm25Val, pm10Val, tvocVal, tempVal, co2Val),
                    "IaqFragment"
                )
                .commit()
        } catch (e: Throwable) {
            logger.verbose("showIaqInfo status: catch")
        }
    }

    fun removeFragment() {
        try {
            logger.verbose("removeFragment")
            val transaction = supportFragmentManager.beginTransaction()
            for (fragment in supportFragmentManager.fragments) {
                if (fragment.id == R.id.fragment_container && fragment.tag != "FaceFragment") {
                    transaction.remove(fragment)
                }
            }
            transaction.commit()
        } catch (e: Exception) {
            logger.verbose("removeFragment status: catch")
        }
    }

    private fun displayTextOrStartSlotMachine(extraText: String?) {
        when (extraText) {
            else -> {
                text.text = extraText
                text.visibility = VISIBLE

                Handler().postDelayed({
                    text.visibility = GONE
                }, 2000)
            }
        }
    }

    private fun showSlotMachine() {
        val intent = Intent(this, SlotMachineActivity::class.java)
        intent.putExtra("start", false)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun scheduleDataPackDownload() {
        val pendingIntent =
            createBroadcastPendingIntent(DataPackDownloadBroadcastReceiver::class.java)
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 2)

        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        pendingIntent.send()

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        logger.debug("alarmManager = $alarmManager")
    }

    private fun updateSetting(bundle: Bundle) {
        for (key in bundle.keySet()) {
            Timber.d("updateSetting key: $key")
            viewModel.updateSetting(key, bundle.getString(key))
        }
    }

    private fun setupMediaPlayWebView(token: String?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.webViewContainer)
        if (fragment == null) {
            val bundle = Bundle()
            bundle.putString(JooxWebViewFragment.ARG_TOKEN, token)
            try {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.webViewContainer, JooxWebViewFragment::class.java, bundle)
                }
                Timber.tag("media").i("Setup Joox player web view")
            } catch (e: Throwable) {
                Timber.tag("media").e( "Setup Joox player web view result: FAILURE reason: 'Fragment'")
                runService<GifPlayerService>(action = ActionIntent.ACTION_MEDIA_ON_ERROR) {
                    putExtra(JooxWebViewFragment.EXTRA_MEDIA_ERROR, StatusCode.ERROR_FRAGMENT.toString())
                }
            }
        } else {
            runService<GifPlayerService>(action = ActionIntent.ACTION_MEDIA_ON_PLAYER_READY)
            Timber.tag("media").i("Already setup Joox player")
        }
    }

    companion object {
        const val EXTRA_EMOTION = "EXTRA_EMOTION"
        const val EXTRA_TEXT = "EXTRA_TEXT"
        const val EXTRA_DURATION_TIME = "EXTRA_DURATION_TIME"
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        const val EXTRA_MEDIA_TOKEN = "EXTRA_MEDIA_TOKEN"
    }
}
