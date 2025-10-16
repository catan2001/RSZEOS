package com.example.musiclibrarydb;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MusicLibraryActivity extends AppCompatActivity {

    private int userId;       // use int
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_library);

        // Get user data from Intent (int, not long!)
        userId = getIntent().getIntExtra("userId", -1);
        username = getIntent().getStringExtra("username");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId(); // int, not Long
            if (id == R.id.nav_artists) {
                selected = new ArtistsFragment();
            } else if (id == R.id.nav_songs) {
                selected = new SongsFragment();
            } else if (id == R.id.nav_genres) {
                selected = new GenreFragment();
            } else if (id == R.id.nav_playlists) {
                selected = createPlaylistsFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
                return true;
            }
            return false;
        });

        // Load default fragment (Artists)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ArtistsFragment())
                .commit();
    }

    private PlaylistsFragment createPlaylistsFragment() {
        PlaylistsFragment fragment = new PlaylistsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);  // pass int
        bundle.putString("username", username);
        fragment.setArguments(bundle);
        return fragment;
    }
}
