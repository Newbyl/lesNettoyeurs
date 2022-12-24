package com.example.lesnettoyeurs.Modele;

public class Message {
    private String text;
    private String time;
    private String pseudo;
    private int viewType;

    public Message(String text, String time, int viewType, String pseudo) {
        this.text = text;
        this.time = time;
        this.pseudo=pseudo;
        this.viewType = viewType;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getPseudo() {
        return pseudo;
    }

    public int getViewType() {
        return viewType;
    }

}
