package com.example.fifteenpuzzle;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultsManager {
    private static final String PREFS_NAME = "GameResults";
    private static final String RESULTS_KEY = "saved_results";
    private static ResultsManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    private List<GameResult> results;

    private ResultsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadResults();
    }

    public static synchronized ResultsManager getInstance(Context context) {
        if (instance == null) {
            instance = new ResultsManager(context);
        }
        return instance;
    }

    public void saveResult(GameResult result) {
        results.add(result);
        saveResults();
    }

    public List<GameResult> getResults() {
        return new ArrayList<>(results);
    }

    public List<GameResult> getResults(int gridSizeFilter) {
        if (gridSizeFilter == 0) {
            return new ArrayList<>(results);
        }
        List<GameResult> filtered = new ArrayList<>();
        for (GameResult result : results) {
            if (result.getGridSize() == gridSizeFilter) {
                filtered.add(result);
            }
        }
        return filtered;
    }

    public void sortResults(List<GameResult> results, String sortBy, boolean ascending) {
        Comparator<GameResult> comparator = getComparator(sortBy, ascending);
        Collections.sort(results, comparator);
    }

    private Comparator<GameResult> getComparator(String sortBy, boolean ascending) {
        Comparator<GameResult> comparator;

        switch (sortBy) {
            case "moves":
                comparator = new Comparator<GameResult>() {
                    @Override
                    public int compare(GameResult r1, GameResult r2) {
                        return Integer.compare(r1.getMoves(), r2.getMoves());
                    }
                };
                break;
            case "time":
                comparator = new Comparator<GameResult>() {
                    @Override
                    public int compare(GameResult r1, GameResult r2) {
                        return Long.compare(r1.getTimeInMillis(), r2.getTimeInMillis());
                    }
                };
                break;
            default:
                comparator = new Comparator<GameResult>() {
                    @Override
                    public int compare(GameResult r1, GameResult r2) {
                        int score1 = r1.getMoves() + (int)(r1.getTimeInMillis() / 1000);
                        int score2 = r2.getMoves() + (int)(r2.getTimeInMillis() / 1000);
                        return Integer.compare(score1, score2);
                    }
                };
                break;
        }

        if (!ascending) {
            comparator = Collections.reverseOrder(comparator);
        }

        return comparator;
    }

    private void loadResults() {
        String json = prefs.getString(RESULTS_KEY, "[]");
        Type type = new TypeToken<List<GameResult>>(){}.getType();
        results = gson.fromJson(json, type);
        if (results == null) {
            results = new ArrayList<>();
        }
    }

    private void saveResults() {
        String json = gson.toJson(results);
        prefs.edit().putString(RESULTS_KEY, json).apply();
    }

    public void clearAllResults() {
        results.clear();
        saveResults();
    }
}