package com.example.lesnettoyeurs.Modele;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Universite {
    ArrayList<GeoPoint> geoPoints = new ArrayList<>();
    GeoPoint Universite;
    GeoPoint Batimentinfo;

    public Universite() {
        Universite=new GeoPoint(47.843843, 1.933817);
        Batimentinfo= new GeoPoint(47.845369, 1.939728) ;


        //Les lignes pour faire le contour
        GeoPoint start =new GeoPoint(47.843598, 1.924139);
        GeoPoint second =new GeoPoint(47.839969, 1.935072);
        GeoPoint trird =new GeoPoint(47.839184, 1.937390);
        GeoPoint four= new GeoPoint(47.839094, 1.938226);
        GeoPoint five= new GeoPoint(47.839094, 1.938725);
        GeoPoint six= new GeoPoint(47.839174, 1.938922);
        GeoPoint seven= new GeoPoint(47.839174, 1.938922);
        GeoPoint eight= new GeoPoint(47.839397, 1.939140);
        GeoPoint nine= new GeoPoint( 47.840650, 1.939620);
        GeoPoint ten=  new GeoPoint(47.841150, 1.939892);
        GeoPoint eleven = new GeoPoint(47.841610, 1.940344);
        GeoPoint douze = new GeoPoint(47.841954, 1.940880);
        GeoPoint treize = new GeoPoint(47.842254, 1.941223);
        GeoPoint quatorze= new GeoPoint(47.843382, 1.941993);
        GeoPoint quinze=new GeoPoint( 47.843857, 1.942088);
        GeoPoint seize = new GeoPoint(47.845726, 1.942062);
        GeoPoint dixsept= new GeoPoint(47.846279, 1.940885);
        GeoPoint dixhuit=new GeoPoint(47.850062, 1.929662);
        GeoPoint dixneuf=new GeoPoint( 47.849369, 1.929290);
        GeoPoint vingt= new GeoPoint(47.848818, 1.929150);
        GeoPoint vingt_un= new GeoPoint(47.848088, 1.928649);
        GeoPoint vingt_deux= new GeoPoint(47.846948, 1.928632);
        GeoPoint vingt_trois=new GeoPoint(47.846215, 1.927854);
        GeoPoint vingt_quatre=new GeoPoint(47.845741, 1.927061);
        GeoPoint vingt_cinq= new GeoPoint(47.844531, 1.924617);

        geoPoints.add(start);
        geoPoints.add(second);
        geoPoints.add(trird);
        geoPoints.add(four);
        geoPoints.add(five);
        geoPoints.add(six);
        geoPoints.add(seven);
        geoPoints.add(eight);
        geoPoints.add(nine);
        geoPoints.add(ten);
        geoPoints.add(eleven);
        geoPoints.add(douze);
        geoPoints.add(treize);
        geoPoints.add(quatorze);
        geoPoints.add(quinze);
        geoPoints.add(seize);
        geoPoints.add(dixsept);
        geoPoints.add(dixhuit);
        geoPoints.add(dixneuf);
        geoPoints.add(vingt);
        geoPoints.add(vingt_un);
        geoPoints.add(vingt_deux);
        geoPoints.add(vingt_trois);
        geoPoints.add(vingt_quatre);
        geoPoints.add(vingt_cinq);

    }

    public ArrayList<GeoPoint> getGeoPoints() {
        return geoPoints;
    }

    public GeoPoint getUniversite() {
        return Universite;
    }

    public GeoPoint getBatimentinfo() {
        return Batimentinfo;
    }
}
