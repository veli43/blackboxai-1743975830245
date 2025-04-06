package com.callrecorder;

import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.RecordingViewHolder> {
    private final List<File> recordings;
    private MediaPlayer mediaPlayer;
    private int currentPosition = -1;
    private final Handler handler = new Handler();
    private Runnable updateSeekBar;

    public RecordingsAdapter(List<File> recordings) {
        this.recordings = recordings;
    }

    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recording_item, parent, false);
        return new RecordingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
        File recording = recordings.get(position);
        holder.recordingTitle.setText(recording.getName());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = sdf.format(new Date(recording.lastModified()));
        holder.recordingDate.setText(String.format("Date: %s", date));

        holder.playButton.setOnClickListener(v -> {
            if (currentPosition == position) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        holder.playButton.setImageResource(R.drawable.ic_play);
                    } else {
                        mediaPlayer.start();
                        holder.playButton.setImageResource(R.drawable.ic_pause);
                    }
                }
            } else {
                playRecording(recording, holder, position);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle(R.string.delete_confirmation)
                .setPositiveButton(R.string.delete_confirm, (dialog, which) -> {
                    if (currentPosition == position && mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        currentPosition = -1;
                    }
                    recordings.remove(position);
                    notifyItemRemoved(position);
                    recording.delete();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        });
    }

    private void playRecording(File recording, RecordingViewHolder holder, int position) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(recording.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            currentPosition = position;
            holder.playButton.setImageResource(R.drawable.ic_pause);
            notifyDataSetChanged();

            holder.seekBar.setMax(mediaPlayer.getDuration());
            updateSeekBar = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        holder.seekBar.setProgress(currentPosition);
                        holder.durationText.setText(formatDuration(currentPosition));
                        handler.postDelayed(this, 100);
                    }
                }
            };
            handler.post(updateSeekBar);

            mediaPlayer.setOnCompletionListener(mp -> {
                holder.playButton.setImageResource(R.drawable.ic_play);
                holder.seekBar.setProgress(0);
                holder.durationText.setText(formatDuration(0));
                currentPosition = -1;
            });

            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDuration(int milliseconds) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - 
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    static class RecordingViewHolder extends RecyclerView.ViewHolder {
        TextView recordingTitle;
        TextView recordingDate;
        ImageButton playButton;
        ImageButton deleteButton;
        SeekBar seekBar;
        TextView durationText;

        public RecordingViewHolder(@NonNull View itemView) {
            super(itemView);
            recordingTitle = itemView.findViewById(R.id.recordingTitle);
            recordingDate = itemView.findViewById(R.id.recordingDate);
            playButton = itemView.findViewById(R.id.playButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            seekBar = itemView.findViewById(R.id.seekBar);
            durationText = itemView.findViewById(R.id.durationText);
        }
    }
}