package com.example.musiclibrarydb;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musiclibrarydb.adapters.SongAdapter;
import com.example.musiclibrarydb.sqlite.helper.MusicLibraryDBHelper;
import com.example.musiclibrarydb.sqlite.model.Artist;
import com.example.musiclibrarydb.sqlite.model.Genre;
import com.example.musiclibrarydb.sqlite.model.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SongsFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private MusicLibraryDBHelper dbHelper;
    private List<Song> songList;

    private FloatingActionButton fabAddSong;
    private EditText etSearchSongName;
    private Button btnSearchSongName;
    private Spinner spinnerArtistFilter, spinnerGenreFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        recyclerView = view.findViewById(R.id.recyclerSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAddSong = view.findViewById(R.id.fabAddSong);
        dbHelper = new MusicLibraryDBHelper(getContext());
        songList = new ArrayList<>();

        // Search views
        etSearchSongName = view.findViewById(R.id.etSearchSongName);
        btnSearchSongName = view.findViewById(R.id.btnSearchSongName);
        spinnerArtistFilter = view.findViewById(R.id.spinnerArtistFilter);
        spinnerGenreFilter = view.findViewById(R.id.spinnerGenreFilter);

        fabAddSong.setOnClickListener(v -> showAddSongDialog());
        btnSearchSongName.setOnClickListener(v -> searchSongs());

        setupSpinners();
        loadSongs();

        return view;
    }

    // -----------------------
    // Setup Artist & Genre Spinners
    // -----------------------
    private void setupSpinners() {
        // Artist filter spinner
        List<String> artistNames = new ArrayList<>();
        artistNames.add("None");
        for (Artist a : dbHelper.getAllArtists()) artistNames.add(a.getName());
        spinnerArtistFilter.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, artistNames));

        // Genre filter spinner
        List<String> genreNames = new ArrayList<>();
        genreNames.add("None");
        for (Genre g : dbHelper.getAllGenres()) genreNames.add(g.getName());
        spinnerGenreFilter.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, genreNames));
    }

    // -----------------------
    // Load all songs
    // -----------------------
    private void loadSongs() {
        songList = dbHelper.getAllSongs();
        if (songAdapter == null) {
            songAdapter = new SongAdapter(songList, this);
            recyclerView.setAdapter(songAdapter);
        } else {
            songAdapter.updateSongs(songList);
        }
    }

    // -----------------------
    // Add Song Dialog
    // -----------------------
    private void showAddSongDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Song");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText inputName = new EditText(getContext());
        inputName.setHint("Song Name");
        layout.addView(inputName);

        // Artist Spinner
        Spinner artistSpinner = new Spinner(getContext());
        List<Artist> artists = dbHelper.getAllArtists();
        List<String> artistNames = new ArrayList<>();
        for (Artist a : artists) artistNames.add(a.getName());
        ArrayAdapter<String> artistAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, artistNames);
        artistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        artistSpinner.setAdapter(artistAdapter);
        layout.addView(artistSpinner);

        // Genre Spinner
        Spinner genreSpinner = new Spinner(getContext());
        List<Genre> genres = dbHelper.getAllGenres();
        List<String> genreNames = new ArrayList<>();
        for (Genre g : genres) genreNames.add(g.getName());
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, genreNames);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(genreAdapter);
        layout.addView(genreSpinner);

        // Update genre when artist changes
        artistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArtist = artistNames.get(position);
                int artistId = dbHelper.getArtistIdByName(selectedArtist);
                int genreId = dbHelper.getGenreIdByArtistId(artistId);
                if (genreId > 0) {
                    String genreName = dbHelper.getGenreNameById(genreId);
                    int pos = genreNames.indexOf(genreName);
                    if (pos >= 0) genreSpinner.setSelection(pos);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String artist = (String) artistSpinner.getSelectedItem();
            String genre = (String) genreSpinner.getSelectedItem();

            if (!name.isEmpty()) {
                dbHelper.addSong(name, artist, genre);
                loadSongs();
                Toast.makeText(getContext(), "Song added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Song name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // -----------------------
    // Edit Song Dialog
    // -----------------------
    private void showEditSongDialog(Song song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Song");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText inputName = new EditText(getContext());
        inputName.setText(song.getName());
        layout.addView(inputName);

        // Artist Spinner
        Spinner artistSpinner = new Spinner(getContext());
        List<Artist> artists = dbHelper.getAllArtists();
        List<String> artistNames = new ArrayList<>();
        for (Artist a : artists) artistNames.add(a.getName());
        ArrayAdapter<String> artistAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, artistNames);
        artistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        artistSpinner.setAdapter(artistAdapter);
        int artistPos = artistNames.indexOf(song.getArtist());
        if (artistPos >= 0) artistSpinner.setSelection(artistPos);
        layout.addView(artistSpinner);

        // Genre Spinner
        Spinner genreSpinner = new Spinner(getContext());
        List<Genre> genres = dbHelper.getAllGenres();
        List<String> genreNames = new ArrayList<>();
        for (Genre g : genres) genreNames.add(g.getName());
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, genreNames);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(genreAdapter);
        int genrePos = genreNames.indexOf(song.getGenre());
        if (genrePos >= 0) genreSpinner.setSelection(genrePos);
        layout.addView(genreSpinner);

        // Update genre when artist changes
        artistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArtist = artistNames.get(position);
                int artistId = dbHelper.getArtistIdByName(selectedArtist);
                int genreId = dbHelper.getGenreIdByArtistId(artistId);
                if (genreId > 0) {
                    String genreName = dbHelper.getGenreNameById(genreId);
                    int pos = genreNames.indexOf(genreName);
                    if (pos >= 0) genreSpinner.setSelection(pos);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = inputName.getText().toString().trim();
            String newArtist = (String) artistSpinner.getSelectedItem();
            String newGenre = (String) genreSpinner.getSelectedItem();

            if (!newName.isEmpty()) {
                dbHelper.updateSong(song.getId(), newName, newArtist, newGenre);
                loadSongs();
                Toast.makeText(getContext(), "Song updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Song name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // -----------------------
    // Search Songs with Filters
    // -----------------------
    private void searchSongs() {
        String query = etSearchSongName.getText().toString().trim();
        String artist = spinnerArtistFilter.getSelectedItem().toString();
        String genre = spinnerGenreFilter.getSelectedItem().toString();

        artist = artist.equals("None") ? null : artist;
        genre = genre.equals("None") ? null : genre;
        query = "%" + query + "%";

        songList.clear();
        songList.addAll(dbHelper.searchSongs(query, genre, artist));

        if (songAdapter == null) {
            songAdapter = new SongAdapter(songList, this);
            recyclerView.setAdapter(songAdapter);
        } else {
            songAdapter.updateSongs(songList);
        }

        if (songList.isEmpty()) {
            Toast.makeText(getContext(), "No songs found", Toast.LENGTH_SHORT).show();
        }
    }

    // -----------------------
    // SongAdapter Listeners
    // -----------------------
    @Override
    public void onEditClick(Song song) {
        showEditSongDialog(song);
    }

    @Override
    public void onDeleteClick(Song song) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Song")
                .setMessage("Are you sure you want to delete " + song.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteSong(song.getId());
                    loadSongs();
                    Toast.makeText(getContext(), "Song deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
