package com.example.musiclibrarydb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musiclibrarydb.R;
import com.example.musiclibrarydb.sqlite.model.Playlist;
import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final Context context;
    private final ArrayList<Playlist> playlists;
    private final OnPlaylistActionListener listener;

    public interface OnPlaylistActionListener {
        void onEdit(Playlist playlist);
        void onDelete(Playlist playlist);
        void onAddSongs(Playlist playlist);

        void onView(Playlist playlist);
    }

    public PlaylistAdapter(Context context, ArrayList<Playlist> playlists, OnPlaylistActionListener listener) {
        this.context = context;
        this.playlists = playlists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.tvName.setText(playlist.getName());
        holder.tvDescription.setText(playlist.getDescription() != null ? playlist.getDescription() : "");
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(playlist));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(playlist));
        holder.btnAddSongs.setOnClickListener(v -> listener.onAddSongs(playlist));
        holder.btnView.setOnClickListener(v -> listener.onView(playlist));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        Button btnEdit, btnDelete, btnAddSongs, btnView;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPlaylistName);
            tvDescription = itemView.findViewById(R.id.tvPlaylistDescription);
            btnEdit = itemView.findViewById(R.id.btnEditPlaylist);
            btnDelete = itemView.findViewById(R.id.btnDeletePlaylist);
            btnAddSongs = itemView.findViewById(R.id.btnAddSongs);
            btnView = itemView.findViewById(R.id.btnViewPlaylist);
        }
    }
}
