package com.example.musiclibrarydb;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.musiclibrarydb.adapters.ArtistAdapter;
import com.example.musiclibrarydb.sqlite.helper.MusicLibraryDBHelper;
import com.example.musiclibrarydb.sqlite.model.Artist;
import com.example.musiclibrarydb.sqlite.model.Genre;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ArtistsFragment extends Fragment implements ArtistAdapter.OnArtistClickListener {

    private RecyclerView recyclerView;
    private ArtistAdapter artistAdapter;
    private MusicLibraryDBHelper dbHelper;
    private List<Artist> artistList;
    private FloatingActionButton fabAddArtist;
    private EditText etSearchArtistName;
    private Button btnSearchArtistName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);

        recyclerView = view.findViewById(R.id.recyclerArtists);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearchArtistName = view.findViewById(R.id.etSearchArtistName);
        btnSearchArtistName = view.findViewById(R.id.btnSearchArtistName);
        fabAddArtist = view.findViewById(R.id.fabAddArtist);

        dbHelper = new MusicLibraryDBHelper(getContext());
        artistList = new ArrayList<>();

        loadArtists();

        fabAddArtist.setOnClickListener(v -> showArtistDialog(null));
        btnSearchArtistName.setOnClickListener(v -> searchArtists());

        return view;
    }

    private void loadArtists() {
        artistList.clear();
        List<Artist> allArtists = dbHelper.getAllArtists();
        artistList.addAll(allArtists);
        updateAdapter();
    }

    private void searchArtists() {
        String query = etSearchArtistName.getText().toString().trim();
        if (query.isEmpty()) {
            loadArtists();
            return;
        }

        artistList.clear();
        List<Artist> results = dbHelper.searchArtistsByName("%" + query + "%");
        artistList.addAll(results);
        updateAdapter();

        if (artistList.isEmpty()) {
            Toast.makeText(getContext(), "No artists found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAdapter() {
        if (artistAdapter == null) {
            artistAdapter = new ArtistAdapter(artistList, this);
            recyclerView.setAdapter(artistAdapter);
        } else {
            artistAdapter.notifyDataSetChanged();
        }
    }

    private void showArtistDialog(@Nullable Artist artist) {
        boolean isEdit = (artist != null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(isEdit ? "Edit Artist" : "Add Artist");

        // Layout for inputs
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Artist name input
        EditText inputName = new EditText(getContext());
        inputName.setHint("Artist Name");
        if (isEdit) inputName.setText(artist.getName());
        layout.addView(inputName);

        // Genre spinner
        Spinner genreSpinner = new Spinner(getContext());
        List<Genre> genres = dbHelper.getAllGenres();
        List<String> genreNames = new ArrayList<>();
        int selectedPos = 0;

        int artistGenreId = isEdit ? artist.getGenreId() : -1;
        if (genres.isEmpty()) {
            genreNames.add("N/A");
        } else {
            for (int i = 0; i < genres.size(); i++) {
                genreNames.add(genres.get(i).getName());
                if (artistGenreId == genres.get(i).getId()) {
                    selectedPos = i;
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, genreNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(adapter);
        genreSpinner.setSelection(selectedPos);
        layout.addView(genreSpinner);

        builder.setView(layout);

        // Define buttons in builder (click listeners null for now)
        builder.setPositiveButton(isEdit ? "Save" : "Add", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Positive button override
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer genreId = genres.isEmpty() ? null : genres.get(genreSpinner.getSelectedItemPosition()).getId();

            if (isEdit) {
                dbHelper.updateArtist(artist.getId(), name, genreId);
                Toast.makeText(getContext(), "Artist updated", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addArtist(name, genreId);
                Toast.makeText(getContext(), "Artist added", Toast.LENGTH_SHORT).show();
            }

            loadArtists();
            dialog.dismiss();
        });

        // Negative button override
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> dialog.dismiss());
    }



    @Override
    public void onEditClick(Artist artist) {
        showArtistDialog(artist);
    }

    @Override
    public void onDeleteClick(Artist artist) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Artist")
                .setMessage("Are you sure you want to delete " + artist.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteArtist(artist.getId());
                    loadArtists();
                    Toast.makeText(getContext(), "Artist deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
