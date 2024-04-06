package com.example.project2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TopGenresAdapter extends RecyclerView.Adapter<TopGenresAdapter.GenreViewHolder> {
    private List<String> genreList;

    public TopGenresAdapter(List<String> genreList) {
        this.genreList = genreList;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.topgenre, parent, false);
        return new GenreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        String genre = genreList.get(position);
        holder.bind(genre);
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {
        private TextView genreTextView;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            genreTextView = itemView.findViewById(R.id.top_genres_text_view); // Assuming top_genre_text_view is the ID of TextView in top_genre.xml
        }

        public void bind(String genre) {
            genreTextView.setText(genre);
        }
    }
}
