package com.obodroid.kaitomm.gifplayer;

import com.obodroid.kaitomm.gifplayer.IGifPlayerMediaListener;

interface IGifPlayerService {
    void setfaceInfo(in Bundle type);
    void showText(String text);
    void takePic();
    void showBottomText(String text);
    void playVideo(in Bundle data);
    void pauseVideo();
    void resumeVideo();
    void setting(String key, String value);

    void setupPlayer(in String token, in IGifPlayerMediaListener listener);
    void playMedia(in String id);
    void pauseMedia();
    void resumeMedia();
    void stopMedia();
}
