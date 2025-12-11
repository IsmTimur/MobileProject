package com.example.fifteenpuzzle;

public class GameResult {
    private String playerName;
    private int gridSize;
    private int moves;
    private long timeInMillis;
    private long timestamp;

    public GameResult(String playerName, int gridSize, int moves, long timeInMillis) {
        this.playerName = playerName;
        this.gridSize = gridSize;
        this.moves = moves;
        this.timeInMillis = timeInMillis;
        this.timestamp = System.currentTimeMillis();
    }

    public String getPlayerName() { return playerName; }
    public int getGridSize() { return gridSize; }
    public int getMoves() { return moves; }
    public long getTimeInMillis() { return timeInMillis; }
    public long getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        int seconds = (int) (timeInMillis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}