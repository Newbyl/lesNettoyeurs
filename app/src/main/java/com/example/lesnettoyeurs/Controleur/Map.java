package com.example.lesnettoyeurs.Controleur;



import static android.content.ContentValues.TAG;

import static java.lang.Float.parseFloat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.lesnettoyeurs.Modele.Cible;
import com.example.lesnettoyeurs.Modele.Joueur;
import com.example.lesnettoyeurs.Modele.Nettoyeur;
import com.example.lesnettoyeurs.R;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.lesnettoyeurs.Modele.Universite;


import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;

import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Map extends AppCompatActivity implements LocationListener  {
    private MapView map;
    private View mMapView;
    private MyLocationNewOverlay mLocationOverlay;
    private Joueur joueur;
    public LocationManager locationManager;
    private Nettoyeur nettoyeur;
    private double longitude;
    private double latitude;
    private String statsEquipe;
    private String statsSolo;
    private String provider;
    private Boolean stopMap=false;


    ArrayList<Cible> listeCibles = new ArrayList<Cible>();

    ItemizedIconOverlay<OverlayItem> mo;

    // attributs pour l'update toutes les 15 secondes
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 16 * 1000; // 15000 milliseconds = 15 sec





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load( getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_map);
        Universite information= new Universite();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            joueur = new Joueur(extras.getString("joueur_session"),extras.getString("joueur_signature"));
        }

        //Creation map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK); //render
        map.setMultiTouchControls(true);;//zoom avec doigt
        IMapController mapController = map.getController();
        mapController.setZoom(16.5); //Dans le zoom de depart
        mapController.setCenter(information.getUniversite()); //permet de faire un centrage vers l'Universite
        // Creation de Overlay pour indiquer la position du batiment 3IA
        ArrayList<OverlayItem> item =new ArrayList<>();
        OverlayItem IA =  new OverlayItem("3IA","Creation de nettoyeur", information.getBatimentinfo());
        item.add(IA);
        mo = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(),item, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                return false;
            }
        });
        mo.setDrawFocusedItem(true);
        map.getOverlays().add(mo);
        //Creation du contour de l'Universite
        Polygon polygon = new Polygon();
        polygon.getOutlinePaint().setColor(Color.argb(75, 255,0,0));
        polygon.setPoints(information.getGeoPoints());
        polygon.setTitle("L'universite d'Orleans");
        map.getOverlayManager().add(polygon);
        Bitmap pBleu = BitmapFactory.decodeResource(getResources(), R.drawable.pbleu);
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        this.mLocationOverlay.setDirectionArrow( pBleu, pBleu );
        this.mLocationOverlay.setPersonIcon(pBleu);
        this.mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, true);
        Log.d("PROVIDER : ", provider);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(provider);
                if(location != null) {
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                }
                locationManager.requestLocationUpdates(provider, 1, 0, this);

                this.onLocationChanged(location);
                this.creationNettoyeur();
            }
            else{
                Location location = locationManager.getLastKnownLocation(provider);
                if(location != null) {
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                }
                locationManager.requestLocationUpdates(provider, 1, 0, this);

                this.onLocationChanged(location);
                this.creationNettoyeur();
            }
        }

        if (!stopMap){//permet de cut le reste de la fonction si on est mort
            this.updateNettoyeur();
            // Afficher toutes les cibles
            this.updatePosition();

            ImageButton imageButton = (ImageButton) findViewById(R.id.BoutonVoyage);


            if (nettoyeur.getStatus().equals("VOY")) {
                imageButton.setBackgroundColor(0x88888888);
                imageButton.setImageResource(R.drawable.atterissage);
            } else {
                imageButton.setBackgroundColor(0x88888888);
                imageButton.setImageResource(R.drawable.decollage);
            }
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateNettoyeur();
                    if (nettoyeur.getStatus().equals("VOY")) {
                        remiseEnJeu(v);
                        imageButton.setImageResource(R.drawable.decollage);
                    } else if (nettoyeur.getStatus().equals("PACK")) {
                        Context context = getApplicationContext();
                        Log.d("KO", " Nettoyeur PACK");
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Vous êtes en préparation pour voyager !";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        });
                    } else {
                        updatePosition();
                        miseEnModeVoyage(v);

                        imageButton.setBackgroundColor(0x00000000);
                        imageButton.setImageResource(R.drawable.clock);
                        imageButton.setEnabled(false);
                        new Handler().postDelayed(new Runnable() {// Ajout d'un délai et une image sur le bouton de mise en voyage afin de savoir quand on est en train de se préparer
                            @Override
                            public void run() {
                                imageButton.setBackgroundColor(0x88888888);
                                imageButton.setImageResource(R.drawable.atterissage);
                                imageButton.setEnabled(true);
                            }
                        }, 60000);
                    }
                }
            });


            handler.postDelayed(runnable = new Runnable() {
                public void run() {
                    updateStats();
                    updateNettoyeur();// Ajout d'un check sur le status du nettoyeur afin de savoir s'il est mort. Si oui on tente d'en refaire un
                    if (nettoyeur.getStatus().equals("DEAD")) {
                        Context context = getApplicationContext();
                        Log.d("KO", "Mort nettoyeur");
                        int duration = Toast.LENGTH_SHORT;
                        CharSequence text = "Vous êtes mort!";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        creationNettoyeur();
                    }
                    if (!(nettoyeur.getStatus().equals("VOY") || nettoyeur.getStatus().equals("PACK"))) {
                        updatePosition();
                    }
                    updateTableau();
                    handler.postDelayed(runnable, delay);
                }
            }, 0);
        }
        else{
            finish();
            return;
        }


    }

    private  void updateNettoyeur(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = this.locationManager.getLastKnownLocation(provider);
            this.onLocationChanged(location);
        }
        Thread tr = new Thread(new Runnable() {//Fonction qui crée un nettoyeur
            @Override
            public void run() {
                try {
                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/stats_nettoyeur.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8") );
                    Log.d(TAG, url.toString());
                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();
                    if (teststatus.equals("OK")) {
                        NodeList Node1 = doc.getElementsByTagName("PARAMS");
                        String nom = Node1.item(0) .  getChildNodes().item(0).getTextContent();
                        String value = Node1.item(0) .  getChildNodes().item(1).getTextContent();
                        String pos_lon = Node1.item(0) .  getChildNodes().item(2).getTextContent();
                        String pos_lat = Node1.item(0) .  getChildNodes().item(3).getTextContent();
                        String status = Node1.item(0) .  getChildNodes().item(4).getTextContent();
                        statsSolo= "Nom : "+nom+"\n" +
                                "Status : "+ status+"\n"+
                                "Points actuels : "+value;

                        Log.d("OK", "Stats nettoyeur OK");
                        if (nettoyeur!=null){
                            nettoyeur =new Nettoyeur(joueur.getSignature(), nom);
                        }
                        nettoyeur.setLatitude(parseFloat(pos_lat));
                        nettoyeur.setLongitude(parseFloat(pos_lon));
                        nettoyeur.setValue(value);
                        nettoyeur.setNom(nom);
                        nettoyeur.setStatus(status);


                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        });
        tr.start();
        try {
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void creationNettoyeur () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = this.locationManager.getLastKnownLocation(provider);
            this.onLocationChanged(location);
        }
        Thread tr = new Thread(new Runnable() {//Fonction qui crée un nettoyeur
            @Override
            public void run() {
                try {
                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/new_nettoyeur.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8")+
                            "&lon=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8")+
                            "&lat=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8"));
                    Log.d(TAG,url.toString());
                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;

                    if (teststatus.equals("OK")) {
                        NodeList Node1 = doc.getElementsByTagName("PARAMS");
                        String nom = Node1.item(0) .  getChildNodes().item(0).getTextContent();
                        Log.d("OK", "Creation Nettoyeur OK");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Bienvenue "+nom+" dans le jeu des Nettoyeurs";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        });
                        stopMap=false;
                        nettoyeur =new Nettoyeur(joueur.getSignature(), nom);
                    }
                    else if ( teststatus.equals("KO - NOT IN 3IA")){
                        Log.d("KO", "Creation Nettoyeur 3IA");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Placez-vous en 3IA afin de créer votre Nettoyeur !";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                            }
                        });
                        stopMap=true;
                        finish();

                    }
                    else{
                        URL url_ = new URL("http://51.68.124.144/nettoyeurs_srv/stats_nettoyeur.php?" +
                                "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                                "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8"));
                        Log.d(TAG,url_.toString());
                        URLConnection cnx_ = url_.openConnection();
                        InputStream in_ = cnx_.getInputStream();
                        DocumentBuilderFactory dbf_ = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db_ = dbf_.newDocumentBuilder();
                        Document doc_ = db_.parse(in_);
                        NodeList Node_ = doc_.getElementsByTagName("STATUS");
                        org.w3c.dom.Node nodeStatus_ = Node_.item(0);
                        String teststatus_ = nodeStatus_.getTextContent();
                        Log.d("KO", "Nettoyeur déjà la KO");
                        if (teststatus_.equals("OK")) {
                            NodeList Node1_ = doc_.getElementsByTagName("PARAMS");
                            String nom = Node1_.item(0).getChildNodes().item(0).getTextContent();
                            Log.d("NOM :", nom);
                            nettoyeur =new Nettoyeur(joueur.getSignature(), nom);
                            stopMap=false;
                        }
                    }



                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }

            }
        });
        tr.start();
        try {
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void remiseEnJeu(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = this.locationManager.getLastKnownLocation(provider);
            this.onLocationChanged(location);
        }
        Thread tr = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/remise_en_jeu.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8") +
                            "&lon=" + URLEncoder.encode(String.valueOf((longitude)), "UTF-8") +
                            "&lat=" + URLEncoder.encode(String.valueOf((latitude)), "UTF-8"));
                    Log.d(TAG, url.toString());
                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();
                    if (teststatus.equals("OK")) {
                        Context context = getApplicationContext();
                        Log.d("KO", "retour en jeu OK");
                        int duration = Toast.LENGTH_SHORT;
                        updatePosition();
                        updateNettoyeur();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Vous êtes bien revenu en jeu, mais vous avez été détecté !";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        });
                    }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
            }
        });
        tr.start();
        try {
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void miseEnModeVoyage(View view){
        Thread tr = new Thread(new Runnable() {//Fonction qui crée un nettoyeur
            @Override
            public void run() {
                try {
                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/mode_voyage.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8"));
                    Log.d(TAG, url.toString());
                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();

                    if (teststatus.equals("OK")) {

                        Log.d("OK", "mode VOYAGE");
                        final int DURATION = 60; // duration in seconds
                        final int INTERVAL = 10; // interval in seconds


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Handler handler = new Handler();
                                Toast.makeText(getApplicationContext(), "Veuillez rester immobile 1 minutes\n afin de passer en mode voyage", Toast.LENGTH_SHORT).show();
                                handler.postDelayed(new Runnable() {

                                    final Context context = getApplicationContext();
                                    final Toast toast = Toast.makeText(context, "Il reste 30 secondes", Toast.LENGTH_SHORT);
                                    @Override
                                    public void run() {
                                        toast.show();
                                    }
                                }, 30000);
                                handler.postDelayed(new Runnable() {
                                    final Context context = getApplicationContext();
                                    final Toast toast = Toast.makeText(context, "Il reste 15 secondes", Toast.LENGTH_SHORT);
                                    @Override
                                    public void run() {
                                        toast.show();
                                    }
                                }, 45000);
                                handler.postDelayed(new Runnable() {
                                    final Context context = getApplicationContext();
                                    final Toast toast = Toast.makeText(context, "Vous êtes désormais en mode voyage" +
                                                    "\n rappuyez sur l'avion afin de retourner en jeu"
                                            , Toast.LENGTH_SHORT);
                                    @Override
                                    public void run() {
                                        toast.show();
                                    }
                                }, 60000);

                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        });
        tr.start();
        try {
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void updatePosition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = this.locationManager.getLastKnownLocation(provider);
            this.onLocationChanged(location);
        }
        Thread tr = new Thread(new Runnable() {//Fonction qui crée un nettoyeur
            @Override
            public void run() {
                try {

                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/deplace.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8")+
                            "&lon=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8")+
                            "&lat=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8"));
                    Log.d(TAG,url.toString());
                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();
                    //KO - AGENT TRANSITING
                    Log.d("teststatus",teststatus);
                    if (teststatus.equals("OK")) {

                        // On clear la liste pour ajouté les nouveaux
                        listeCibles.clear();

                        NodeList detectedCTR = doc.getElementsByTagName("PARAMS").item(0).getChildNodes().item(0).getChildNodes();
                        //items
                        Log.d("LengthCTR",detectedCTR.getLength()+"");
                        for (int i = 0;i<detectedCTR.getLength();i++){
                            NodeList attributItem = detectedCTR.item(i).getChildNodes();
                            int id=-1;
                            int value=-1;
                            double lon=-1;
                            double lat=-1;
                            for (int j=0;j<attributItem.getLength();j++){
                                Node field=attributItem.item(j);
                                if (field.getNodeName().equalsIgnoreCase("cible_id")){
                                    id=Integer.parseInt(field.getTextContent());
                                }
                                if (field.getNodeName().equalsIgnoreCase("value")){
                                    value=Integer.parseInt(field.getTextContent());
                                }
                                if (field.getNodeName().equalsIgnoreCase("lon")){
                                    lon=Double.parseDouble(field.getTextContent());
                                }
                                if (field.getNodeName().equalsIgnoreCase("lat")){
                                    lat=Double.parseDouble(field.getTextContent());
                                }
                            }
                            Cible ctr=new Cible(id,value,lon,lat);
                            listeCibles.add(ctr);
                            Log.d("Cible trouvé :", ctr.toString());
                        }



                        NodeList detectedNET = doc.getElementsByTagName("PARAMS").item(0).getChildNodes().item(1).getChildNodes();
                        Log.d("Length",detectedNET.getLength()+"");
                        for (int i = 0;i<detectedNET.getLength();i++){
                            NodeList attributItem = detectedNET.item(i).getChildNodes();
                            int id=-1;
                            int value=-1;
                            double lon=-1;
                            double lat=-1;
                            int lifespan=-1;
                            Boolean estNettoyeur=true;

                            for (int j=0;j<attributItem.getLength();j++){
                                Node field=attributItem.item(j);
                                if (field.getNodeName().equalsIgnoreCase("net_id")){
                                    id=Integer.parseInt(field.getTextContent());
                                }
                                if (field.getNodeName().equalsIgnoreCase("value")){
                                    value=Integer.parseInt(field.getTextContent());
                                }
                                if (field.getNodeName().equalsIgnoreCase("lon")){
                                    lon=Double.parseDouble(field.getTextContent());
                                }
                                if (field.getNodeName().equalsIgnoreCase("lat")){
                                    lat=Double.parseDouble(field.getTextContent());
                                }
                                if (field.getNodeName().equalsIgnoreCase("lifespan")){
                                    lifespan=Integer.parseInt(field.getTextContent());
                                }
                            }
                            Cible net = new Cible(id,value,lon,lat,lifespan,estNettoyeur);
                            listeCibles.add(net);

                            Log.d("Nettoyeur trouvé :", net.toString());
                        }



                    }
                    else if (teststatus.equals("KO - AGENT PACKING FOR TRANSIT")){
                        Log.d("Update position", "KO - AGENT PACKING FOR TRANSIT");
                        listeCibles.clear();
                    }
                    else if (teststatus.equals("KO - AGENT TRANSITING")){
                        Log.d("Update position", "KO - AGENT TRANSITING");
                        listeCibles.clear();
                    }
                    Bitmap inspecteur = BitmapFactory.decodeResource(getResources(), R.drawable.nettoyeur);
                    Bitmap cible_ = BitmapFactory.decodeResource(getResources(), R.drawable.cible);
                    ArrayList<OverlayItem> itemCTR = new ArrayList<>();
                    ArrayList<OverlayItem> itemNET = new ArrayList<>();
                    Drawable cibleDrawable = new BitmapDrawable(getResources(), cible_);
                    Drawable inspecteurDrawable = new BitmapDrawable(getResources(), inspecteur);



                    for (int i=0;i<listeCibles.size();i++) {
                        OverlayItem cible;
                        if (listeCibles.get(i).getEstNettoyeur()) {
                            cible = new OverlayItem("Nettoyeur ayant pour valeur : "+listeCibles.get(i).getValue()+"", listeCibles.get(i).getId()+"", new GeoPoint(listeCibles.get(i).getLat(), listeCibles.get(i).getLon()));
                            cible.setMarker(inspecteurDrawable);
                            itemNET.add(cible);

                        } else {
                            cible = new OverlayItem("Cible ayant pour valeur : "+listeCibles.get(i).getValue()+"", listeCibles.get(i).getId()+"", new GeoPoint(listeCibles.get(i).getLat(), listeCibles.get(i).getLon()));
                            cible.setMarker(cibleDrawable);
                            itemCTR.add(cible);
                        }


                    }
                        ItemizedIconOverlay<OverlayItem> ciblesNET = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(),itemNET, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                                nettoyerNET(item.getSnippet());
                                return true;
                            }

                            @Override
                            public boolean onItemLongPress(int index, OverlayItem item) {
                                Context context = getApplicationContext();
                                Log.d("longItemPressNET", item.getTitle());
                                int duration = Toast.LENGTH_SHORT;
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        CharSequence text = item.getTitle();
                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }
                                });
                                return true;
                            }
                        });
                    ItemizedIconOverlay<OverlayItem> ciblesCTR = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(),itemCTR, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(int index, OverlayItem item) {
                            nettoyerCTR(item.getSnippet());
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(int index, OverlayItem item) {
                            Context context = getApplicationContext();
                            Log.d("longItemPressCTR", item.getTitle());
                            int duration = Toast.LENGTH_SHORT;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    CharSequence text = item.getTitle();
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            });
                            return true;
                        }
                    });




                    if (map.getOverlays().size()>=5){
                        Log.d("SIZE",map.getOverlays().size()+"");
                        map.getOverlays().remove(4);
                        map.getOverlays().remove(3);

                        map.postInvalidate();
                        map.getOverlays().add(ciblesCTR);
                        map.getOverlays().add(ciblesNET);
                    }
                    else{
                        map.getOverlays().add(ciblesCTR);
                        map.getOverlays().add(ciblesNET);
                    }





                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }

            }
        });
        tr.start();
        try {
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void nettoyerNET(String id){
        Thread tr = new Thread(new Runnable() {//Fonction qui crée un nettoyeur
            @Override
            public void run() {
                try {
                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/frappe_net.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8")+
                            "&net_id=" + URLEncoder.encode(id, "UTF-8"));
                    Log.d(TAG, url.toString());
                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();
                    NodeList Node1 = doc.getElementsByTagName("PARAMS");

                    if (teststatus.equals("OK")) {
                        int outcome = Integer.parseInt(Node1.item(0).getChildNodes().item(0).getTextContent());

                        String text="";
                        if (outcome==1 ){
                            text="Vous avez nettoyer le nettoyeur ennemi bien joué !";
                        }
                        if (outcome==0 ){
                            text="Vous n'avez pas réussi à nettoyer le nettoyeur ennemi, dommage ! ";
                        }
                        Context context = getApplicationContext();
                        Log.d("OK", "Tentative cible (NET)");
                        final String text2=text;
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast toast = Toast.makeText(context, text2, duration);
                                toast.show();
                            }
                        });
                    }
                    else if (teststatus.equals("KO - AGENT PACKING FOR TRANSIT")){
                        Context context = getApplicationContext();
                        Log.d("KO", "préparation voyage impossible de nettoyer (NET)");
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Vous êtes en préparation pour le mode voyage, vous ne pouvez pas nettoyer";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }

                        });
                    }
                    else if (teststatus.equals("KO - AGENT TRANSITING")){
                        Context context = getApplicationContext();
                        Log.d("KO", "tentative nettoyage en voyage (NET)");
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Vous êtes en mode voyage, vous ne pouvez pas nettoyer";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }

                        });
                    }
                    else {
                        Context context = getApplicationContext();
                        Log.d("KO", "Trop loin pour nettoyer la cible (NET)");
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Vous êtes trop loin pour nettoyer la cible !";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }

                    });
                    }
                    updatePosition();
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        });
        tr.start();
        try {
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void nettoyerCTR(String id){
        Thread tr = new Thread(new Runnable() {//Fonction qui crée un nettoyeur
            @Override
            public void run() {
                try {
                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/frappe_cible.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8")+
                            "&cible_id=" + URLEncoder.encode(id, "UTF-8"));
                    Log.d(TAG, url.toString());
                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();
                    NodeList Node1 = doc.getElementsByTagName("PARAMS");

                    if (teststatus.equals("OK")) {
                        int outcome = Integer.parseInt(Node1.item(0).getChildNodes().item(0).getTextContent());
                        int detected = Integer.parseInt(Node1.item(0).getChildNodes().item(1).getTextContent());
                        String text="";
                        if (outcome==1 && detected==0){
                            text="Vous avez nettoyer la cible et n'avez pas été detecté !";
                        }
                        if (outcome==1 && detected==0){
                            text="Vous avez nettoyer la cible, malheureusement vous avez été detecté ";
                        }
                        if (outcome==0 && detected==1){
                            text="C'est une catastrophe, vous n'avez pas réussi à nettoyer la cible et avez été detecté ! ";
                        }
                        if (outcome==1 && detected==1){
                            text="Vous avez été detecté et avez nettoyé la cible ";
                        }
                        Context context = getApplicationContext();
                        Log.d("OK", "Tentative cible (CTR)");
                        final String text2=text;
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast toast = Toast.makeText(context, text2, duration);
                                toast.show();
                            }
                        });
                    }
                    else if (teststatus.equals("KO - AGENT PACKING FOR TRANSIT")){
                        Context context = getApplicationContext();
                        Log.d("KO", "préparation voyage impossible de nettoyer (CTR)");
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Vous êtes en préparation pour le mode voyage, vous ne pouvez pas nettoyer";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }

                        });
                    }
                    else if (teststatus.equals("KO - AGENT TRANSITING")){
                        Context context = getApplicationContext();
                        Log.d("KO", "tentative nettoyage en voyage (CTR)");
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Vous êtes en mode voyage , vous ne pouvez pas nettoyer";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }

                        });
                    }
                    else {
                        Context context = getApplicationContext();
                        Log.d("KO", "Trop loin pour nettoyer la cible (CTR)");
                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Vous êtes trop loin pour nettoyer la cible !";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }

                        });
                    }
                    updatePosition();
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        });
        tr.start();
        try {
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void updateStats(){

        Thread tr = new Thread(new Runnable() {//Fonction qui crée un nettoyeur
            @Override
            public void run() {
                try {

                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/stats_equipe.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8"));
                    Log.d(TAG,url.toString());
                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();
                    Log.d("teststatus",teststatus);
                    if (teststatus.equals("OK")) {
                        String value  = doc.getElementsByTagName("PARAMS").item(0).getChildNodes().item(0).getTextContent();
                        String adv_value  = doc.getElementsByTagName("PARAMS").item(0).getChildNodes().item(1).getTextContent();
                        String active_member  = doc.getElementsByTagName("PARAMS").item(0).getChildNodes().item(2).getTextContent();
                        statsEquipe ="Votre équipe possède "+value+" points \n"+
                                "L'équipe adverse possède "+adv_value+" points \n"+
                                "Il y a "+active_member+" joueurs en ligne";
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }

            }
        });
        tr.start();
        try {
            tr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void updateTableau(){
         TextView tableau = (TextView) findViewById(R.id.tableauStats);
         tableau.setText(statsSolo+" \n"+statsEquipe);
         //tableau.setTextColor(0xFF000000);
         //tableau.setBackgroundColor(0x88000000);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMapView = new MapView(inflater.getContext());
        return mMapView;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
         longitude = location.getLongitude();
         latitude = location.getLatitude();
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}