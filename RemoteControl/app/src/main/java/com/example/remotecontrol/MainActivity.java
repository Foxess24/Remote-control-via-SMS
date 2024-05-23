package com.example.remotecontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //inizializzo variabili
    Toolbar toolbar;
    EditText editTextPhone, editTextMsg;
    String phone, msg;
    Button buttonSent;

    //MENU DELLA TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.MENU_1) {
            clearAll(findViewById(R.id.NumPhone), findViewById(R.id.Messaggio));
        }
        return true;
    }

    private void clearAll(EditText number, EditText sms){
        number.setText("");
        sms.setText("");
    }
    //FINE GESTIONE MENU

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //imposto ToolBar
        toolbar = findViewById(R.id.ToolBar);
        setSupportActionBar(toolbar);

        //assegno valori a variabili
        editTextMsg = findViewById(R.id.Messaggio);
        editTextPhone = findViewById(R.id.NumPhone);
        buttonSent = findViewById(R.id.SendBotton);

        editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 10) {
                    editTextPhone.setError("10 cifre senza prefisso");
                } else {
                    editTextPhone.setError(null);
                }
            }
        });



        buttonSent.setOnClickListener(v -> {
            //controllo condizione per permission
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) ==
                    PackageManager.PERMISSION_GRANTED){
                //permission garantita
                //richiamo metodo di invio
                sendSMS();
            }else{
                //permission non garantita
                //richiedo permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS},
                        100);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 100 && grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //permission garantita
            //richiamo metodo di invio
            sendSMS();
        }else{
            //permission negata
            Toast.makeText(MainActivity.this, "Permission Denied!!",Toast.LENGTH_LONG).show();
        }
    }

    private void sendSMS() {
        //get valori dalle editText
        phone = editTextPhone.getText().toString();
        msg = editTextMsg.getText().toString();

        //controllo se stringhe rispettano vincoli stabiliti
        if(phone.length() == 10 && msg.length() >= 1 && msg.length() <= 80){
            //inizializzo SMS Manager
            SmsManager smsManager = SmsManager.getDefault();
            //send message
            smsManager.sendTextMessage(phone, null, msg, null, null);
            //toast di conferma
            Toast.makeText(this, "SMS SENT", Toast.LENGTH_LONG).show();
        }else{
            //quando almeno una stringa non rispetta i rispettivi vincoli
            Toast.makeText(this, "Please enter correct phone and message", Toast.LENGTH_LONG).show();
        }
    }
}