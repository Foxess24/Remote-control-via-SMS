package com.example.remotelabour;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.Interfacce.InterfacciaCallBackSound;


public class PlaySoundService extends Service {
    private final MyBinder binder = new MyBinder();
    private MediaPlayer mediaPlayer = null;
    private InterfacciaCallBackSound callBackSound = null;

    public PlaySoundService(){
        //CONSTRUCTOR
    }

    public class MyBinder extends Binder{
        PlaySoundService getService(){
            return PlaySoundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SoundService", "SERVIZIO CREATO");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.song);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            stopSelf();
            controlSideService();
        });
        Log.d("SoundService", "SERVIZIO INIZIATO");
        return START_STICKY;
    }

    //PER CONTROLLO RIPRODUZIONE DA BUTTON SU FRAGMENT
    public void riprendi(){
        mediaPlayer.start();
    }
    public void pause(){
        mediaPlayer.pause();
    }
    public MediaPlayer passMediaPlayer(){
        if(mediaPlayer!=null){
            return mediaPlayer;
        }else{
            return null;
        }
    }
    //

    public void callBack(InterfacciaCallBackSound callBackSound1){
        callBackSound = callBackSound1;
    }

    public void controlSideService(){
        Log.d("SoundService", "ESEGUITO CONTROL-SIDE-SERVICE");
        callBackSound.closeServiceSoundCallBack();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SoundService", "SERVIZIO DISTRUTTO");
    }
}
