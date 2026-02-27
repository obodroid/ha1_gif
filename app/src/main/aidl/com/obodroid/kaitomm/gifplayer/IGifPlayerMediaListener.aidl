package com.obodroid.kaitomm.gifplayer;

interface IGifPlayerMediaListener {
    void onPlayerReady();
    void onEnd();
    void onPlay();
    void onPause();
    void onStop();
    void onError(in String error);
}
