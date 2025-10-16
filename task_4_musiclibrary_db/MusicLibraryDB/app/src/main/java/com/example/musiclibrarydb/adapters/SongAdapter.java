package com.example.musiclibrarydb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musiclibrarydb.R;
import com.example.musiclibrarydb.sqlite.model.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Song> songList;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onEditClick(Song song);
        void onDeleteClick(Song song);
    }

    public SongAdapter(List<Song> songList, OnSongClickListener listener) {
        this.songList = songList;
        this.listener = listener;
    }

    public void updateSongs(List<Song> newSongs) {
        this.songList.clear();
        this.songList.addAll(newSongs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.textName.setText(song.getName());
        holder.textArtist.setText("Artist: " + song.getArtist());
        holder.textGenre.setText("Genre: " + song.getGenre());

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(song));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(song));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textArtist, textGenre;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textSongName);
            textArtist = itemView.findViewById(R.id.textSongArtist);
            textGenre = itemView.findViewById(R.id.textSongGenre);
            btnEdit = itemView.findViewById(R.id.btnEditSong);
            btnDelete = itemView.findViewById(R.id.btnDeleteSong);
        }
    }
}
