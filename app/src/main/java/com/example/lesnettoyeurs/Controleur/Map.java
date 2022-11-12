package com.example.lesnettoyeurs.Controleur;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.lesnettoyeurs.Modele.Universite;
import com.example.lesnettoyeurs.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

public class Map extends AppCompatActivity {
    private MapView map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load( getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_map);
        Universite information= new Universite();


        //Creation map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK); //render
        map.setMultiTouchControls(true);;//zoom avec doigt

        IMapController mapController = map.getController();
        mapController.setZoom(16.5); //Dans le zoom de depart
        mapController.setCenter(information.getUniversite()); //permet de faire un centrage vers l'Universite



        // Creation de Overlay pour indiquer la position du batiment 3IA
        ArrayList<OverlayItem> item =new ArrayList<>();
        OverlayItem IA =  new OverlayItem("3IA","Creation de netoyeur", information.getBatimentinfo());
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
        polygon.setTitle("L'universite d'orlean");


        map.getOverlayManager().add(polygon);



    }
    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }




}