package com.travaux.manu.testandroid;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Input extends AppCompatActivity {
    static private String castAddress = "";
    final int PORTMAX = 65535;
    final int PORTMIN = 1024;
    final int PSEUDOMINLENGTH = 2;
    boolean pseudoEstValide = false;
    boolean portEstValide = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        fillSpinner(); // Remplis le spinner
        checkInputs();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // ici on met dans le bundle les données à sauvegarder
        outState.putString("username", ((TextView) findViewById(R.id.TB_Pseudo)).getText().toString());
        outState.putInt("ip", Integer.parseInt(((TextView) findViewById(R.id.TB_Port)).getText().toString()));
        outState.putInt("spinnerPosition", ((Spinner) findViewById(R.id.region_spinner)).getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // ici on récupère les données du bundle
        String username = savedInstanceState.getString("username");
        int ip = savedInstanceState.getInt("ip");
        int spinnerPosition = savedInstanceState.getInt("spinnerPosition");

        if(username.isEmpty())
            username.equals("");
        if(ip==0)
            ip = 6000;
        if(spinnerPosition < 0 || spinnerPosition > 2)
            spinnerPosition = 0;

        // Rétablissement des données
        ((TextView) findViewById(R.id.TB_Pseudo)).setText(username);
        ((TextView) findViewById(R.id.TB_Port)).setText(String.valueOf(ip));
        ((Spinner) findViewById(R.id.region_spinner)).setSelection(spinnerPosition);
    }

    public void fillSpinner() {
        Spinner spinner;
        spinner = (Spinner) findViewById(R.id.region_spinner);
        List<String> list = new ArrayList<>();
        list.add("La Comté (tout le monde)");
        list.add("Mordor (info de gestion)");
        list.add("Isengard (info industrielle)");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        castAddress = "230.0.0.1";
                        break;
                    case 1:
                        castAddress = "230.0.0.2";
                        break;
                    case 2:
                        castAddress = "230.0.0.3";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void afficherMessages(View v) {
        final TextView portInput = (TextView) findViewById(R.id.TB_Port);
        final TextView nameInput = (TextView) findViewById(R.id.TB_Pseudo);
        Intent intent = new Intent(this, Output.class);

        intent.putExtra("cast", castAddress);
        intent.putExtra("port", Integer.parseInt(portInput.getText().toString()));
        intent.putExtra("pseudo", nameInput.getText().toString());
        startActivity(intent);
    }

    private void checkInputs() {
        final TextView nameInput = (TextView) findViewById(R.id.TB_Pseudo);
        final TextView portInput = (TextView) findViewById(R.id.TB_Port);
        nameInput.addTextChangedListener(new TextWatcher() {
                                             @Override
                                             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                             }

                                             @Override
                                             public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                 if (nameInput.length() >= PSEUDOMINLENGTH) {
                                                     pseudoEstValide = true;
                                                     nameInput.setTextColor(Color.BLACK);

                                                 } else {
                                                     Toast.makeText(getApplicationContext(), "Votre pseudo doit contenir de 2-8 caractères", Toast.LENGTH_SHORT);
                                                     pseudoEstValide = false;
                                                     nameInput.setTextColor(Color.RED);
                                                 }
                                             }

                                             @Override
                                             public void afterTextChanged(Editable s) {
                                                 if (portEstValide && pseudoEstValide) {
                                                     findViewById(R.id.BTN_Start).setEnabled(true);
                                                 } else
                                                     findViewById(R.id.BTN_Start).setEnabled(false);
                                             }
                                         }
        );
        portInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isNumeric(portInput.getText().toString())) {
                    if ((Integer.parseInt(portInput.getText().toString()) <= PORTMIN) || (Integer.parseInt(portInput.getText().toString()) >= PORTMAX)) {
                        Toast.makeText(getApplicationContext(), "Le numéro de port doit se situer entre 1024 et 65535", Toast.LENGTH_SHORT);
                        portInput.setTextColor(Color.RED);
                        portEstValide = false;
                    } else {
                        portEstValide = true;
                        portInput.setTextColor(Color.BLACK);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (portEstValide && pseudoEstValide) {
                    findViewById(R.id.BTN_Start).setEnabled(true);
                } else
                    findViewById(R.id.BTN_Start).setEnabled(false);
            }
        });
    }
    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
