package com.example.fifteenpuzzle;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private Spinner spinnerSortBy, spinnerSortOrder, spinnerFilterSize;
    private RelativeLayout btnBack, btnClearAll;
    private TextView tvEmptyResults;
    private LinearLayout filtersLayout;

    private ResultsManager resultsManager;
    private LeaderboardAdapter adapter;
    private List<GameResult> displayedResults;

    private String currentSortBy = "default";
    private boolean currentAscending = true;
    private int currentFilterSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        resultsManager = ResultsManager.getInstance(this);
        initViews();
        setupClickListeners();
        setupSpinners();
        loadResults();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewLeaderboard);
        spinnerSortBy = findViewById(R.id.spinnerSortBy);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
        spinnerFilterSize = findViewById(R.id.spinnerFilterSize);
        btnBack = findViewById(R.id.btnBack);
        btnClearAll = findViewById(R.id.btnClearAll);
        tvEmptyResults = findViewById(R.id.tvEmptyResults);
        filtersLayout = findViewById(R.id.filtersLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        displayedResults = new ArrayList<>();
        adapter = new LeaderboardAdapter(displayedResults);
        recyclerView.setAdapter(adapter);

        setupButton(btnBack, R.drawable.ic_back, getString(R.string.back));

        btnClearAll.setBackgroundResource(R.drawable.button_clear_selector);

        android.widget.ImageView clearIcon = btnClearAll.findViewById(R.id.buttonIcon);
        android.widget.TextView clearText = btnClearAll.findViewById(R.id.buttonText);

        clearIcon.setImageResource(R.drawable.ic_delete);
        clearIcon.setColorFilter(android.graphics.Color.WHITE);
        clearText.setText("Очистить всё");
        clearText.setTextColor(android.graphics.Color.WHITE);

        btnClearAll.setVisibility(View.GONE);
    }

    private void setupButton(RelativeLayout button, int iconRes, String text) {
        android.widget.ImageView icon = button.findViewById(R.id.buttonIcon);
        android.widget.TextView textView = button.findViewById(R.id.buttonText);

        icon.setImageResource(iconRes);
        textView.setText(text);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> sortByAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_by_options, android.R.layout.simple_spinner_item);
        sortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(sortByAdapter);

        ArrayAdapter<CharSequence> sortOrderAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_order_options, android.R.layout.simple_spinner_item);
        sortOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortOrderAdapter);

        ArrayAdapter<CharSequence> filterSizeAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_size_options, android.R.layout.simple_spinner_item);
        filterSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterSize.setAdapter(filterSizeAdapter);

        spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortBy = getSortByValue(position);
                applySortingAndFiltering();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerSortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentAscending = position == 0;
                applySortingAndFiltering();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerFilterSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilterSize = getFilterSizeValue(position);
                applySortingAndFiltering();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private String getSortByValue(int position) {
        switch (position) {
            case 0: return "default";
            case 1: return "moves";
            case 2: return "time";
            default: return "default";
        }
    }

    private int getFilterSizeValue(int position) {
        switch (position) {
            case 0: return 0;
            case 1: return 3;
            case 2: return 4;
            case 3: return 5;
            default: return 0;
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearAll.setOnClickListener(v -> showClearConfirmationDialog());
    }

    private void showClearConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_exit_confirmation, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        RelativeLayout btnYes = dialogView.findViewById(R.id.btnYes);
        RelativeLayout btnNo = dialogView.findViewById(R.id.btnNo);

        dialogTitle.setText("Вы уверены, что хотите удалить все записи?");

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            resultsManager.clearAllResults();
            loadResults();
        });

        btnNo.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadResults() {
        List<GameResult> allResults = resultsManager.getResults(currentFilterSize);
        resultsManager.sortResults(allResults, currentSortBy, currentAscending);

        displayedResults.clear();
        displayedResults.addAll(allResults);
        adapter.notifyDataSetChanged();

        if (displayedResults.isEmpty()) {
            tvEmptyResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.GONE);
        } else {
            tvEmptyResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
        }
    }

    private void applySortingAndFiltering() {
        loadResults();
    }
}