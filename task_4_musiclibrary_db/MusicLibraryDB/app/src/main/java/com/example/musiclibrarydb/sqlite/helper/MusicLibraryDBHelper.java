package com.example.musiclibrarydb.sqlite.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.musiclibrarydb.sqlite.model.User;
import com.example.musiclibrarydb.sqlite.model.Artist;
import com.example.musiclibrarydb.sqlite.model.Genre;
import com.example.musiclibrarydb.sqlite.model.Song;
import com.example.musiclibrarydb.sqlite.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class MusicLibraryDBHelper extends SQLiteOpenHelper {

    private static final String LOG = "MusicLibraryDBHelper";
    public static final String DATABASE_NAME = "MusicLibrary.db";
    public static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_GENRES = "genres";
    private static final String TABLE_ARTISTS = "artists";
    private static final String TABLE_SONGS = "songs";
    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String TABLE_PLAYLIST_SONGS = "playlist_songs";

    // Common
    private static final String KEY_ID = "id";

    // Users
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    // Genres
    private static final String KEY_GENRE_NAME = "name";

    // Artists
    private static final String KEY_ARTIST_NAME = "name";
    private static final String KEY_ARTIST_GENRE_ID = "genre_id";

    // Songs
    private static final String KEY_SONG_NAME = "name";
    private static final String KEY_SONG_ARTIST_ID = "artist_id";
    private static final String KEY_SONG_GENRE_ID = "genre_id";

    // Playlists
    private static final String KEY_PLAYLIST_NAME = "name";
    private static final String KEY_PLAYLIST_USER_ID = "user_id";

    // Playlist-Songs
    private static final String KEY_PLAYLIST_ID = "playlist_id";
    private static final String KEY_PLAYLIST_SONG_ID = "song_id";

    // Default values
    private static final String UNKNOWN_GENRE = "No Genre";
    private static final String UNKNOWN_ARTIST = "Unknown Artist";

    public MusicLibraryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // USERS
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_USERNAME + " TEXT UNIQUE NOT NULL, "
                + KEY_PASSWORD + " TEXT NOT NULL)");

        // GENRES
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_GENRES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_GENRE_NAME + " TEXT UNIQUE NOT NULL)");

        // ARTISTS
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ARTISTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_ARTIST_NAME + " TEXT NOT NULL, "
                + KEY_ARTIST_GENRE_ID + " INTEGER, "
                + "FOREIGN KEY(" + KEY_ARTIST_GENRE_ID + ") REFERENCES " + TABLE_GENRES + "(" + KEY_ID + ") ON DELETE SET NULL)");

        // SONGS
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SONGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_SONG_NAME + " TEXT NOT NULL, "
                + KEY_SONG_ARTIST_ID + " INTEGER, "
                + KEY_SONG_GENRE_ID + " INTEGER, "
                + "FOREIGN KEY(" + KEY_SONG_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + KEY_ID + ") ON DELETE SET NULL, "
                + "FOREIGN KEY(" + KEY_SONG_GENRE_ID + ") REFERENCES " + TABLE_GENRES + "(" + KEY_ID + ") ON DELETE SET NULL)");

        // PLAYLISTS
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PLAYLISTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_PLAYLIST_NAME + " TEXT NOT NULL, "
                + "description TEXT, "
                + KEY_PLAYLIST_USER_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + KEY_PLAYLIST_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ") ON DELETE CASCADE)");

        // PLAYLIST_SONGS
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PLAYLIST_SONGS + "("
                + KEY_PLAYLIST_ID + " INTEGER, "
                + KEY_PLAYLIST_SONG_ID + " INTEGER, "
                + "PRIMARY KEY(" + KEY_PLAYLIST_ID + "," + KEY_PLAYLIST_SONG_ID + "), "
                + "FOREIGN KEY(" + KEY_PLAYLIST_ID + ") REFERENCES " + TABLE_PLAYLISTS + "(" + KEY_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY(" + KEY_PLAYLIST_SONG_ID + ") REFERENCES " + TABLE_SONGS + "(" + KEY_ID + ") ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GENRES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    // -----------------------
    // GENRES
    // -----------------------
    public long addGenre(String name) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_GENRE_NAME, name);
            return db.insert(TABLE_GENRES, null, values);
        }
    }

    public ArrayList<Genre> getAllGenres() {
        ArrayList<Genre> list = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery("SELECT * FROM " + TABLE_GENRES, null)) {
            if (c.moveToFirst()) {
                do {
                    Genre g = new Genre();
                    g.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                    g.setName(c.getString(c.getColumnIndexOrThrow(KEY_GENRE_NAME)));
                    list.add(g);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public String getGenreNameById(int genreId) {
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery("SELECT " + KEY_GENRE_NAME + " FROM " + TABLE_GENRES + " WHERE " + KEY_ID + " = ?", new String[]{String.valueOf(genreId)})) {
            if (c.moveToFirst()) return c.getString(0);
        }
        return "N/A";
    }

    public Integer getGenreIdByArtistId(int artistId) {
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery("SELECT " + KEY_ARTIST_GENRE_ID + " FROM " + TABLE_ARTISTS + " WHERE " + KEY_ID + " = ?", new String[]{String.valueOf(artistId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return null;
    }

    public void updateGenre(int id, String newName) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_GENRE_NAME, newName);
            db.update(TABLE_GENRES, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }

    public void deleteGenre(int id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.beginTransaction();
            try {
                ContentValues cv = new ContentValues();
                cv.putNull(KEY_ARTIST_GENRE_ID);
                db.update(TABLE_ARTISTS, cv, KEY_ARTIST_GENRE_ID + " = ?", new String[]{String.valueOf(id)});

                cv = new ContentValues();
                cv.putNull(KEY_SONG_GENRE_ID);
                db.update(TABLE_SONGS, cv, KEY_SONG_GENRE_ID + " = ?", new String[]{String.valueOf(id)});

                db.delete(TABLE_GENRES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    // -----------------------
    // ARTISTS
    // -----------------------
    public long addArtist(String name, Integer genreId) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_ARTIST_NAME, name);
            if (genreId != null) values.put(KEY_ARTIST_GENRE_ID, genreId);
            return db.insert(TABLE_ARTISTS, null, values);
        }
    }

    public ArrayList<Artist> getAllArtists() {
        ArrayList<Artist> list = new ArrayList<>();
        String query = "SELECT a." + KEY_ID + ", a." + KEY_ARTIST_NAME + ", g." + KEY_GENRE_NAME +
                " FROM " + TABLE_ARTISTS + " a LEFT JOIN " + TABLE_GENRES + " g ON a." + KEY_ARTIST_GENRE_ID + " = g." + KEY_ID;
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(query, null)) {
            if (c.moveToFirst()) {
                do {
                    Artist a = new Artist();
                    a.setId(c.getInt(0));
                    a.setName(c.getString(1));
                    a.setGenre(c.getString(2) != null ? c.getString(2) : UNKNOWN_GENRE);
                    list.add(a);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public void updateArtist(int id, String newName, Integer newGenreId) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_ARTIST_NAME, newName);
            if (newGenreId != null) values.put(KEY_ARTIST_GENRE_ID, newGenreId);
            db.update(TABLE_ARTISTS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});

            if (newGenreId != null) {
                ContentValues songValues = new ContentValues();
                songValues.put(KEY_SONG_GENRE_ID, newGenreId);
                db.update(TABLE_SONGS, songValues, KEY_SONG_ARTIST_ID + " = ?", new String[]{String.valueOf(id)});
            }
        }
    }

    public void deleteArtist(int id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.beginTransaction();
            try {
                ContentValues cv = new ContentValues();
                cv.putNull(KEY_SONG_ARTIST_ID);
                db.update(TABLE_SONGS, cv, KEY_SONG_ARTIST_ID + " = ?", new String[]{String.valueOf(id)});

                db.delete(TABLE_ARTISTS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public Integer getArtistIdByName(String name) {
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_ARTISTS + " WHERE " + KEY_ARTIST_NAME + " = ?", new String[]{name})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return null;
    }

    public Integer getGenreIdByName(String name) {
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_GENRES + " WHERE " + KEY_GENRE_NAME + " = ?", new String[]{name})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return null;
    }

    // -----------------------
    // SONGS
    // -----------------------
    public long addSong(String name, String artistName, String genreName) {
        Integer artistId = getArtistIdByName(artistName);
        Integer genreId = artistId != null ? getGenreIdByArtistId(artistId) : getGenreIdByName(genreName);

        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_SONG_NAME, name);
            if (artistId != null) values.put(KEY_SONG_ARTIST_ID, artistId);
            if (genreId != null) values.put(KEY_SONG_GENRE_ID, genreId);
            return db.insert(TABLE_SONGS, null, values);
        }
    }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> list = new ArrayList<>();
        String query = "SELECT s." + KEY_ID + ", s." + KEY_SONG_NAME + ", a." + KEY_ARTIST_NAME + ", g." + KEY_GENRE_NAME +
                " FROM " + TABLE_SONGS + " s " +
                "LEFT JOIN " + TABLE_ARTISTS + " a ON s." + KEY_SONG_ARTIST_ID + " = a." + KEY_ID + " " +
                "LEFT JOIN " + TABLE_GENRES + " g ON s." + KEY_SONG_GENRE_ID + " = g." + KEY_ID;
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(query, null)) {
            if (c.moveToFirst()) {
                do {
                    Song s = new Song();
                    s.setId(c.getInt(0));
                    s.setName(c.getString(1));
                    s.setArtist(c.getString(2) != null ? c.getString(2) : UNKNOWN_ARTIST);
                    s.setGenre(c.getString(3) != null ? c.getString(3) : UNKNOWN_GENRE);
                    list.add(s);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public void updateSong(int id, String newName, String artistName, String genreName) {
        Integer artistId = getArtistIdByName(artistName);
        Integer genreId = artistId != null ? getGenreIdByArtistId(artistId) : getGenreIdByName(genreName);

        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_SONG_NAME, newName);
            if (artistId != null) values.put(KEY_SONG_ARTIST_ID, artistId);
            else values.putNull(KEY_SONG_ARTIST_ID);

            if (genreId != null) values.put(KEY_SONG_GENRE_ID, genreId);
            else values.putNull(KEY_SONG_GENRE_ID);

            db.update(TABLE_SONGS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }

    public void deleteSong(int id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.delete(TABLE_SONGS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }

    // -----------------------
    // PLAYLISTS
    // -----------------------
    public long addPlaylist(String name, String description, int userId) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_PLAYLIST_NAME, name);
            values.put("description", description);
            values.put(KEY_PLAYLIST_USER_ID, userId);
            return db.insert(TABLE_PLAYLISTS, null, values);
        }
    }

    public void updatePlaylist(int id, String newName, String newDescription) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_PLAYLIST_NAME, newName);
            values.put("description", newDescription != null ? newDescription : "");
            db.update(TABLE_PLAYLISTS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }

    public ArrayList<Playlist> getAllPlaylists(int userId) {
        ArrayList<Playlist> list = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PLAYLISTS + " WHERE " + KEY_PLAYLIST_USER_ID + " = ?", new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                do {
                    Playlist p = new Playlist();
                    p.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                    p.setName(c.getString(c.getColumnIndexOrThrow(KEY_PLAYLIST_NAME)));
                    p.setUserId(c.getInt(c.getColumnIndexOrThrow(KEY_PLAYLIST_USER_ID)));
                    p.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
                    list.add(p);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public void deletePlaylist(int id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.delete(TABLE_PLAYLISTS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }

    public void addSongToPlaylist(int playlistId, int songId) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(KEY_PLAYLIST_ID, playlistId);
            values.put(KEY_PLAYLIST_SONG_ID, songId);
            db.insert(TABLE_PLAYLIST_SONGS, null, values);
        }
    }

    public void removeSongFromPlaylist(int playlistId, int songId) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.delete(TABLE_PLAYLIST_SONGS,
                    KEY_PLAYLIST_ID + " = ? AND " + KEY_PLAYLIST_SONG_ID + " = ?",
                    new String[]{String.valueOf(playlistId), String.valueOf(songId)});
        }
    }

    public ArrayList<Song> getSongsInPlaylist(int playlistId) {
        ArrayList<Song> list = new ArrayList<>();
        String query = "SELECT s." + KEY_ID + ", s." + KEY_SONG_NAME + ", a." + KEY_ARTIST_NAME + ", g." + KEY_GENRE_NAME +
                " FROM " + TABLE_PLAYLIST_SONGS + " ps " +
                "LEFT JOIN " + TABLE_SONGS + " s ON ps." + KEY_PLAYLIST_SONG_ID + " = s." + KEY_ID + " " +
                "LEFT JOIN " + TABLE_ARTISTS + " a ON s." + KEY_SONG_ARTIST_ID + " = a." + KEY_ID + " " +
                "LEFT JOIN " + TABLE_GENRES + " g ON s." + KEY_SONG_GENRE_ID + " = g." + KEY_ID + " " +
                "WHERE ps." + KEY_PLAYLIST_ID + " = ?";
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(query, new String[]{String.valueOf(playlistId)})) {
            if (c.moveToFirst()) {
                do {
                    Song s = new Song();
                    s.setId(c.getInt(0));
                    s.setName(c.getString(1));
                    s.setArtist(c.getString(2) != null ? c.getString(2) : UNKNOWN_ARTIST);
                    s.setGenre(c.getString(3) != null ? c.getString(3) : UNKNOWN_GENRE);
                    list.add(s);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    // -----------------------
    // SEARCH METHODS
    // -----------------------
    public List<Song> searchSongs(String nameQuery, String genre, String artist) {
        List<Song> songs = new ArrayList<>();
        String query = "SELECT s.id, s.name, a.name, g.name " +
                "FROM songs s " +
                "LEFT JOIN artists a ON s.artist_id = a.id " +
                "LEFT JOIN genres g ON s.genre_id = g.id " +
                "WHERE s.name LIKE ? ";
        List<String> args = new ArrayList<>();
        args.add("%" + nameQuery + "%");

        if (genre != null && !genre.equals("None")) {
            query += "AND g.name LIKE ? ";
            args.add("%" + genre + "%");
        }
        if (artist != null && !artist.equals("None")) {
            query += "AND a.name LIKE ? ";
            args.add("%" + artist + "%");
        }
        query += "ORDER BY s.name ASC";

        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(query, args.toArray(new String[0]))) {
            if (c.moveToFirst()) {
                do {
                    Song s = new Song();
                    s.setId(c.getInt(0));
                    s.setName(c.getString(1));
                    s.setArtist(c.getString(2) != null ? c.getString(2) : UNKNOWN_ARTIST);
                    s.setGenre(c.getString(3) != null ? c.getString(3) : UNKNOWN_GENRE);
                    songs.add(s);
                } while (c.moveToNext());
            }
        }
        return songs;
    }

    public List<Playlist> searchPlaylistsByName(String queryName, int userId) {
        List<Playlist> playlists = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PLAYLISTS + " WHERE " + KEY_PLAYLIST_USER_ID + " = ? AND " + KEY_PLAYLIST_NAME + " LIKE ? ORDER BY " + KEY_PLAYLIST_NAME + " ASC";
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(query, new String[]{String.valueOf(userId), "%" + queryName + "%"})) {
            if (c.moveToFirst()) {
                do {
                    Playlist p = new Playlist();
                    p.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                    p.setName(c.getString(c.getColumnIndexOrThrow(KEY_PLAYLIST_NAME)));
                    p.setUserId(c.getInt(c.getColumnIndexOrThrow(KEY_PLAYLIST_USER_ID)));
                    p.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
                    playlists.add(p);
                } while (c.moveToNext());
            }
        }
        return playlists;
    }

    public List<Artist> searchArtistsByName(String nameQuery) {
        List<Artist> artists = new ArrayList<>();
        String query = "SELECT a." + KEY_ID + ", a." + KEY_ARTIST_NAME + ", g." + KEY_GENRE_NAME +
                " FROM " + TABLE_ARTISTS + " a LEFT JOIN " + TABLE_GENRES + " g ON a." + KEY_ARTIST_GENRE_ID + " = g." + KEY_ID +
                " WHERE a." + KEY_ARTIST_NAME + " LIKE ? ORDER BY a." + KEY_ARTIST_NAME + " ASC";
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(query, new String[]{"%" + nameQuery + "%"})) {
            if (c.moveToFirst()) {
                do {
                    Artist a = new Artist();
                    a.setId(c.getInt(0));
                    a.setName(c.getString(1));
                    a.setGenre(c.getString(2) != null ? c.getString(2) : UNKNOWN_GENRE);
                    artists.add(a);
                } while (c.moveToNext());
            }
        }
        return artists;
    }

    public List<Genre> searchGenresByName(String nameQuery) {
        List<Genre> genres = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_GENRES + " WHERE " + KEY_GENRE_NAME + " LIKE ? ORDER BY " + KEY_GENRE_NAME + " ASC";
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(query, new String[]{"%" + nameQuery + "%"})) {
            if (c.moveToFirst()) {
                do {
                    Genre g = new Genre();
                    g.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                    g.setName(c.getString(c.getColumnIndexOrThrow(KEY_GENRE_NAME)));
                    genres.add(g);
                } while (c.moveToNext());
            }
        }
        return genres;
    }
}
