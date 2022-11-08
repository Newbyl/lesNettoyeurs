package com.example.lesnettoyeurs;

import androidx.appcompat.app.AppCompatActivity;
import java.security.MessageDigest;


import android.os.Bundle;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG= "Information" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void Connexion () throws IOException, ParserConfigurationException, SAXException, NoSuchAlgorithmException {
        String pseudo="";
        String mdp="";

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(mdp.getBytes());

        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        String mdphaché= sb.toString();


        URL url = new URL("http://51.68.124.144/nettoyeurs_srv/connexion.php"+ URLEncoder.encode(pseudo,"UTF-8")+ "&msg="+ URLEncoder.encode(mdphaché,"UTF-8"));
        URLConnection cnx = url.openConnection();
        cnx.getInputStream();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(cnx.getInputStream());
        NodeList Node = doc.getElementsByTagName("STATUS");
        org.w3c.dom.Node nodeStatus=  Node.item(0);
        String teststatus= nodeStatus.getTextContent();
        Log.d(TAG,teststatus);


    }



}