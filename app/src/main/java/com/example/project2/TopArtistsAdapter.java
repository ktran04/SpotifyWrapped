package com.example.project2;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TopArtistsAdapter extends RecyclerView.Adapter<TopArtistsAdapter.ArtistViewHolder> {

    private List<String> artistsList;
    private Context context;

    public TopArtistsAdapter(Context context, List<String> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.topartist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        String artist = artistsList.get(position);
        holder.bind(artist);
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder {

        private TextView artistTextView;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            artistTextView = itemView.findViewById(R.id.top_artists_text_view);
        }

        public void bind(String artist) {
            artistTextView.setText(artist);
        }
    }
}
