package com.example.mynotes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private DatabaseHelper dbHelper;
    private EditText searchEditText;
    private TextView totalCount, highCount, lowCount, emptyMessage;
    private List<Note> allNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les vues
        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        totalCount = findViewById(R.id.totalCount);
        highCount = findViewById(R.id.highCount);
        lowCount = findViewById(R.id.lowCount);
        emptyMessage = findViewById(R.id.emptyMessage);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Initialiser la base de données
        dbHelper = new DatabaseHelper(this);

        // Configurer RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadNotes();

        // Bouton ajouter
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

        // Recherche en temps réel
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        allNotes = dbHelper.getAllNotes();
        updateStatistics();

        if (allNotes.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new NoteAdapter(this, allNotes, note -> {
            Intent intent = new Intent(MainActivity.this, DetailsNoteActivity.class);
            intent.putExtra("note", note);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void searchNotes(String query) {
        if (query.isEmpty()) {
            adapter.updateNotes(allNotes);
        } else {
            List<Note> searchResults = dbHelper.searchNotes(query);
            adapter.updateNotes(searchResults);
        }
    }

    private void updateStatistics() {
        totalCount.setText(String.valueOf(allNotes.size()));

        int high = 0, low = 0;
        for (Note note : allNotes) {
            if (note.getPriorite().equals("Haute")) high++;
            if (note.getPriorite().equals("Basse")) low++;
        }
        highCount.setText(String.valueOf(high));
        lowCount.setText(String.valueOf(low));
    }
}