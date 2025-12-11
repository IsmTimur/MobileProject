package com.example.fifteenpuzzle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<GameResult> results;

    public LeaderboardAdapter(List<GameResult> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameResult result = results.get(position);

        if (position == 0) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            holder.ivMedal.setImageResource(R.drawable.medal_gold);
            holder.tvPosition.setVisibility(View.GONE);
        } else if (position == 1) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            holder.ivMedal.setImageResource(R.drawable.medal_silver);
            holder.tvPosition.setVisibility(View.GONE);
        } else if (position == 2) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            holder.ivMedal.setImageResource(R.drawable.medal_bronze);
            holder.tvPosition.setVisibility(View.GONE);
        } else {
            holder.ivMedal.setVisibility(View.GONE);
            holder.tvPosition.setVisibility(View.VISIBLE);
            holder.tvPosition.setText(String.valueOf(position + 1));
        }

        holder.tvPlayerName.setText(result.getPlayerName());
        holder.tvGridSize.setText(result.getGridSize() + "x" + result.getGridSize());
        holder.tvMoves.setText(String.valueOf(result.getMoves()));
        holder.tvTime.setText(result.getFormattedTime());
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public void updateData(List<GameResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedal;
        TextView tvPosition, tvPlayerName, tvGridSize, tvMoves, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            ivMedal = itemView.findViewById(R.id.ivMedal);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvGridSize = itemView.findViewById(R.id.tvGridSize);
            tvMoves = itemView.findViewById(R.id.tvMoves);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}