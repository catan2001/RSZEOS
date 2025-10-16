package com.example.musiclibrarydb;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.musiclibrarydb.sqlite.helper.MusicLibraryDBHelper;

public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    MusicLibraryDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Login screen

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        dbHelper = new MusicLibraryDBHelper(this);

        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = getUserId(username, password);
            if (userId != -1) {
                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
            } else {
                userId = addUser(username, password);
                Toast.makeText(MainActivity.this, "New user created and logged in!", Toast.LENGTH_SHORT).show();
            }

            openHomeScreen(userId, username);
        });
    }

    // Returns user ID if exists, otherwise -1
    private int getUserId(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        }
        cursor.close();
        db.close();
        return userId;
    }

    // Adds new user and returns the new ID
    private int addUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long newUserIdLong = db.insert("users", null, values);
        db.close();
        android.util.Log.d("MainActivity", "New user ID (long) = " + newUserIdLong);
        return (int) newUserIdLong; // safe if IDs are < Integer.MAX_VALUE
    }

    private void openHomeScreen(int userId, String username) {
        Intent intent = new Intent(MainActivity.this, MusicLibraryActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }
}
