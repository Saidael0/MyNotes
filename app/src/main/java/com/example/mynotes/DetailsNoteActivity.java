package com.example.mynotes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class DetailsNoteActivity extends AppCompatActivity {

    private static final int EDIT_NOTE_REQUEST = 100;
    private TextView textNom, textDescription, textDate, textPriorite;
    private ImageView imagePhoto;
    private Button btnDelete, btnEdit;
    private CardView dateCard, priorityCard;
    private View priorityDot;
    private DatabaseHelper dbHelper;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_note);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Détails de la Note");

        // Initialiser les vues
        textNom = findViewById(R.id.textNom);
        textDescription = findViewById(R.id.textDescription);
        textDate = findViewById(R.id.textDate);
        textPriorite = findViewById(R.id.textPriorite);
        imagePhoto = findViewById(R.id.imagePhoto);
        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnEdit);
        dateCard = findViewById(R.id.dateCard);
        priorityCard = findViewById(R.id.priorityCard);
        priorityDot = findViewById(R.id.priorityDot);

        dbHelper = new DatabaseHelper(this);

        // Récupérer la note
        currentNote = (Note) getIntent().getSerializableExtra("note");

        if (currentNote != null) {
            displayNoteDetails();
        }

        // Bouton supprimer
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        btnEdit.setOnClickListener(v -> editNote());
    }

    private void displayNoteDetails() {
        textNom.setText(currentNote.getNom());
        textDescription.setText(currentNote.getDescription());
        textDate.setText(currentNote.getDate());
        textPriorite.setText(currentNote.getPriorite());

        // Couleurs selon la priorité
        int backgroundColor, dotColor, textColor;
        switch (currentNote.getPriorite()) {
            case "Haute":
                backgroundColor = R.color.priority_high_bg;
                dotColor = R.color.priority_high;
                textColor = R.color.priority_high;
                break;
            case "Moyenne":
                backgroundColor = R.color.priority_medium_bg;
                dotColor = R.color.priority_medium;
                textColor = R.color.priority_medium;
                break;
            default:
                backgroundColor = R.color.priority_low_bg;
                dotColor = R.color.priority_low;
                textColor = R.color.priority_low;
                break;
        }

        dateCard.setCardBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        priorityCard.setCardBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        priorityDot.setBackgroundColor(ContextCompat.getColor(this, dotColor));
        textPriorite.setTextColor(ContextCompat.getColor(this, textColor));

        // Afficher la photo si disponible
        if (currentNote.getPhotoPath() != null && !currentNote.getPhotoPath().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentNote.getPhotoPath());
            if (bitmap != null) {
                imagePhoto.setImageBitmap(bitmap);
                imagePhoto.setVisibility(View.VISIBLE);
            }
        }
    }

    // Méthode pour modifier la note
    private void editNote() {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra("note", currentNote);
        startActivityForResult(intent, EDIT_NOTE_REQUEST);
    }

    //  Gérer le résultat de l'édition
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            // Récupérer la note mise à jour
            Note updatedNote = (Note) data.getSerializableExtra("updatedNote");
            if (updatedNote != null) {
                currentNote = updatedNote;
                displayNoteDetails();
                Toast.makeText(this, "Note modifiée avec succès", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la note")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette note ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    dbHelper.deleteNote(currentNote.getId());
                    Toast.makeText(this, "Note supprimée", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}