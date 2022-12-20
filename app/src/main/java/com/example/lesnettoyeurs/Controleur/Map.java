package com.example.lesnettoyeurs.Controleur;



import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.lesnettoyeurs.Modele.Joueur;
import com.example.lesnettoyeurs.Modele.Nettoyeur;
import com.example.lesnettoyeurs.R;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.lesnettoyeurs.Modele.Universite;


import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;

import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
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
    double longitude;
    double latitude;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        ItemizedIconOverlay<OverlayItem> mo = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(),item, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
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




        GeoPoint myLocationGeo = this.mLocationOverlay.getMyLocation();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.onLocationChanged(location);
        this.creationNettoyeur(joueur.getSession(), joueur.getSignature(),String.valueOf(longitude),String.valueOf(latitude));







    }




    private void creationNettoyeur (String session, String signature,String longitude, String latitude) {
        Thread tr = new Thread(new Runnable() {//Fonction qui crée un nettoyeur
            @Override
            public void run() {
                try {

                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/new_nettoyeur.php?" +
                            "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                            "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8")+
                            "&lon=" + URLEncoder.encode((longitude), "UTF-8")+
                            "&lat=" + URLEncoder.encode((latitude), "UTF-8"));
                    Log.d(TAG,url.toString());
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
                        String nom = Node1.item(0).getChildNodes().item(0).getTextContent();
                        Log.d("OK", "Creation Nettoyeur OK");
                        nettoyeur =new Nettoyeur(joueur.getSignature(), nom);
                    }
                    else if ( teststatus.equals("KO - NOT IN 3IA")){

                        Context context = getApplicationContext();
                        Log.d("KO", "Creation Nettoyeur KO");

                        int duration = Toast.LENGTH_SHORT;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CharSequence text = "Placez-vous en 3IA afin de créer votre Nettoyeur !";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                            }
                        });
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