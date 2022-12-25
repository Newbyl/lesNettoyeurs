package com.example.lesnettoyeurs.Controleur;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
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

public class Tchat2 extends Fragment {


    List<Message> messageChatModelList =  new ArrayList<>();
    RecyclerView recyclerView;
    MessageChatAdapter adapter ;

    Joueur joueur;
    EditText messageET;
    ImageView sendBtn;

    DrawerLayout drawerLayout;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.page_tchat, container, false);

        super.onCreate(savedInstanceState);


        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            joueur = new Joueur(extras.getString("joueur_session"),extras.getString("joueur_signature"));
            joueur.setPseudo(extras.getString("joueur-pseudo"));
        }




        messageET = (EditText)view.findViewById(R.id.messageET);
        sendBtn = (ImageView)view.findViewById(R.id.sendBtn);

        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        manager.setStackFromEnd(true);

        recyclerView.setLayoutManager(manager);


        recyclerView.smoothScrollToPosition(messageChatModelList.size());
        adapter = new MessageChatAdapter(messageChatModelList, getContext() );
        recyclerView.setAdapter(adapter);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                String msg = messageET.getText().toString();
                if(msg.length()!=0) {
                    new Thread(() -> {
                        WebServiceNewMSG ws = new WebServiceNewMSG(joueur.getPseudo(), msg, joueur);
                        boolean ok = ws.call();
                        if (!ok)
                            requireActivity().runOnUiThread(() -> Toast.makeText(context, getResources().getString(R.string.erreurenvoiemessage), Toast.LENGTH_LONG).show());

                        else {
                            requireActivity().runOnUiThread(() ->
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




    return  view;
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
                getActivity().runOnUiThread(() -> {
                    this.deleteMessages();
                    for (Message m : aAjouter)
                    {
                       messageChatModelList.add(0,m);
                    }

                    recyclerView.smoothScrollToPosition(messageChatModelList.size());
                    adapter = new MessageChatAdapter(messageChatModelList, getContext() );
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
        if(messageChatModelList.size()>0) {
            for (int i = 0; i < messageChatModelList.size(); i++) {
                messageChatModelList.remove(i);
            }
        }
    }







}
