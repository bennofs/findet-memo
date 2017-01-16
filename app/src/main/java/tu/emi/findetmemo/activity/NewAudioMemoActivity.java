package tu.emi.findetmemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import tu.emi.findetmemo.R;
import tu.emi.findetmemo.classes.PausableAudioRecorder;
import tu.emi.findetmemo.data.AudioMemo;
import tu.emi.findetmemo.data.Memo;

public class NewAudioMemoActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_REQUEST = 1;
    private static final int UPDATE_RATE = 4;

    private PausableAudioRecorder recorder;
    private UUID uuid;
    private Handler handler;

    private long finishedPartsDuration;
    private long partStartTime = -1;

    private FloatingActionButton viewFab;
    private TextView viewRecordingDuration;
    private TextView viewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_audio_memo);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uuid = UUID.randomUUID();
        handler = new Handler();
        recorder = new PausableAudioRecorder(this, MediaRecorder.AudioSource.MIC, "recording_" + uuid + ".wav");

        viewFab = (FloatingActionButton) findViewById(R.id.fab_audiomemo);
        viewRecordingDuration = (TextView) findViewById(R.id.textview_audiomemo_recording_duration);
        viewTitle = (TextView) findViewById(R.id.edittext_audiomemo_title);

        requestMicPermissionAndStart();
    }

    private void requestMicPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecording();
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RECORD_AUDIO_PERMISSION_REQUEST) return;

        if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
            finishCancel();

        startRecording();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startRecording() {
        try {
            recorder.startRecording();
            partStartTime = System.nanoTime();

            startUpdateLoop();
            viewFab.setImageResource(R.drawable.ic_pause_white_24dp);
            viewFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopRecording();
                }
            });
        } catch(IOException e) {
            finishWithException(e);
        }
    }

    private void startUpdateLoop() {
        if(!recorder.isRecording()) return;
        updateRecordingInfo();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startUpdateLoop();
            }
        }, 1000 / UPDATE_RATE);
    }

    private void updateRecordingInfo() {
        if (partStartTime < 0 || !recorder.isRecording()) return;
        displayRecordingDuration(finishedPartsDuration + (System.nanoTime() - partStartTime));
    }

    private void displayRecordingDuration(final long durationInNanosecs) {
        final long oneNanoSec = 1_000_000_000;
        final long time = durationInNanosecs / oneNanoSec;
        final long seconds = time % 60;
        final long minutes = (time / 60) % 60;
        final long hours = time / 3600;
        viewRecordingDuration.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void stopRecording() {
        if(!recorder.isRecording()) return;

        try {
            recorder.pauseRecording();
            long partStopTime = System.nanoTime();

            finishedPartsDuration += (partStopTime - partStartTime);
            partStartTime = -1;

            viewFab.setImageResource(R.drawable.ic_fiber_manual_record_white_24dp);
            viewFab.setContentDescription(getResources().getString(R.string.record));
            viewFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startRecording();
                }
            });
        } catch(IOException e) {
            finishWithException(e);
        }
    }

    @Override
    protected void onPause() {
        try {
            recorder.suspend();
            super.onPause();
        } catch(IOException e) {
            finishWithException(e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            recorder.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finishSuccess();
    }

    private void finishSuccess() {
        final long oneMilliSec = 1_000_000;

        stopRecording();
        try {
            recorder.close();


            final String title = viewTitle.getText().toString();
            final File target = getFileStreamPath("recording_" + uuid.toString() + ".wav");
            final AudioMemo memo = AudioMemo.create(title, target, (int) (finishedPartsDuration / oneMilliSec));

            final Intent result = new Intent();
            result.putExtra(Memo.EXTRA_MEMO, memo);
            setResult(RESULT_OK, result);
            finish();
        } catch(IOException e) {
            finishWithException(e);
        }
    }

    private void finishCancel() {
        this.setResult(RESULT_CANCELED);
        finish();
    }

    private void finishWithException(Exception e) {
        e.printStackTrace();
        Toast errMsg = Toast.makeText(this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
        errMsg.show();
        finishCancel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finishSuccess();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
