package com.example.lesnettoyeurs.Controleur;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lesnettoyeurs.Modele.Joueur;
import com.example.lesnettoyeurs.Modele.Message;
import com.example.lesnettoyeurs.R;
import com.example.lesnettoyeurs.WebService.WebServiceLastMSG;
import com.example.lesnettoyeurs.WebService.WebServiceNewMSG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tchat2 extends AppCompatActivity {


    List<Message> messageChatModelList =  new ArrayList<>();
    RecyclerView recyclerView;
    MessageChatAdapter adapter ;

    Joueur joueur;
    EditText messageET;
    ImageView sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_tchat);
        Bundle extras = getIntent().getExtras();
        ImageView back = findViewById(R.id.retour);
        if (extras != null) {
            joueur = new Joueur(extras.getString("joueur_session"),extras.getString("joueur_signature"));
            joueur.setPseudo(extras.getString("joueur-pseudo"));
        }




        messageET = (EditText)findViewById(R.id.messageET);
        sendBtn = (ImageView) findViewById(R.id.sendBtn);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(Tchat2.this, RecyclerView.VERTICAL, false);
        manager.setStackFromEnd(true);

        recyclerView.setLayoutManager(manager);


        recyclerView.smoothScrollToPosition(messageChatModelList.size());
        adapter = new MessageChatAdapter(messageChatModelList, Tchat2.this );
        recyclerView.setAdapter(adapter);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                String msg = messageET.getText().toString();
                if(msg.length()!=0) {
                    new Thread(() -> {
                        WebServiceNewMSG ws = new WebServiceNewMSG(joueur.getPseudo(), msg, joueur);
                        boolean ok = ws.call();
                        if (!ok)
                            runOnUiThread(() -> Toast.makeText(context, getResources().getString(R.string.erreurenvoiemessage), Toast.LENGTH_LONG).show());

                        else {
                            runOnUiThread(() ->
                                    {
                                        raffaichirMessages();
                                    }
                            );
                        }
                    }).start();
                    messageET.setText("");
                }
                else{

                    Toast.makeText(context, getResources().getString(R.string.erreurmessagevide), Toast.LENGTH_LONG).show();

                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2= new Intent(Tchat2.this, Map.class);
                startActivity(intent2);
                finish();
            }
        });



    }


    @Override
    public void onResume() {
        super.onResume();
        raffaichirMessages();
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    private void raffaichirMessages() {
        new Thread(() -> {
            WebServiceLastMSG ws = new WebServiceLastMSG(joueur);
            ArrayList<Message> aAjouter = ws.call();
            try {
                runOnUiThread(() -> {
                    this.deleteMessages();
                    for (Message m : aAjouter)
                    {
                       messageChatModelList.add(0,m);
                    }

                    recyclerView.smoothScrollToPosition(messageChatModelList.size());
                    adapter = new MessageChatAdapter(messageChatModelList, Tchat2.this );
                    recyclerView.setAdapter(adapter);


                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }).start();

    }


    private void deleteMessages(){
        for (int i=0; i<messageChatModelList.size();i++){
            messageChatModelList.remove(i);
        }

    }
































}
