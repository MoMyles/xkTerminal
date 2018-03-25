package com.cetcme.xkterminal.MyClass;

import android.content.Context;
import android.media.MediaPlayer;

import com.cetcme.xkterminal.R;

/**
 * Created by qiuhong on 20/03/2018.
 */

public class SoundPlay {

    private static MediaPlayer alertMediaPlayer;
    private static MediaPlayer msgMediaPlayer;

    public static void startAlertSound(Context context) {
        if (alertMediaPlayer == null) alertMediaPlayer = MediaPlayer.create(context, R.raw.alert);
        alertMediaPlayer.start();
        alertMediaPlayer.setLooping(true);
    }

    public static void stopAlertSound() {
        if (alertMediaPlayer != null) {
            alertMediaPlayer.pause();
        }
    }

    public static void playMessageSound(Context context) {
        if (msgMediaPlayer == null) msgMediaPlayer = MediaPlayer.create(context, R.raw.msg);
        msgMediaPlayer.start();
    }

}
