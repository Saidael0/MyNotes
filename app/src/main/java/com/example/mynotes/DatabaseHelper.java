package com.example.mynotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mynotes.db";
    private static final int DATABASE_VERSION = 1;

    // Table et colonnes
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOM = "nom";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_PRIORITE = "priorite";
    private static final String COLUMN_PHOTO = "photo_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOM + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_PRIORITE + " TEXT,"
                + COLUMN_PHOTO + " TEXT"
                + ")";
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    // Ajoutez cette méthode dans la classe DatabaseHelper
    public boolean updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOM, note.getNom());
        values.put(COLUMN_DESCRIPTION, note.getDescription());
        values.put(COLUMN_DATE, note.getDate());
        values.put(COLUMN_PRIORITE, note.getPriorite());
        values.put(COLUMN_PHOTO, note.getPhotoPath());

        int rowsAffected = db.update(TABLE_NOTES, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});

        db.close();

        return rowsAffected > 0;
    }

    // Ajouter une note
    public long addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOM, note.getNom());
        values.put(COLUMN_DESCRIPTION, note.getDescription());
        values.put(COLUMN_DATE, note.getDate());
        values.put(COLUMN_PRIORITE, note.getPriorite());
        values.put(COLUMN_PHOTO, note.getPhotoPath());

        long id = db.insert(TABLE_NOTES, null, values);
        db.close();
        return id;
    }

    // Récupérer toutes les notes
    public List<Note> getAllNotes() {
        List<Note> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                note.setNom(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOM)));
                note.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                note.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                note.setPriorite(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITE)));
                note.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)));
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return noteList;
    }

    // Supprimer une note
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Rechercher des notes
    public List<Note> searchNotes(String query) {
        List<Note> noteList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NOTES +
                " WHERE " + COLUMN_NOM + " LIKE ? OR " + COLUMN_DESCRIPTION + " LIKE ?" +
                " ORDER BY " + COLUMN_ID + " DESC";

        String searchPattern = "%" + query + "%";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{searchPattern, searchPattern});

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                note.setNom(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOM)));
                note.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                note.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                note.setPriorite(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITE)));
                note.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)));
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return noteList;
    }
}