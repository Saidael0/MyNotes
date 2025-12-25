package com.example.mynotes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {

    private EditText editNom, editDescription, editDate;
    private Spinner spinnerPriorite;
    private Button btnSave, btnPhoto, btnCancel;
    private ImageView imagePreview;
    private DatabaseHelper dbHelper;
    private Note currentNote;
    private String currentPhotoPath = null;

    // Launcher pour la caméra
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

                            // Sauvegarder la nouvelle image
                            currentPhotoPath = saveImageToInternalStorage(imageBitmap);
                        }
                    }
                }
            }
    );

    // Launcher pour la permission de caméra
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            // Permission accordée, ouvrir la caméra
                            openCamera();
                        } else {
                            // Permission refusée
                            Toast.makeText(this,
                                    "La permission caméra est nécessaire pour changer la photo",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Modifier la Note");

        // Initialiser les vues
        editNom = findViewById(R.id.editNom);
        editDescription = findViewById(R.id.editDescription);
        editDate = findViewById(R.id.editDate);
        spinnerPriorite = findViewById(R.id.spinnerPriorite);
        btnSave = findViewById(R.id.btnSave);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnCancel = findViewById(R.id.btnCancel);
        imagePreview = findViewById(R.id.imagePreview);

        dbHelper = new DatabaseHelper(this);

        // Récupérer la note à modifier
        currentNote = (Note) getIntent().getSerializableExtra("note");

        if (currentNote == null) {
            Toast.makeText(this, "Erreur: note non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pré-remplir les champs avec les données actuelles
        editNom.setText(currentNote.getNom());
        editDescription.setText(currentNote.getDescription());
        editDate.setText(currentNote.getDate());

        // Configurer le Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priorities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriorite.setAdapter(adapter);

        // Sélectionner la priorité actuelle
        String[] priorities = getResources().getStringArray(R.array.priorities);
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].equals(currentNote.getPriorite())) {
                spinnerPriorite.setSelection(i);
                break;
            }
        }

        // Afficher la photo actuelle si elle existe
        if (currentNote.getPhotoPath() != null && !currentNote.getPhotoPath().isEmpty()) {
            // Garder le chemin actuel par défaut
            currentPhotoPath = currentNote.getPhotoPath();

            // Ici, vous pourriez charger et afficher l'image
            // Pour simplifier, on montre juste une icône
            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageResource(android.R.drawable.ic_menu_camera);
        }

        // Bouton photo - AVEC VÉRIFICATION DE PERMISSION
        btnPhoto.setOnClickListener(v -> checkCameraPermission());

        // Bouton sauvegarder
        btnSave.setOnClickListener(v -> updateNote());

        // Bouton annuler
        btnCancel.setOnClickListener(v -> finish());
    }

    // NOUVELLE MÉTHODE : Vérifier la permission caméra
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission déjà accordée
            openCamera();
        } else {
            // Demander la permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }



    private void openCamera() {
        // Détecter si on est sur émulateur
        boolean isEmulator = false;

        try {
            isEmulator = Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK")
                    || Build.MODEL.contains("sdk_gphone")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || Build.BRAND.startsWith("generic")
                    || Build.DEVICE.startsWith("generic")
                    || Build.PRODUCT.contains("sdk")
                    || Build.HARDWARE.contains("goldfish")
                    || Build.HARDWARE.contains("ranchu");
        } catch (Exception e) {
            // En cas d'erreur, on considère que c'est un émulateur
            isEmulator = true;
        }

        if (isEmulator) {
            // MODE TEST POUR ÉMULATEUR
            Toast.makeText(this, "Mode émulateur: image de test générée", Toast.LENGTH_SHORT).show();

            try {
                // Créer une image de test colorée avec du texte
                int width = 400;
                int height = 300;
                Bitmap testBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                // Remplir avec une couleur de fond
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int color;
                        if (x < width/2) {
                            color = 0xFF4F46E5; // Violet (votre couleur primary)
                        } else {
                            color = 0xFFA855F7; // Violet clair (accent)
                        }
                        testBitmap.setPixel(x, y, color);
                    }
                }

                // Ajouter du texte sur l'image
                android.graphics.Canvas canvas = new android.graphics.Canvas(testBitmap);
                android.graphics.Paint paint = new android.graphics.Paint();
                paint.setColor(0xFFFFFFFF); // Blanc
                paint.setTextSize(40);
                paint.setTextAlign(android.graphics.Paint.Align.CENTER);
                canvas.drawText("MyNotes", width/2, height/2, paint);
                paint.setTextSize(20);
                canvas.drawText("Image de test", width/2, height/2 + 50, paint);

                imagePreview.setImageBitmap(testBitmap);
                imagePreview.setVisibility(View.VISIBLE);

                // Sauvegarder l'image de test
                currentPhotoPath = saveImageToInternalStorage(testBitmap);

                Toast.makeText(this, "Image de test créée avec succès", Toast.LENGTH_SHORT).show();
                return;

            } catch (Exception e) {
                Toast.makeText(this, "Erreur création image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        // CODE POUR VRAI APPAREIL
        try {
            Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            // Vérifier si une app caméra est disponible
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                // Même sur vrai appareil, si pas d'app caméra, utiliser le mode test
                Toast.makeText(this,
                        "Aucune app caméra trouvée. Utilisation du mode test...",
                        Toast.LENGTH_LONG).show();

                // Créer une simple image de test
                Bitmap simpleBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                simpleBitmap.eraseColor(0xFF6200EE); // Violet

                imagePreview.setImageBitmap(simpleBitmap);
                imagePreview.setVisibility(View.VISIBLE);
                currentPhotoPath = "no_camera_test.jpg";
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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

    private void updateNote() {
        String nom = editNom.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String priorite = spinnerPriorite.getSelectedItem().toString();

        if (nom.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si aucune nouvelle photo n'a été prise, garder l'ancienne
        if (currentPhotoPath == null) {
            currentPhotoPath = currentNote.getPhotoPath();
        }

        // Mettre à jour l'objet note
        currentNote.setNom(nom);
        currentNote.setDescription(description);
        currentNote.setDate(date);
        currentNote.setPriorite(priorite);
        currentNote.setPhotoPath(currentPhotoPath);

        // Utiliser la vraie méthode update
        boolean success = dbHelper.updateNote(currentNote);

        if (success) {
            Toast.makeText(this, "Note modifiée avec succès !", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedNote", currentNote);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}