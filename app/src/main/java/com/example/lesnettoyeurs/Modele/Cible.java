package com.example.lesnettoyeurs.Modele;

public class Cible  {
    int id;
    int value;
    Boolean estNettoyeur;
    double lon;
    double lat;
    int lifespan;

    public Cible(int id, int value, double lon, double lat, int lifespan,Boolean estNettoyeur) {
        this.id = id;
        this.value = value;
        this.estNettoyeur = estNettoyeur;
        this.lon = lon;
        this.lat = lat;
        this.lifespan = lifespan;
    }

    @Override
    public String toString() {
        return "Cible{" +
                "id=" + id +
                ", value=" + value +
                ", estNettoyeur=" + estNettoyeur +
                ", lon=" + lon +
                ", lat=" + lat +
                ", lifespan=" + lifespan +
                '}';
    }

    public Cible(int id, int value, double lon, double lat) {
        this.id = id;
        this.value = value;
        this.lon = lon;
        this.lat = lat;
        this.estNettoyeur=false;
        this.lifespan=0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Boolean getEstNettoyeur() {
        return estNettoyeur;
    }

    public void setEstNettoyeur(Boolean estNettoyeur) {
        this.estNettoyeur = estNettoyeur;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }


}
