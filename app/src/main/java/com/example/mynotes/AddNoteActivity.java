package com.example.mynotes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {

    private EditText editNom, editDescription, editDate;
    private Spinner spinnerPriorite;
    private Button btnSave, btnPhoto;
    private ImageView imagePreview;
    private DatabaseHelper dbHelper;
    private String currentPhotoPath = null;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            imagePreview.setImageBitmap(imageBitmap);
                            imagePreview.setVisibility(View.VISIBLE);

                            // Sauvegarder l'image
                            currentPhotoPath = saveImageToInternalStorage(imageBitmap);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Initialiser les vues
        editNom = findViewById(R.id.editNom);
        editDescription = findViewById(R.id.editDescription);
        editDate = findViewById(R.id.editDate);
        spinnerPriorite = findViewById(R.id.spinnerPriorite);
        btnSave = findViewById(R.id.btnSave);
        btnPhoto = findViewById(R.id.btnPhoto);
        imagePreview = findViewById(R.id.imagePreview);

        dbHelper = new DatabaseHelper(this);

        // Configurer le Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priorities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriorite.setAdapter(adapter);
        spinnerPriorite.setSelection(1); // Moyenne par défaut

        // Date actuelle par défaut
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editDate.setText(currentDate);

        // Bouton photo
        btnPhoto.setOnClickListener(v -> openCamera());

        // Bouton sauvegarder
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur caméra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "NOTE_" + timeStamp + ".jpg";
            File storageDir = new File(getFilesDir(), "notes_images");

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File imageFile = new File(storageDir, imageFileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveNote() {
        String nom = editNom.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String priorite = spinnerPriorite.getSelectedItem().toString();

        if (nom.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(nom, description, date, priorite, currentPhotoPath);
        long result = dbHelper.addNote(note);

        if (result > 0) {
            Toast.makeText(this, "Note enregistrée !", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
        }
    }
}