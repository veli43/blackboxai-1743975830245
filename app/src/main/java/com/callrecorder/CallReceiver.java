package com.callrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static boolean isRecording = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        
        if (state == null) return;

        switch (state) {
            case TelephonyManager.EXTRA_STATE_OFFHOOK:
                // Call started (either incoming or outgoing)
                if (!isRecording) {
                    Log.d(TAG, "Call started - starting recording");
                    Intent serviceIntent = new Intent(context, RecordingService.class);
                    serviceIntent.setAction("START_RECORDING");
                    context.startService(serviceIntent);
                    isRecording = true;
                }
                break;
            case TelephonyManager.EXTRA_STATE_IDLE:
                // Call ended
                if (isRecording) {
                    Log.d(TAG, "Call ended - stopping recording");
                    Intent serviceIntent = new Intent(context, RecordingService.class);
                    serviceIntent.setAction("STOP_RECORDING");
                    context.startService(serviceIntent);
                    isRecording = false;
                }
                break;
        }
    }
}