package com.travaux.manu.testandroid;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Output extends AppCompatActivity {
    private Handler uiHandler;
    static final int DELAI = 2000;
    static final int LONG_TAMPON = 1024;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiManager.MulticastLock lock
                    = wifi.createMulticastLock("HobbitChat");
            lock.acquire();
        }

        uiHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                Bundle bundle = msg.getData();
                String string = bundle.getString("msg");
                ((TextView)findViewById(R.id.TB_ConteneurMessage)).append(string);
            }
        };

        recevoir();
    }

    public void recevoir(){
        new Thread( new Runnable()
        {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                Intent intent = getIntent();

                try {
                    byte tampon[] = new byte[LONG_TAMPON];

                    DatagramPacket paquet =
                            new DatagramPacket(tampon, 0, LONG_TAMPON);

                    MulticastSocket socket;
                    InetAddress group = InetAddress.getByName(intent.getStringExtra("cast"));

                    try {
                        // On cré le socket
                        socket = new MulticastSocket(intent.getIntExtra("port", -1));
                        // On rejoit le group de listening
                        socket.joinGroup(group);
                        while (true) {
                            try {
                                // on recoit les message et ensuite on affiche ..
                                socket.receive(paquet);
                                String chaine = new String(paquet.getData(),
                                        paquet.getOffset(), paquet.getLength());

                                bundle.putString("msg", "Message: " + chaine + "Recu de " + paquet.getSocketAddress());
                            } catch (IOException e) {
                                bundle.putString("msg", e.toString());
                            }
                        }
                    } catch (IOException e) {
                        bundle.putString("msg", e.toString());
                    }

                } catch (Exception e) {
                    bundle.putString("msg", e.toString());
                }
                Message message = uiHandler.obtainMessage();
                message.setData(bundle);
                uiHandler.sendMessage(message);
            }
        }).start();
    }

    public void envoyerMessage(View v)
    {
        envoyer();
    }


    public void envoyer(){
        new Thread(new Runnable() {
            Intent intent = getIntent();
            String msg = ((TextView) findViewById(R.id.TB_Message)).getText().toString();
            @Override
            public void run() {
                try {
                    msg = intent.getStringExtra("pseudo") + ": " + msg;
                    byte tampon[] = msg.getBytes();

                    InetAddress adrMulticast = InetAddress.getByName(intent.getStringExtra("cast"));
                    MulticastSocket soc = new MulticastSocket();
                    soc.joinGroup(adrMulticast);

                    DatagramPacket paquet =
                            new DatagramPacket(tampon, 0, tampon.length, adrMulticast, intent.getIntExtra("port", -1));

                    soc.send(paquet);

                    try {
                        Thread.sleep(DELAI);
                    } catch (InterruptedException ie) {
                    }

                } catch (Exception e) {
                    System.err.println("Houston we have a problem");
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
