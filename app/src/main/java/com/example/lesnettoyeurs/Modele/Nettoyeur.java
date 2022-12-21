package com.example.lesnettoyeurs.Modele;

public class Nettoyeur {
    private String nom;
    private String signature;
    private Boolean estDetecte;
    private Float longitude;
    private Float latitude;
    private String status;
    private String value;



    public Nettoyeur(String nom, String signature) {
        this.nom = nom;
        this.signature = signature;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Boolean getEstDetecte() {
        return estDetecte;
    }

    public void setEstDetecte(Boolean estDetecte) {
        this.estDetecte = estDetecte;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
