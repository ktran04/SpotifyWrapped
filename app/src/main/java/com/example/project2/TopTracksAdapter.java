package com.example.project2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TopTracksAdapter extends RecyclerView.Adapter<TopTracksAdapter.TrackViewHolder> {
    private List<String> trackList;

    public TopTracksAdapter(List<String> trackList) {
        this.trackList = trackList;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.toptrack, parent, false);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        String track = trackList.get(position);
        holder.bind(track);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        private TextView trackTextView;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackTextView = itemView.findViewById(R.id.top_tracks_text_view); // assuming trackTextView is the ID of TextView in item_track.xml
        }

        public void bind(String track) {
            trackTextView.setText(track);
        }
    }
}
