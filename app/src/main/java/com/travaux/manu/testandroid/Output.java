package com.travaux.manu.testandroid;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Output extends AppCompatActivity {
    private Handler uiHandler;
    static final int LONG_TAMPON = 1024;
    private String ip;

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
        WifiInfo wifiInf = wifi.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        final ScrollView scroll = (ScrollView) findViewById(R.id.SV_message);

        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView text = (TextView) findViewById(R.id.TB_ConteneurMessage);
                Bundle bundle = msg.getData();
                String string = bundle.getString("msg");
                text.append(string);
                sendScroll();
                ((TextView) findViewById(R.id.TB_Message)).setText("");
            }
        };
        recevoir();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // ici on met dans le bundle les données à sauvegarder
        outState.putString("messageList", ((TextView) findViewById(R.id.TB_ConteneurMessage)).getText().toString());
        outState.putString("message", ((TextView) findViewById(R.id.TB_Message)).getText().toString());
        outState.putBoolean("checked", ((CheckBox) findViewById(R.id.CB_ShowIp)).isChecked());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // ici on récupère les données du bundle
        String messageList = savedInstanceState.getString("messageList");
        String message = savedInstanceState.getString("message");
        Boolean checked = savedInstanceState.getBoolean("checked");

        // Rétablissement des données
        ((TextView) findViewById(R.id.TB_ConteneurMessage)).setText(messageList);
        ((TextView) findViewById(R.id.TB_Message)).setText(message);
        ((CheckBox) findViewById(R.id.CB_ShowIp)).setChecked(checked);
    }

    public void recevoir() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                Intent intent = getIntent();
                String groupIp = intent.getStringExtra("cast");
                int port = intent.getIntExtra("port", -1);

                try {
                    byte tampon[] = new byte[LONG_TAMPON];

                    DatagramPacket paquet =
                            new DatagramPacket(tampon, 0, LONG_TAMPON);

                    MulticastSocket socket;
                    InetAddress group = InetAddress.getByName(groupIp);

                    try {
                        // On creer le socket
                        socket = new MulticastSocket(port);
                        // On rejoint le group de listening
                        socket.joinGroup(group);

                        while (true) {
                            try {
                                // on recoit les message et ensuite on affiche ..
                                socket.receive(paquet);
                                String chaine = new String(paquet.getData(),
                                        paquet.getOffset(), paquet.getLength());

                                bundle.putString("msg", chaine + "\n");
                                Message message = uiHandler.obtainMessage();
                                message.setData(bundle);
                                uiHandler.sendMessage(message);
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

            }
        }).start();
    }

    public void envoyerMessage(View v) {
        TextView tv = (TextView) findViewById(R.id.TB_Message);

        if (!tv.getText().toString().equals(""))
            envoyer();
    }

    public void envoyer() {
        new Thread(new Runnable() {
            Intent intent = getIntent();
            String msg = ((TextView) findViewById(R.id.TB_Message)).getText().toString();
            String pseudo = intent.getStringExtra("pseudo");
            String groupIp = intent.getStringExtra("cast");
            int port = intent.getIntExtra("port", -1);

            @Override
            public void run() {
                try {
                    if (((CheckBox) findViewById(R.id.CB_ShowIp)).isChecked()) {
                        msg = pseudo + " (" + getMobileIP() + ") : " + msg;
                    } else {
                        msg = pseudo + " : " + msg;
                    }

                    byte tampon[] = msg.getBytes();
                    InetAddress adrMulticast;
                    MulticastSocket soc;

                    adrMulticast = InetAddress.getByName(groupIp);
                    soc = new MulticastSocket();
                    soc.joinGroup(adrMulticast);


                    DatagramPacket paquet =
                            new DatagramPacket(tampon, 0, tampon.length, adrMulticast, port);
                    soc.send(paquet);


                } catch (Exception e) {
                }
            }
        }).start();
    }

    private void sendScroll() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        final ScrollView scroll = (ScrollView) findViewById(R.id.SV_message);
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }).start();
    }

    public static String getMobileIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipaddress = inetAddress.getHostAddress().toString();
                        return ipaddress;
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }
}
