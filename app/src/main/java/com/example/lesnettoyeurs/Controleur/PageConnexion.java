package com.example.lesnettoyeurs.Controleur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.security.MessageDigest;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lesnettoyeurs.R;
import com.example.lesnettoyeurs.Modele.Joueur;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PageConnexion extends AppCompatActivity {

    private static final String TAG= "Information" ;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private Joueur joueur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button connexion= findViewById(R.id.seconnecter);
        requestPermissionsIfNecessary(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}
        );
        connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText login = findViewById(R.id.Pseudo);
                EditText password = findViewById(R.id.Password);
                TextView message_erreur= findViewById(R.id.Messageerreur);
                if(login.length()==0 ){
                    message_erreur.setText("");
                    login.setError(getResources().getString(R.string.erreurlogin));
                }
                else if(password.length()==0 ){
                    message_erreur.setText("");
                    password.setError(getResources().getString(R.string.erreurmdp));
                }
                else{
                     if (PageConnexion.this.Connexion(login.getText().toString(),password.getText().toString())!= null ){
                        Intent intent= new Intent(PageConnexion.this, Map.class);
                        intent.putExtra("joueur_signature",joueur.getSignature());
                        intent.putExtra("joueur_session",joueur.getSession());
                        intent.putExtra("joueur-pseudo",login.getText().toString());
                        startActivity(intent);
                    }
                    else {
                      message_erreur.setText(getResources().getString(R.string.erreurconnexion));
                    }
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private Joueur Connexion (String pseudo,String mdp) {

        joueur=null;

        // haschage du mot de passe en SHA-256
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        registerComponentCallbacks();
        md.update(mdp.getBytes());

        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        String mdphaché = sb.toString();

        // COnnexion au serveur

        URL url = null;
        Thread tr;
        String finalPseudo = pseudo;
        tr = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://51.68.124.144/nettoyeurs_srv/connexion.php?" + "&login=" + URLEncoder.encode(pseudo, "UTF-8") + "&passwd=" + URLEncoder.encode(mdphaché, "UTF-8"));

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
                            String session = Node1.item(0).getChildNodes().item(0).getTextContent();
                            String signature = Node1.item(0).getChildNodes().item(1).getTextContent();
                            joueur=new Joueur(session,signature);

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

        return joueur;
    }

    private void registerComponentCallbacks() {
    }
}
