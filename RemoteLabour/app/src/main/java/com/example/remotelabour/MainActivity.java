package com.example.remotelabour;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.Interfacce.InterfacciaCallBackSound;
import com.example.Interfacce.InterfaceCallBackRecorder;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements InterfacciaCallBackSound, InterfaceCallBackRecorder {

    //Permission
    ActivityResultLauncher<String[]> permissionLaunch;
    private boolean isSendGaranted = false;
    private boolean isReceiveGaranted = false;
    private boolean isReadGaranted = false;
    private boolean isCameraGaranted = false;
    private boolean isRecordGaranted = false;
    private boolean isWriteGaranted = false;
    private boolean isReadStorageGaranted = false;

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    //

    //Menu
    BottomNavigationView bottomNavigationView;
    BadgeDrawable badgePhoto;
    BadgeDrawable badgeSound;
    BadgeDrawable badgeRecorder;
    //

    //X gestione SMS in arrivo
    RicezioneSMS ricezioneSMS;
    private static final String NUMERO_AUTORIZZATO1 = "5555215554";
    private static final String NUMERO_AUTORIZZATO2 = "5555215556";
    //

    //Fragment
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment photoFragment = new Photo();
    Fragment soundFragment = new PlaySound();
    Fragment homeFragment = new Home();
    Fragment recorderFragment = new Recorder();
    //

    //Servizio di gestione playmusic in background
    PlaySoundService playSoundService;
    boolean myboundSound = false;
    //

    //Servizio di gestione Rec in background
    RecorderService recorderService;
    boolean myboundRecord;
    //Fondamentale dare accesso al MIC del pc all'emulatore

    //X ricezione nome immagine da CameraService
    BroadcastReceiver imageNameReceiver = null;
    String imageName = null;
    Bundle dataPass = new Bundle(); //to pass image name to fragment
    //

    //X immagine da inviare
    Uri uri= null;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Verifica permission
        permissionLaunch = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if(result.get(Manifest.permission.SEND_SMS)!=null){
                isSendGaranted = result.get(Manifest.permission.SEND_SMS);
            }
            if(result.get(Manifest.permission.RECEIVE_SMS)!=null){
                isReceiveGaranted = result.get(Manifest.permission.RECEIVE_SMS);
            }
            if(result.get(Manifest.permission.READ_SMS)!=null){
                isReadGaranted = result.get(Manifest.permission.READ_SMS);
            }
            if(result.get(Manifest.permission.CAMERA)!=null){
                isCameraGaranted = result.get(Manifest.permission.CAMERA);
            }
            if(result.get(Manifest.permission.RECORD_AUDIO)!=null){
                isRecordGaranted = result.get(Manifest.permission.RECORD_AUDIO);
            }
            if(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=null){
                isWriteGaranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if(result.get(Manifest.permission.READ_EXTERNAL_STORAGE)!=null){
                isReadStorageGaranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
        requestPermission();


        //Set navigation menu
        bottomNavigationView = findViewById(R.id.BottomNavigation);

        fragmentManager.beginTransaction()
                .add(R.id.ContenitoreFragment,homeFragment, "HOME")
                .commit();

        badgePhoto = bottomNavigationView.getOrCreateBadge(R.id.Foto);
        badgePhoto.setVisible(false);
        badgeSound = bottomNavigationView.getOrCreateBadge(R.id.PlaySound);
        badgeSound.setVisible(false);
        badgeRecorder = bottomNavigationView.getOrCreateBadge(R.id.Registratore);
        badgeRecorder.setVisible(false);


        //Gestione fragment menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.Home:
                    fragmentManager.beginTransaction()
                            .replace(R.id.ContenitoreFragment, homeFragment)
                            .commit();
                    return true;
                case R.id.Foto:
                    fragmentManager.beginTransaction()
                            .replace(R.id.ContenitoreFragment, photoFragment)
                            .commit();
                    dataPass.putString("imageName", imageName);
                    photoFragment.setArguments(dataPass);
                    badgePhoto.setVisible(false);
                    return true;
                case R.id.PlaySound:
                    fragmentManager.beginTransaction()
                            .replace(R.id.ContenitoreFragment, soundFragment)
                            .commit();
                    badgeSound.setVisible(false);
                    return true;
                case R.id.Registratore:
                    fragmentManager.beginTransaction()
                            .replace(R.id.ContenitoreFragment, recorderFragment)
                            .commit();
                    badgeRecorder.setVisible(false);
                    return true;
            }
            return false;
        });
        //

        //Gestione + controllo messaggi in arrivo
        ricezioneSMS = new RicezioneSMS(){
            @Override
            public void onReceive(Context context, @NonNull Intent intent) {
                super.onReceive(context, intent);
                if(numero.contains(NUMERO_AUTORIZZATO1)|| numero.contains(NUMERO_AUTORIZZATO2)){
                    controlSMS(numero,testo);
                }else{
                    Toast.makeText(context, "UNAUTHORIZED NUMBER", Toast.LENGTH_LONG).show();
                }
            }
        };
        //
    }

    public void requestPermission(){
        isReceiveGaranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED;
        isReceiveGaranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED;
        isReadGaranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED;
        isRecordGaranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
        isCameraGaranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;
        isWriteGaranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
        isReadStorageGaranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;

        List<String> permissionRequest = new ArrayList<String>();

        if(!isSendGaranted){
            permissionRequest.add(Manifest.permission.SEND_SMS);
        }
        if(!isReceiveGaranted){
            permissionRequest.add(Manifest.permission.RECEIVE_SMS);
        }
        if(!isReadGaranted){
            permissionRequest.add(Manifest.permission.READ_SMS);
        }
        if(!isCameraGaranted){
            permissionRequest.add(Manifest.permission.CAMERA);
        }
        if(!isRecordGaranted){
            permissionRequest.add(Manifest.permission.RECORD_AUDIO);
        }
        if(!isWriteGaranted){
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!isReadStorageGaranted){
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if(!permissionRequest.isEmpty()){
            permissionLaunch.launch(permissionRequest.toArray(new String[0]));
        }

    }

    //Metodo per la gestione dei messaggi arrivati
    public void controlSMS(String numero, String testo){
        testo = testo.toLowerCase();
        int count = 0;
        if(testo.contains("riproduci suono")||testo.contains("play sound")){
            count++;
            badgeSound.setVisible(true);
            if(!myboundSound){
                Intent intent = new Intent(MainActivity.this, PlaySoundService.class);
                startService(intent);
                bindService(intent, connectionSound, Context.BIND_AUTO_CREATE);
                Log.d("SoundService", "SOUND SERVICE LANCIATO");
            }else{
                Log.d("SoundService", "SOUND SERVICE GIA' IN ESECUZIONE");
            }
        }
        if(testo.contains("scatta foto")||testo.contains("take a picture")) {
            count++;
            badgePhoto.setVisible(true);
            scattaFoto();
        }
        if(testo.contains("invia foto")||testo.contains("send photo")) {
            count++;
            inviaFoto(numero);
        }
        if(testo.contains("registrate audio")||testo.contains("registra suono")||testo.contains("record sound")) {
            count++;
            badgeRecorder.setVisible(true);
            if(!myboundRecord){
                Intent intent = new Intent(MainActivity.this, RecorderService.class);
                startService(intent);
                bindService(intent, connectionRecorder, Context.BIND_AUTO_CREATE);
                Log.d("RecorderService", "RECORDER SERVICE LANCIATO");
            }else{
                Log.d("RecorderService", "RECORDER SERVICE GIA' IN ESECUZIONE");
            }
        }
        if(count == 0){
            Toast.makeText(MainActivity.this, "COMMAND NOT RECOGNIZED", Toast.LENGTH_LONG).show();
        }
    }


    //Connessione a service X play sound
    private final ServiceConnection connectionSound = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

                PlaySoundService.MyBinder binder = (PlaySoundService.MyBinder) service;
                playSoundService = binder.getService();
                playSoundService.callBack(MainActivity.this);
                myboundSound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
                myboundSound = false; //maybe redundant
        }
    };

    public void closeServiceSound(){
        if(playSoundService!=null){
            if(myboundSound){
                myboundSound = false;
                unbindService(connectionSound);
                Log.d("SoundService", "ESEGUITO UNBIND");
                playSoundService.stopSelf();
                playSoundService = null;
            }
        }
    }

    @Override
    public void closeServiceSoundCallBack() {
        myboundSound = false;
        playSoundService = null;
        unbindService(connectionSound);
        Log.d("SoundService", "ESEGUITO UNBIND");
    }

    public PlaySoundService passSoundService(){
        if(playSoundService!=null){
            return playSoundService;
        }else{
            return null;
        }
    }
    //


    //Connessione a service X Rec
    private final ServiceConnection connectionRecorder = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.MyBinder binder = (RecorderService.MyBinder) service;
            recorderService = binder.getService();
            recorderService.callBack(MainActivity.this);
            myboundRecord = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myboundRecord = false;
        }
    };

    public void closeServiceRecCallBack(){
        myboundRecord = false;
        recorderService = null;
        Log.d("RecorderService", "ESEGUITO UNBIND");
        unbindService(connectionRecorder);
    }
    //


    //Start service x capture image + gestione foto scattata
    public void scattaFoto(){
        startService(new Intent(MainActivity.this, CameraService.class));
        Log.d("PhotoService", "SERVIZIO LANCIATO");

        IntentFilter filter = new IntentFilter("ImageNamePass");
        imageNameReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Salvo il nome dell'immagine
                imageName = intent.getStringExtra("image");
            }
        };
        registerReceiver(imageNameReceiver, filter);
    }
    //

    //Invio immagine
    //--->Problemi di questa funzionalità sottolineati nella documentazione prodotta<---
    public void inviaFoto(String numero){
        //Converto l'immagine in bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icona);

        //Ottengo l'URI della bitmap
        if(uri == null){
            uri = getImageUri(bitmap);
        }

        //Creato SendIntent
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("image/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "ICONA");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //Avvio l'attività per inviare l'immagine
        startActivity(Intent.createChooser(sendIntent, "Share image"));
    }
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "icona", null);
        return Uri.parse(path);
    }
    //

    @Override
    protected void onDestroy() {
        unregisterReceiver(ricezioneSMS);
        if(imageNameReceiver!=null){
            unregisterReceiver(imageNameReceiver);
        }
        if(myboundSound){
            playSoundService.stopSelf();
            unbindService(connectionSound);
            Log.d("SoundService", "ESEGUITO UNBIND");
        }
        if(myboundRecord){
            //capture recording up to this moment
            recorderService.stopRec();
            //
            recorderService.onDestroy();
            unbindService(connectionRecorder);
            Log.d("RecorderService", "ESEGUITO UNBIND");
        }
        super.onDestroy();
        Log.d("MainActivityLifeCycle", "DISTRUTTA");

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(ricezioneSMS, new IntentFilter(SMS_RECEIVED));
    }

}