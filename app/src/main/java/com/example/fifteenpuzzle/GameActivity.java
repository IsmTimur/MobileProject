package com.example.fifteenpuzzle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends BaseActivity  {

    private GridLayout gameGrid;
    private TextView tvMoves, tvTimer;
    private RelativeLayout btnBack, btnShuffle, btnPause;

    private androidx.activity.OnBackPressedCallback onBackPressedCallback;

    private long pausedTime = 0;
    private long totalPausedDuration = 0;
    private long dialogPauseStartTime = 0;

    private int gridSize;
    private int emptyX, emptyY;
    private int moveCount = 0;
    private long startTime;
    private boolean isPlaying = false;
    private boolean isPaused = false;

    private boolean isExitDialogShowing = false;

    private static final long TIME_LIMIT_MINUTES = 20;
    private static final long TIME_LIMIT_MILLIS = TIME_LIMIT_MINUTES * 60 * 1000;
    private boolean isGameOver = false;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    private int[][] board;
    private List<TextView> tileViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        gridSize = getIntent().getIntExtra("GRID_SIZE", 4);

        initViews();
        setupClickListeners();
        initGame();
        setupBackPressedCallback();
    }

    private float startX, startY;

    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeListeners() {
        if (tileViews == null || tileViews.isEmpty()) {
            return;
        }

        for (TextView tile : tileViews) {
            tile.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            return true;

                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();

                            Integer position = (Integer) v.getTag();
                            if (position != null) {
                                int x = position / gridSize;
                                int y = position % gridSize;
                                handleTileSwipe(x, y, startX, startY, endX, endY);
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

    }

    private void handleTileSwipe(int tileX, int tileY, float startX, float startY, float endX, float endY) {
        if (isPaused || !isPlaying) return;

        float deltaX = endX - startX;
        float deltaY = endY - startY;

        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);

        int minSwipeDistance = 30;

        if (absDeltaX > absDeltaY && absDeltaX > minSwipeDistance) {
            if (deltaX > 0) {
                tryMoveTile(tileX, tileY, 0, 1);
            } else {
                tryMoveTile(tileX, tileY, 0, -1);
            }
        } else if (absDeltaY > absDeltaX && absDeltaY > minSwipeDistance) {
            if (deltaY > 0) {
                tryMoveTile(tileX, tileY, 1, 0);
            } else {
                tryMoveTile(tileX, tileY, -1, 0);
            }
        }
    }

    private void tryMoveTile(int tileX, int tileY, int dirX, int dirY) {
        int targetX = tileX + dirX;
        int targetY = tileY + dirY;

        if (targetX >= 0 && targetX < gridSize && targetY >= 0 && targetY < gridSize &&
                board[targetX][targetY] == 0) {
            moveTile(tileX, tileY);
        }
    }

    private void setupBackPressedCallback() {
        onBackPressedCallback = new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void initViews() {
        gameGrid = findViewById(R.id.gameGrid);
        tvMoves = findViewById(R.id.tvMoves);
        tvTimer = findViewById(R.id.tvTimer);
        btnBack = findViewById(R.id.btnBack);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnPause = findViewById(R.id.btnPause);

        setupButton(btnBack, R.drawable.ic_back, "");
        setupButton(btnShuffle, R.drawable.ic_refresh, "");
        setupButton(btnPause, R.drawable.ic_pause, "");

        gameGrid.setRowCount(gridSize);
        gameGrid.setColumnCount(gridSize);
    }

    private void setupButton(RelativeLayout button, int iconRes, String text) {
        android.widget.ImageView icon = button.findViewById(R.id.buttonIcon);
        android.widget.TextView textView = button.findViewById(R.id.buttonText);

        icon.setImageResource(iconRes);
        textView.setText(text);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> showExitConfirmationDialog());

        btnShuffle.setOnClickListener(v -> {
            shuffleBoard();
            resetGame();
        });

        btnPause.setOnClickListener(v -> togglePause());
    }

    private void showExitConfirmationDialog() {
        if (isExitDialogShowing) {
            return;
        }

        isExitDialogShowing = true;

        dialogPauseStartTime = System.currentTimeMillis();

        boolean wasPausedBeforeDialog = isPaused;
        if (!isPaused && isPlaying) {
            togglePause();
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_exit_confirmation, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RelativeLayout btnYes = dialogView.findViewById(R.id.btnYes);
        RelativeLayout btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            isExitDialogShowing = false;
            dialog.dismiss();
            finish();
        });

        btnNo.setOnClickListener(v -> {
            isExitDialogShowing = false;
            dialog.dismiss();
            if (isPaused && isPlaying && !wasPausedBeforeDialog) {
                togglePause();
            }
        });

        dialog.setOnDismissListener(dialogInterface -> {
            isExitDialogShowing = false;
            if (isPaused && isPlaying && !wasPausedBeforeDialog) {
                togglePause();
            }
        });

        dialog.show();
    }

    private void initGame() {
        board = new int[gridSize][gridSize];
        tileViews = new ArrayList<>();

        initializeBoard();
        createTiles();
        shuffleBoard();
        startGame();
    }

    private void initializeBoard() {
        int number = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                board[i][j] = number++;
            }
        }
        emptyX = gridSize - 1;
        emptyY = gridSize - 1;
        board[emptyX][emptyY] = 0;
    }

    private void createTiles() {
        gameGrid.removeAllViews();
        tileViews.clear();

        int tileSize = calculateTileSize();

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                TextView tile = new TextView(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tileSize;
                params.height = tileSize;
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                params.setMargins(1, 1, 1, 1);

                tile.setLayoutParams(params);
                tile.setGravity(android.view.Gravity.CENTER);
                tile.setTypeface(null, Typeface.BOLD);

                tile.setTag(i * gridSize + j);

                if (gridSize >= 5) {
                    tile.setTextSize(16);
                } else if (gridSize == 4) {
                    tile.setTextSize(18);
                } else {
                    tile.setTextSize(20);
                }

                gameGrid.addView(tile);
                tileViews.add(tile);
            }
        }
        updateTiles();

        setupSwipeListeners();
    }

    private int calculateTileSize() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        int totalPadding = (int) ((32 + 8 + 2 + 4) * displayMetrics.density);
        int totalMargins = (int) (1 * displayMetrics.density) * (gridSize - 1);

        int availableWidth = screenWidth - totalPadding - totalMargins;

        int tileSize = (int) Math.floor(availableWidth / (float) gridSize);

        return tileSize;
    }

    private void updateTiles() {
        int index = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                TextView tile = tileViews.get(index++);
                int number = board[i][j];

                if (number == 0) {
                    tile.setText("");
                    tile.setBackgroundResource(R.drawable.tile_empty);
                    tile.setTextColor(ContextCompat.getColor(this, android.R.color.transparent));
                } else {
                    tile.setText(String.valueOf(number));
                    tile.setBackgroundResource(R.drawable.tile_background);
                    tile.setTextColor(ContextCompat.getColor(this, R.color.white));

                    tile.setScaleX(0.8f);
                    tile.setScaleY(0.8f);
                    tile.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                }
            }
        }
    }

    private void moveTile(int x, int y) {
        if ((Math.abs(x - emptyX) == 1 && y == emptyY) ||
                (Math.abs(y - emptyY) == 1 && x == emptyX)) {

            board[emptyX][emptyY] = board[x][y];
            board[x][y] = 0;

            emptyX = x;
            emptyY = y;

            moveCount++;
            tvMoves.setText(String.valueOf(moveCount));

            updateTiles();

            if (checkWin()) {
                endGame();
            }
        }
    }

    private void shuffleBoard() {
        performGuaranteedShuffle();

        updateTiles();
        resetGame();

        if (!verifyBoardSolvability()) {
            performFallbackShuffle();
        }
    }

    private void performGuaranteedShuffle() {
        initializeBoard();

        Random random = new Random();
        int shuffleMoves = gridSize * gridSize * 20;

        for (int i = 0; i < shuffleMoves; i++) {
            List<int[]> possibleMoves = getPossibleMoves();

            if (!possibleMoves.isEmpty()) {
                int[] move = possibleMoves.get(random.nextInt(possibleMoves.size()));
                makeMove(move[0], move[1]);
            }
        }
    }

    private List<int[]> getPossibleMoves() {
        List<int[]> moves = new ArrayList<>();

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            int newX = emptyX + dir[0];
            int newY = emptyY + dir[1];

            if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize) {
                moves.add(new int[]{newX, newY});
            }
        }

        return moves;
    }

    private void makeMove(int x, int y) {
        board[emptyX][emptyY] = board[x][y];
        board[x][y] = 0;
        emptyX = x;
        emptyY = y;
    }

    private void performFallbackShuffle() {
        initializeBoard();

        Random random = new Random();
        int swaps = gridSize * 3;

        for (int i = 0; i < swaps; i++) {
            int x1 = random.nextInt(gridSize);
            int y1 = random.nextInt(gridSize);

            int[][] neighbors = {{x1-1, y1}, {x1+1, y1}, {x1, y1-1}, {x1, y1+1}};
            List<int[]> validNeighbors = new ArrayList<>();

            for (int[] neighbor : neighbors) {
                int x2 = neighbor[0], y2 = neighbor[1];
                if (x2 >= 0 && x2 < gridSize && y2 >= 0 && y2 < gridSize &&
                        board[x1][y1] != 0 && board[x2][y2] != 0) {
                    validNeighbors.add(neighbor);
                }
            }

            if (!validNeighbors.isEmpty()) {
                int[] neighbor = validNeighbors.get(random.nextInt(validNeighbors.size()));
                int temp = board[x1][y1];
                board[x1][y1] = board[neighbor[0]][neighbor[1]];
                board[neighbor[0]][neighbor[1]] = temp;
            }
        }
    }

    private boolean verifyBoardSolvability() {
        List<Integer> flatBoard = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                flatBoard.add(board[i][j]);
            }
        }
        return isSolvable(flatBoard);
    }

    private boolean isSolvable(List<Integer> numbers) {
        int inversions = 0;
        int n = gridSize * gridSize;

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int numI = numbers.get(i);
                int numJ = numbers.get(j);
                if (numI != 0 && numJ != 0 && numI > numJ) {
                    inversions++;
                }
            }
        }

        int emptyRow = 0;
        for (int i = 0; i < n; i++) {
            if (numbers.get(i) == 0) {
                emptyRow = i / gridSize + 1;
                break;
            }
        }

        boolean solvable;

        if (gridSize % 2 == 1) {
            solvable = inversions % 2 == 0;
        } else {
            int emptyRowFromBottom = gridSize - emptyRow + 1;
            solvable = (emptyRowFromBottom + inversions) % 2 == 1;
        }

        return solvable;
    }

    private boolean checkWin() {
        if (isGameOver) return false;

        int number = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (i == gridSize - 1 && j == gridSize - 1) {
                    if (board[i][j] != 0) return false;
                } else {
                    if (board[i][j] != number++) return false;
                }
            }
        }
        return true;
    }

    private void startGame() {
        moveCount = 0;
        startTime = System.currentTimeMillis();
        isPlaying = true;
        isPaused = false;
        isGameOver = false;
        totalPausedDuration = 0;

        tvMoves.setText("0");
        updatePauseButton();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused && isPlaying && !isGameOver) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - startTime - totalPausedDuration;

                    int seconds = (int) (elapsedTime / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;

                    tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                    if (elapsedTime >= TIME_LIMIT_MILLIS) {
                        showGameOverDialog();
                    }
                }
                timerHandler.postDelayed(this, 500);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void showGameOverDialog() {
        if (isGameOver) return;

        isGameOver = true;
        isPlaying = false;
        timerHandler.removeCallbacks(timerRunnable);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_over, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvGameOverMessage = dialogView.findViewById(R.id.tvGameOverMessage);
        RelativeLayout btnRestart = dialogView.findViewById(R.id.btnRestart);
        RelativeLayout btnExit = dialogView.findViewById(R.id.btnExit);

        tvGameOverMessage.setText("Время вышло!");

        btnRestart.setOnClickListener(v -> {
            dialog.dismiss();
            restartLevel();
        });

        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void restartLevel() {
        isGameOver = false;
        shuffleBoard();
        resetGame();
    }

    private void resetGame() {
        moveCount = 0;
        startTime = System.currentTimeMillis();
        isPlaying = true;
        isPaused = false;
        isGameOver = false;
        totalPausedDuration = 0;

        tvMoves.setText("0");
        updatePauseButton();
    }

    private void togglePause() {
        if (isGameOver) return;

        isPaused = !isPaused;
        updatePauseButton();

        if (isPaused) {
            pausedTime = System.currentTimeMillis();
            timerHandler.removeCallbacks(timerRunnable);
        } else {
            long pauseDuration = System.currentTimeMillis() - pausedTime;
            startTime += pauseDuration;
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }

    private void updatePauseButton() {
        android.widget.ImageView icon = btnPause.findViewById(R.id.buttonIcon);
        android.widget.TextView textView = btnPause.findViewById(R.id.buttonText);

        if (isPaused) {
            icon.setImageResource(R.drawable.ic_play_filled);
            textView.setText("");
        } else {
            icon.setImageResource(R.drawable.ic_pause);
            textView.setText("");
        }
    }

    private void endGame() {
        isPlaying = false;
        timerHandler.removeCallbacks(timerRunnable);

        showWinDialog();
    }

    private void showWinDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_win, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvWinTime = dialogView.findViewById(R.id.tvWinTime);
        TextView tvWinMoves = dialogView.findViewById(R.id.tvWinMoves);
        EditText etPlayerName = dialogView.findViewById(R.id.etPlayerName);
        RelativeLayout btnSaveResult = dialogView.findViewById(R.id.btnSaveResult);

        long elapsedTime = System.currentTimeMillis() - startTime;
        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        tvWinTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        tvWinMoves.setText(String.valueOf(moveCount));

        etPlayerName.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(50)
        });

        etPlayerName.addTextChangedListener(new android.text.TextWatcher() {
            private TextView tvCharCount = dialogView.findViewById(R.id.tvCharCount);

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvCharCount != null) {
                    tvCharCount.setText(s.length() + "/50");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        btnSaveResult.setOnClickListener(v -> {
            String playerName = etPlayerName.getText().toString().trim();
            if (playerName.isEmpty()) {
                playerName = "Игрок";
            }

            GameResult result = new GameResult(playerName, gridSize, moveCount, elapsedTime);
            ResultsManager.getInstance(GameActivity.this).saveResult(result);

            dialog.dismiss();
            finish();
        });

        dialog.show();

        etPlayerName.post(() -> {
            etPlayerName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etPlayerName, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        if (onBackPressedCallback != null) {
            onBackPressedCallback.remove();
        }
    }
}