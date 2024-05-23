package com.example.remotelabour;

import android.app.Service;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.Interfacce.InterfaceCallBackRecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderService extends Service {

    private final MyBinder binder = new MyBinder();
    private InterfaceCallBackRecorder callBackRecorder = null;
    private MediaRecorder mediaRecorder = null;
    private CountDownTimer countDownTimer = null;

    public RecorderService(){
        //CONSTRUCTOR
    }

    public class MyBinder extends Binder {
        RecorderService getService(){
            return RecorderService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.d("RecorderService", "SERVIZIO CREATO");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("RecorderService", "SERVIZIO INIZIATO");

        if(isMicPresent()){
            try {
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setOutputFile(Path());
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.prepare();
                mediaRecorder.start();
                TimerCountDown();
                Toast.makeText(getApplicationContext(), "REC START", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("RecorderService", e.getMessage());
            }
        }else{
            Log.d("REC-->MIC", "Mic non presente");
        }
        return START_STICKY;
    }

    //X stabilire tempo massimo di REC
    public void TimerCountDown(){
        if(mediaRecorder!=null){
            countDownTimer = new CountDownTimer(7000,1000){ //7sec di REC max
                @Override
                public void onTick(long millisUntilFinished) {}
                @Override
                public void onFinish() {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    stopSelf();
                    callBackRecorder.closeServiceRecCallBack();
                    Toast.makeText(getApplicationContext(), "REC SAVED", Toast.LENGTH_SHORT).show();
                }
            }.start();
        }
    }
    //

    //X stabilire il path dove salvare la REC
    public String Path(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd--HH:mm:ss");
        Date now = new Date();
        String time = simpleDateFormat.format(now);
        File file = new File(musicDirectory, "REC"+time+".mp3");
        return file.getPath();
    }
    //

    //Verifica se MIC presente
    public boolean isMicPresent(){
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return true;
        }else{
            return false;
        }
    }
    //

    public void callBack(InterfaceCallBackRecorder callBackRecorder1){
        callBackRecorder = callBackRecorder1;
    }

    //Metodo richiamato da MainActivity quando MainActivity viene terminata e REC in esecuzione
    public void stopRec(){
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        stopSelf();
    }
    //

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("RecorderService", "SERVIZIO DISTRUTTO");
    }
}
