package com.example.fifteenpuzzle;

import android.app.Application;
import android.media.MediaPlayer;

public class App extends Application {
    private static App instance;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPrepared = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initMusic();
    }

    private void initMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.3f, 0.3f);

            mediaPlayer.setOnPreparedListener(mp -> {
                isMusicPrepared = true;
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isMusicPrepared = false;
                return false;
            });
        }
    }

    public void startMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && isMusicPrepared) {
            mediaPlayer.start();
        } else if (mediaPlayer == null || !isMusicPrepared) {
            initMusic();
            if (mediaPlayer != null && isMusicPrepared) {
                mediaPlayer.start();
            }
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isMusicPrepared = false;
        }
    }

    public boolean isMusicPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public static App getInstance() {
        return instance;
    }
}