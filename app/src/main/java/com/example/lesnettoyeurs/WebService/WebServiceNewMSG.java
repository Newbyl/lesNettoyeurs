package com.example.lesnettoyeurs.WebService;

import android.util.Log;

import com.example.lesnettoyeurs.Modele.Joueur;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WebServiceNewMSG
{
    public static final String TAG = "WSNewMessage";
    private String mAuteur=null;
    private String mContenu=null;
    private Joueur joueur;
    public WebServiceNewMSG(String auteur, String contenu, Joueur joueur)
    {
       this.mAuteur=auteur;
       this.mContenu =contenu;
       this.joueur=joueur;
    }


    public boolean call()
    {
        if (mContenu==null) return false;
        try {
            URL url =new URL("http://51.68.124.144/nettoyeurs_srv/new_msg.php?" +
                    "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                    "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8") +"&message="+mContenu);
            Log.d(TAG, url.toString());
            URLConnection cnx = url.openConnection();
            InputStream in = cnx.getInputStream();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document xml = db.parse(in);

            NodeList nl = xml.getElementsByTagName("STATUS");
            Node nodeStatus = nl.item(0);
            String status = nodeStatus.getTextContent();
            Log.d(TAG, "Thread last msg : status " + status);
            return status.startsWith("OK");
        }catch (Exception e)
        {
            return false;
        }
    }
}
