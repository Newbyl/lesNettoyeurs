package com.example.lesnettoyeurs.WebService;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.lesnettoyeurs.Modele.Joueur;
import com.example.lesnettoyeurs.Modele.Message;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WebServiceLastMSG
{
    public static final String TAG = "WSLastMessages";
    /// à ne pas exécuter dans le thread principal
    private  Joueur joueur;

    public WebServiceLastMSG(Joueur joueur) {
        this.joueur = joueur;
    }

    public ArrayList<Message> call()
    {
        try {
            URL url = new URL("http://51.68.124.144/nettoyeurs_srv/last_msgs.php?" +
                    "&session=" + URLEncoder.encode(joueur.getSession(), "UTF-8") +
                    "&signature=" + URLEncoder.encode(joueur.getSignature(), "UTF-8") );
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
            if (!status.startsWith("OK"))
                return null;
            nl = xml.getElementsByTagName("CONTENT");
            Node nodeContent = nl.item(0);

            NodeList messagesXML = nodeContent.getChildNodes();
            ArrayList<Message> aAjouter = new ArrayList<Message>();

            for (int i = 0; i < messagesXML.getLength(); i++) {
                Node message = messagesXML.item(i);
                aAjouter.add(parseMessage(message));
            }
            return aAjouter;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private Message parseMessage(Node msgNode)
    {
        int id = -1;
        int type=-1;
        String auteur = null;
        String contenu = null;
        String stringDate = null;
        NodeList messageFields = msgNode.getChildNodes();
        for (int j = 0; j < messageFields.getLength(); j++) {
            Node field = messageFields.item(j);
            if (field.getNodeName().equalsIgnoreCase("ID"))
                id = Integer.parseInt(field.getTextContent());
            else if (field.getNodeName().equalsIgnoreCase("DATESENT"))
                stringDate = field.getTextContent();
            else if (field.getNodeName().equalsIgnoreCase("AUTHOR"))
                auteur = field.getTextContent();
            else if (field.getNodeName().equalsIgnoreCase("MSG"))
                contenu = field.getTextContent();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        assert stringDate != null;
        Date date = null;
        try {
            date = formatter.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        assert auteur != null;
        assert contenu != null;
        assert date != null;


        String strDate;


        DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT);
        strDate=shortDateFormat.format(date);
        type=1;
        if (auteur.equals(joueur.getPseudo())){
            type=0;}

        return new Message(contenu, strDate, type,auteur );
    }

}
