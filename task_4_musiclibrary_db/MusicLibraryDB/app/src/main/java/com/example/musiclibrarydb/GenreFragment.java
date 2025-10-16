package com.example.musiclibrarydb;

import android.app.AlertDialog;
import android.os.Bundle;
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

import com.example.musiclibrarydb.adapters.GenreAdapter;
import com.example.musiclibrarydb.sqlite.helper.MusicLibraryDBHelper;
import com.example.musiclibrarydb.sqlite.model.Genre;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GenreFragment extends Fragment {

    private RecyclerView recyclerView;
    private GenreAdapter genreAdapter;
    private MusicLibraryDBHelper dbHelper;
    private List<Genre> genreList;
    private FloatingActionButton fabAddGenre;
    private EditText etSearchGenreName;
    private Button btnSearchGenreName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genre, container, false);

        // Views
        recyclerView = view.findViewById(R.id.recyclerGenres);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearchGenreName = view.findViewById(R.id.etSearchGenreName);
        btnSearchGenreName = view.findViewById(R.id.btnSearchGenreName);
        fabAddGenre = view.findViewById(R.id.fabAddGenre);

        // DB helper
        dbHelper = new MusicLibraryDBHelper(getContext());
        genreList = new ArrayList<>();

        // Load all genres initially
        loadGenres();

        // Add genre button
        fabAddGenre.setOnClickListener(v -> showAddGenreDialog());

        // Search button
        btnSearchGenreName.setOnClickListener(v -> searchGenresByName());

        return view;
    }

    private void loadGenres() {
        genreList.clear();
        genreList.addAll(dbHelper.getAllGenres());
        updateAdapter();
    }

    private void searchGenresByName() {
        String query = etSearchGenreName.getText().toString().trim();

        if (query.isEmpty()) {
            loadGenres(); // show all if empty
            return;
        }

        // Use % for wildcard search (starts with, ends with, contains)
        String sqlQuery = "%" + query + "%";
        genreList.clear();
        genreList.addAll(dbHelper.searchGenresByName(sqlQuery));
        updateAdapter();

        if (genreList.isEmpty()) {
            Toast.makeText(getContext(), "No genres found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAdapter() {
        if (genreAdapter == null) {
            genreAdapter = new GenreAdapter(genreList, getContext());
            recyclerView.setAdapter(genreAdapter);
        } else {
            genreAdapter.notifyDataSetChanged();
        }
    }

    private void showAddGenreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Genre");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputName = new EditText(getContext());
        inputName.setHint("Genre Name");
        layout.addView(inputName);

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            if (!name.isEmpty()) {
                dbHelper.addGenre(name);
                loadGenres();
                Toast.makeText(getContext(), "Genre added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Genre name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
