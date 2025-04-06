package com.callrecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    private static final String CHANNEL_ID = "CallRecorderChannel";
    private MediaRecorder mediaRecorder;
    private String currentFilePath;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        
        if (action != null) {
            switch (action) {
                case "START_RECORDING":
                    startRecording();
                    break;
                case "STOP_RECORDING":
                    stopRecording();
                    stopSelf();
                    break;
            }
        }
        
        return START_STICKY;
    }

    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            
            // Create directory if it doesn't exist
            File recordingsDir = new File(Environment.getExternalStorageDirectory(), "CallRecordings");
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs();
            }
            
            // Create filename with timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            currentFilePath = recordingsDir.getAbsolutePath() + "/" + "CALL_" + timeStamp + ".mp4";
            mediaRecorder.setOutputFile(currentFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.d(TAG, "Recording started: " + currentFilePath);
            startForeground(1, createNotification());
        } catch (IOException e) {
            Log.e(TAG, "Recording failed: " + e.getMessage());
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Log.d(TAG, "Recording stopped: " + currentFilePath);
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Recording")
                .setContentText("Recording in progress...")
                .setSmallIcon(R.drawable.ic_recording)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Recorder Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}