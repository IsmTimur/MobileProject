package com.example.fifteenpuzzle;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().startMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().startMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getInstance().pauseMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTaskRoot()) {
            App.getInstance().stopMusic();
        }
    }
}