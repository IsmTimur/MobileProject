package com.example.fifteenpuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends BaseActivity  {

    private RelativeLayout btnPlay, btnLeaderboard, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnPlay = findViewById(R.id.btnPlay);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnExit = findViewById(R.id.btnExit);

        setupButton(btnPlay, R.drawable.ic_play_filled, getString(R.string.play));
        setupButton(btnLeaderboard, R.drawable.ic_leaderboard_filled, getString(R.string.leaderboard));
        setupButton(btnExit, R.drawable.ic_exit_filled, getString(R.string.exit));
    }

    private void setupButton(RelativeLayout button, int iconRes, String text) {
        ImageView icon = button.findViewById(R.id.buttonIcon);
        TextView textView = button.findViewById(R.id.buttonText);

        icon.setImageResource(iconRes);
        textView.setText(text);
    }

    private void setupClickListeners() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SizeSelectionActivity.class);
                startActivity(intent);
            }
        });

        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
                startActivity(intent);
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}