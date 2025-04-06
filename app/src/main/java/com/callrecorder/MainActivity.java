package com.callrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView statusTextView;
    private RecyclerView recordingsRecyclerView;
    private RecordingsAdapter recordingsAdapter;
    private List<File> recordingsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        recordingsRecyclerView = findViewById(R.id.recordingsRecyclerView);

        // Setup RecyclerView
        recordingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recordingsAdapter = new RecordingsAdapter(recordingsList);
        recordingsRecyclerView.setAdapter(recordingsAdapter);

        Button startRecordingButton = findViewById(R.id.startRecordingButton);
        Button stopRecordingButton = findViewById(R.id.stopRecordingButton);
        Button refreshButton = findViewById(R.id.refreshButton);

        startRecordingButton.setText(R.string.start_recording);
        stopRecordingButton.setText(R.string.stop_recording);
        statusTextView.setText(R.string.status_ready);

        startRecordingButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, RecordingService.class);
            serviceIntent.setAction("START_RECORDING");
            startService(serviceIntent);
            statusTextView.setText(R.string.status_recording);
        });

        stopRecordingButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, RecordingService.class);
            serviceIntent.setAction("STOP_RECORDING");
            startService(serviceIntent);
            statusTextView.setText(R.string.status_ready);
            refreshRecordingsList();
        });

        refreshButton.setOnClickListener(v -> refreshRecordingsList());
        
        refreshRecordingsList();
    }

    private void refreshRecordingsList() {
        File recordingsDir = new File(Environment.getExternalStorageDirectory(), "CallRecordings");
        if (recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                recordingsList.clear();
                recordingsList.addAll(Arrays.asList(files));
                recordingsAdapter.notifyDataSetChanged();
            }
        }
    }
}
