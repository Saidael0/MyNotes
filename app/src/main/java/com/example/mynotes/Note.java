package com.example.mynotes;

import java.io.Serializable;

public class Note implements Serializable {
    private int id;
    private String nom;
    private String description;
    private String date;
    private String priorite;
    private String photoPath;

    // Constructeur vide
    public Note() {
    }

    // Constructeur complet
    public Note(int id, String nom, String description, String date, String priorite, String photoPath) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.date = date;
        this.priorite = priorite;
        this.photoPath = photoPath;
    }

    // Constructeur sans ID (pour l'insertion)
    public Note(String nom, String description, String date, String priorite, String photoPath) {
        this.nom = nom;
        this.description = description;
        this.date = date;
        this.priorite = priorite;
        this.photoPath = photoPath;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}