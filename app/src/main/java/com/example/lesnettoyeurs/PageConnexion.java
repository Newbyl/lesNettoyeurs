package com.example.lesnettoyeurs;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.MessageDigest;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PageConnexion extends AppCompatActivity {

    private static final String TAG= "Information" ;
    private  boolean connecter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button connexion= findViewById(R.id.seconnecter);

        connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText login = findViewById(R.id.Pseudo);
                EditText password = findViewById(R.id.Password);
                TextView messageerruer= findViewById(R.id.Messageerreur);
                if(login.length()==0 ){
                    messageerruer.setText("");
                    login.setError(getResources().getString(R.string.erreurlogin));
                }

                else if(password.length()==0 ){
                    messageerruer.setText("");
                    password.setError(getResources().getString(R.string.erreurmdp));
                }
                else{
                     if (PageConnexion.this.Connexion(login.getText().toString(),password.getText().toString())){
                        Intent intent= new Intent(PageConnexion.this, MapsActivity.class);
                        startActivity(intent);
                    }
                    else {
                      messageerruer.setText(getResources().getString(R.string.erreurconnexion));

                    }

                }

            }

        });

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private boolean Connexion (String pseudo,String mdp) {

        connecter = false;

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


                    URLConnection cnx = url.openConnection();
                    InputStream in = cnx.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(in);
                    NodeList Node = doc.getElementsByTagName("STATUS");
                    org.w3c.dom.Node nodeStatus = Node.item(0);
                    String teststatus = nodeStatus.getTextContent();
                    Log.d(TAG,teststatus);
                    if (teststatus.equals("OK")) {
                        PageConnexion.this.connecter=true;
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

        return connecter;
    }

    private void registerComponentCallbacks() {
    }


}