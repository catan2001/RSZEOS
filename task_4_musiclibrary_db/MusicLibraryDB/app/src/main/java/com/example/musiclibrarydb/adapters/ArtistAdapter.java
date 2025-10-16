package com.example.musiclibrarydb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musiclibrarydb.R;
import com.example.musiclibrarydb.sqlite.model.Artist;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private final List<Artist> artistList;
    private final OnArtistClickListener listener;

    public interface OnArtistClickListener {
        void onEditClick(Artist artist);
        void onDeleteClick(Artist artist);
    }

    public ArtistAdapter(List<Artist> artistList, OnArtistClickListener listener) {
        this.artistList = artistList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artist artist = artistList.get(position);
        holder.textName.setText(artist.getName());
        holder.textGenre.setText("Genre: " + (artist.getGenre() != null ? artist.getGenre() : "N/A"));

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(artist));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(artist));
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textGenre;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textArtistName);
            textGenre = itemView.findViewById(R.id.textArtistGenre);
            btnEdit = itemView.findViewById(R.id.btnEditArtist);
            btnDelete = itemView.findViewById(R.id.btnDeleteArtist);
        }
    }
}
