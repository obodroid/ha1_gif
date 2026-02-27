package com.obodroid.kaitomm.gifplayer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.iid.FirebaseInstanceId
import com.obodroid.kaitomm.gifplayer.common.setting.SettingKey
import com.obodroid.kaitomm.gifplayer.common.extensions.subscribeEx
import com.obodroid.kaitomm.gifplayer.data.apicontroller.PushNotificationMappingApiController
import com.obodroid.kaitomm.gifplayer.data.model.EmotionData
import com.obodroid.kaitomm.gifplayer.data.repository.DeviceRepository
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MainViewModel(
    private val deviceRepository: DeviceRepository,
    private val pushNotificationApiController: PushNotificationMappingApiController
) : ViewModel() {
    val emotion: MutableLiveData<EmotionData> = MutableLiveData()

    val displayText: MutableLiveData<String> = MutableLiveData()
    val bottomText: MutableLiveData<String> = MutableLiveData()
    val showScreenSaver: MutableLiveData<Boolean> = MutableLiveData(false)

    val volumeStatus: MutableLiveData<String> = MutableLiveData()
    val wifiStatus: MutableLiveData<String> = MutableLiveData()
    val homeAutomationStatus: MutableLiveData<String> = MutableLiveData()
    val batteryStatus: MutableLiveData<String> = MutableLiveData()

    val firebaseInstanceId: MutableLiveData<String?> = MutableLiveData<String?>().also {
        val value = deviceRepository.getFirebaseInstanceId() ?: return@also
        it.postValue(value)
    }

    fun loadLocalData() {
        for (key in SettingKey.values()) {
            val value = deviceRepository.getStatus(key)
            when (key) {
                SettingKey.Volume -> volumeStatus.postValue(value)
                SettingKey.WifStatus -> wifiStatus.postValue(value)
                SettingKey.HomeAutomationStatus -> homeAutomationStatus.postValue(value)
                SettingKey.Battery -> batteryStatus.postValue(value)
                else -> {
                }
            }
        }
    }

    fun fetchFirebaseInstanceId() {
        if (firebaseInstanceId.value != null) {
            return
        }
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.e(task.exception, "getInstanceId failed")
                    return@addOnCompleteListener
                }
                val token = task.result?.token ?: return@addOnCompleteListener
                pushNotificationApiController.registerPushnotification(token)
                    .subscribeOn(Schedulers.io())
                    .subscribeEx(
                        onSuccess = {
                            deviceRepository.updateFirebaseInstanceId(token)
                            firebaseInstanceId.postValue(token)
                            Timber.d("Register push notification with firebase token: $token success!")
                        }
                    )
            }
    }

    fun updateSetting(key: String, value: String?) {
        val settingKey = SettingKey.valueOf(key)
        Timber.d("updateSetting key: $settingKey value: $value")
        when (settingKey) {
            SettingKey.screensaver -> {
                showScreenSaver.postValue(true)
            }
            SettingKey.Volume -> {
                value?.let {
                    volumeStatus.postValue(it)
                }
            }
            SettingKey.WifStatus -> {
                value?.let {
                    wifiStatus.postValue(it)
                }
            }
            SettingKey.HomeAutomationStatus -> {
                value?.let {
                    homeAutomationStatus.postValue(it)
                }
            }
            SettingKey.Battery -> {
                value?.let {
                    batteryStatus.postValue(it)
                }
            }
        }
        deviceRepository.updateStatus(settingKey, value ?: "")
    }
}