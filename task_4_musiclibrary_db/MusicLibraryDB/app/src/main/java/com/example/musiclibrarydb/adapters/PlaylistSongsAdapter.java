package com.example.musiclibrarydb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musiclibrarydb.R;
import com.example.musiclibrarydb.sqlite.model.Song;
import java.util.ArrayList;

public class PlaylistSongsAdapter extends RecyclerView.Adapter<PlaylistSongsAdapter.SongViewHolder> {

    private final Context context;
    private final ArrayList<Song> songs;

    public PlaylistSongsAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_in_playlist, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.tvSongName.setText(song.getName());
        holder.tvArtist.setText("Artist: " + song.getArtist());
        holder.tvGenre.setText("Genre: " + song.getGenre());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongName, tvArtist, tvGenre;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvGenre = itemView.findViewById(R.id.tvGenre);
        }
    }
}
