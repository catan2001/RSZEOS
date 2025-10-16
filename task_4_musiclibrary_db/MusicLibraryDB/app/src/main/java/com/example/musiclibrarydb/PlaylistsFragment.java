package com.example.musiclibrarydb;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musiclibrarydb.adapters.PlaylistAdapter;
import com.example.musiclibrarydb.adapters.PlaylistSongsAdapter;
import com.example.musiclibrarydb.sqlite.helper.MusicLibraryDBHelper;
import com.example.musiclibrarydb.sqlite.model.Playlist;
import com.example.musiclibrarydb.sqlite.model.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PlaylistsFragment extends Fragment implements PlaylistAdapter.OnPlaylistActionListener {

    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;
    private ArrayList<Playlist> playlists;
    private MusicLibraryDBHelper dbHelper;
    private int userId;

    private EditText etSearchPlaylist;
    private Button btnSearchPlaylist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        recyclerView = view.findViewById(R.id.recyclerPlaylists);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddPlaylist);
        etSearchPlaylist = view.findViewById(R.id.etSearchPlaylistName);
        btnSearchPlaylist = view.findViewById(R.id.btnSearchPlaylistName);

        dbHelper = new MusicLibraryDBHelper(getContext());
        userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;

        playlists = dbHelper.getAllPlaylists(userId);
        adapter = new PlaylistAdapter(getContext(), playlists, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
        btnSearchPlaylist.setOnClickListener(v -> searchPlaylists());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPlaylists();
    }

    private void refreshPlaylists() {
        playlists.clear();
        playlists.addAll(dbHelper.getAllPlaylists(userId));
        adapter.notifyDataSetChanged();
    }

    // -----------------------
    // Search Playlists
    // -----------------------
    private void searchPlaylists() {
        String query = etSearchPlaylist.getText().toString().trim();
        if (query.isEmpty()) {
            refreshPlaylists();
            return;
        }

        String sqlQuery = "%" + query + "%";
        playlists.clear();
        playlists.addAll(dbHelper.searchPlaylistsByName(sqlQuery, userId));
        adapter.notifyDataSetChanged();

        if (playlists.isEmpty()) {
            Toast.makeText(getContext(), "No playlists found", Toast.LENGTH_SHORT).show();
        }
    }

    // -----------------------
    // Add / Edit Playlist
    // -----------------------
    private void showAddEditDialog(@Nullable Playlist playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(playlist == null ? "Add Playlist" : "Edit Playlist");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etName = new EditText(getContext());
        etName.setHint("Playlist Name");
        etName.setInputType(InputType.TYPE_CLASS_TEXT);
        if (playlist != null) etName.setText(playlist.getName());
        layout.addView(etName);

        final EditText etDescription = new EditText(getContext());
        etDescription.setHint("Description");
        etDescription.setInputType(InputType.TYPE_CLASS_TEXT);
        if (playlist != null) etDescription.setText(playlist.getDescription());
        layout.addView(etDescription);

        builder.setView(layout);

        builder.setPositiveButton(playlist == null ? "Add" : "Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (playlist == null) {
                long rowId = dbHelper.addPlaylist(name, description, userId); // insert returns long
                Playlist p = new Playlist(name, description, userId);
                p.setId((int) rowId); // cast long to int safely
                playlists.add(p);
            } else {
                playlist.setName(name);
                playlist.setDescription(description);
                dbHelper.updatePlaylist(playlist.getId(), name, description);
            }


            adapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // -----------------------
    // Add / Remove Songs
    // -----------------------
    private void showAddSongsDialog(Playlist playlist) {
        ArrayList<Song> allSongs = dbHelper.getAllSongs();
        ArrayList<Song> playlistSongs = dbHelper.getSongsInPlaylist(playlist.getId());
        Set<Integer> playlistSongIds = new HashSet<>();
        for (Song s : playlistSongs) playlistSongIds.add(s.getId());

        String[] songNames = new String[allSongs.size()];
        boolean[] checkedItems = new boolean[allSongs.size()];

        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            songNames[i] = song.getName();
            checkedItems[i] = playlistSongIds.contains(song.getId());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Songs to " + playlist.getName());
        builder.setMultiChoiceItems(songNames, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);

        builder.setPositiveButton("Save", (dialog, which) -> {
            for (int i = 0; i < allSongs.size(); i++) {
                Song song = allSongs.get(i);
                boolean inPlaylist = playlistSongIds.contains(song.getId());

                if (checkedItems[i] && !inPlaylist) {
                    dbHelper.addSongToPlaylist(playlist.getId(), song.getId());
                } else if (!checkedItems[i] && inPlaylist) {
                    dbHelper.removeSongFromPlaylist(playlist.getId(), song.getId());
                }
            }
            Toast.makeText(getContext(), "Playlist updated!", Toast.LENGTH_SHORT).show();
            refreshPlaylists();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // -----------------------
    // PlaylistAdapter Listener
    // -----------------------
    @Override
    public void onEdit(Playlist playlist) {
        showAddEditDialog(playlist);
    }

    @Override
    public void onDelete(Playlist playlist) {
        dbHelper.deletePlaylist(playlist.getId());
        playlists.remove(playlist);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAddSongs(Playlist playlist) {
        showAddSongsDialog(playlist);
    }

    @Override
    public void onView(Playlist playlist) {
        showPlaylistDialog(playlist);
    }

    private void showPlaylistDialog(Playlist playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Playlist: " + playlist.getName());

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayList<Song> songs = dbHelper.getSongsInPlaylist(playlist.getId());
        PlaylistSongsAdapter adapter = new PlaylistSongsAdapter(getContext(), songs);
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }
}
