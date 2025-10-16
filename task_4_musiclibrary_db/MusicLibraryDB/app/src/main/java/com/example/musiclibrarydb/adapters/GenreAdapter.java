package com.example.musiclibrarydb.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musiclibrarydb.R;
import com.example.musiclibrarydb.sqlite.helper.MusicLibraryDBHelper;
import com.example.musiclibrarydb.sqlite.model.Genre;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {

    private List<Genre> genreList;
    private MusicLibraryDBHelper dbHelper;
    private Context context;

    public GenreAdapter(List<Genre> genreList, Context context) {
        this.genreList = genreList;
        this.context = context;
        dbHelper = new MusicLibraryDBHelper(context);
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_genre, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Genre genre = genreList.get(position);
        holder.tvGenreName.setText(genre.getName());

        holder.btnEdit.setOnClickListener(v -> showEditDialog(genre, position));
        holder.btnDelete.setOnClickListener(v -> deleteGenre(genre, position));
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    private void showEditDialog(Genre genre, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Genre");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_genre, null);
        EditText etGenreName = view.findViewById(R.id.etGenreName);
        etGenreName.setText(genre.getName());

        builder.setView(view);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = etGenreName.getText().toString().trim();
            if (!newName.isEmpty()) {
                dbHelper.updateGenre(genre.getId(), newName);
                genre.setName(newName);
                notifyItemChanged(position);
                Toast.makeText(context, "Genre updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Genre name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteGenre(Genre genre, int position) {
        dbHelper.deleteGenre(genre.getId());
        genreList.remove(position);
        notifyItemRemoved(position);
        Toast.makeText(context, "Genre deleted", Toast.LENGTH_SHORT).show();
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {
        TextView tvGenreName;
        Button btnEdit, btnDelete;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGenreName = itemView.findViewById(R.id.tvGenreName);
            btnEdit = itemView.findViewById(R.id.btnEditGenre);
            btnDelete = itemView.findViewById(R.id.btnDeleteGenre);
        }
    }
}
