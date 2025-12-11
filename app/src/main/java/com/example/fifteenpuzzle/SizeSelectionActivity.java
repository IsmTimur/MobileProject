package com.example.fifteenpuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SizeSelectionActivity extends BaseActivity  {

    private Button btn3x3, btn4x4, btn5x5;
    private RelativeLayout btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btn3x3 = findViewById(R.id.btn3x3);
        btn4x4 = findViewById(R.id.btn4x4);
        btn5x5 = findViewById(R.id.btn5x5);
        btnBack = findViewById(R.id.btnBack);

        setupButton(btnBack, R.drawable.ic_back, getString(R.string.back));
    }

    private void setupButton(RelativeLayout button, int iconRes, String text) {
        ImageView icon = button.findViewById(R.id.buttonIcon);
        TextView textView = button.findViewById(R.id.buttonText);

        icon.setImageResource(iconRes);
        textView.setText(text);
    }

    private void setupClickListeners() {
        View.OnClickListener sizeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int gridSize = 0;
                int viewId = v.getId();

                if (viewId == R.id.btn3x3) {
                    gridSize = 3;
                } else if (viewId == R.id.btn4x4) {
                    gridSize = 4;
                } else if (viewId == R.id.btn5x5) {
                    gridSize = 5;
                }

                Intent intent = new Intent(SizeSelectionActivity.this, GameActivity.class);
                intent.putExtra("GRID_SIZE", gridSize);
                startActivity(intent);
            }
        };

        btn3x3.setOnClickListener(sizeClickListener);
        btn4x4.setOnClickListener(sizeClickListener);
        btn5x5.setOnClickListener(sizeClickListener);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}