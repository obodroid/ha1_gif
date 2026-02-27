package com.obodroid.kaitomm.gifplayer

import com.obodroid.kaitomm.gifplayer.fragment.ClockFragment

class MainActivity : BaseMainActivity() {
    override val showScreenSaverRunnable = Runnable {
        try {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.fragment_container,
                    ClockFragment.newInstance(),
                    "ClockFragment"
                )
                .commit()
        } catch (e: Exception) {
            logger.verbose("addFragment status: catch")
        }
    }
}