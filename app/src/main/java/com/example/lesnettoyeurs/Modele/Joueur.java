package com.example.lesnettoyeurs.Modele;

public class Joueur {

    String signature;
    String session;

    public Joueur(String session, String signature) {
        this.signature = signature;
        this.session = session;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
