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
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.Build;
public class AddNoteActivity extends AppCompatActivity {

    private EditText editNom, editDescription, editDate;
    private Spinner spinnerPriorite;
    private Button btnSave, btnPhoto;
    private ImageView imagePreview;
    private DatabaseHelper dbHelper;
    private String currentPhotoPath = null;

    // Code de permission pour la caméra
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

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

                            // Sauvegarder l'image
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
                                    "La permission caméra est nécessaire pour prendre des photos",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

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

        // Bouton photo - VERSION CORRIGÉE
        btnPhoto.setOnClickListener(v -> checkCameraPermission());

        // Bouton sauvegarder
        btnSave.setOnClickListener(v -> saveNote());
    }

    // NOUVELLE MÉTHODE : Vérifier la permission
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