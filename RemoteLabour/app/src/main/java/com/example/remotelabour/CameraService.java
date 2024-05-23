package com.example.remotelabour;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class CameraService extends Service {
    private CameraDevice cameraDevice = null;
    private ImageReader imageReader = null;
    private CameraCaptureSession captureSession = null;

    public CameraService(){
        //CONSTRUCTOR
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("PhotoService", "SERVIZIO CREATO");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PhotoService", "SERVIZIO INIZIATO");
        //Get Id camera
        String cameraId = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            //Ottieni le caratteristiche della fotocamera
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                stopSelf();
                Log.d("PhotoService","CHIAMATA A ONDESTROY");
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;

                    //Creo un ImageReader per catturare l'immagine
                    imageReader = ImageReader.newInstance(1920,1080, ImageFormat.JPEG, 1);
                    imageReader.setOnImageAvailableListener(onImageAvailableListener, null);

                    //Creo una sessione di cattura
                    try {
                        cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                try {
                                    captureSession = cameraCaptureSession;

                                    //Ottengo le caratteristiche della fotocamera
                                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                                    //Ottengo l'orientamento attuale del dispositivo
                                    WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                                    int rotation = windowManager.getDefaultDisplay().getRotation();

                                    //Calcolo l'orientamento JPEG desiderato
                                    int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                                    int jpegOrientation = (sensorOrientation + getJpegRotation(rotation)) % 360;

                                    //Creo una CaptureRequest e avvio l'acquisizione
                                    CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                    captureRequestBuilder.addTarget(imageReader.getSurface());
                                    captureRequestBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)100);

                                    //
                                    captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);
                                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                    captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                                    CaptureRequest captureRequest = captureRequestBuilder.build();
                                    captureSession.capture(captureRequest, null, null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                Toast.makeText(getApplicationContext(), "Impossibile creare la sessione di acquisizione", Toast.LENGTH_SHORT).show();
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    cameraDevice.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    cameraDevice.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = reader -> {
        //Ottengo l'immagine dal lettore
        Image image = reader.acquireLatestImage();

        //Ottengo i dati dell'immagine come un array di byte
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        //Salvo l'immagine nella memoria esterna
        File cartel = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!cartel.exists()) {
            cartel.mkdirs();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
        String time = simpleDateFormat.format(new Date());
        File file = new File(cartel, "IMG_"+time+".jpg");
        FileOutputStream output =null;
        try {
            output = new FileOutputStream(file);
            output .write(data);
            Toast.makeText(getApplicationContext(), "Image saved to " + file.getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output!= null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //Chiudo l'immagine
        image.close();

        //Passo il nome dell'immagine alla MainActivity
        Intent intent = new Intent("ImageNamePass");
        intent.putExtra("image", "IMG_"+time+".jpg");
        sendBroadcast(intent);
    };

    private int getJpegRotation(int deviceOrientation) {
        switch (deviceOrientation) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }

    @Override
    public void onDestroy() {
        if (cameraDevice != null) {
            cameraDevice.close();
        }
        super.onDestroy();
        Log.d("PhotoService","SERVIZIO DISTRUTTO");
    }

}