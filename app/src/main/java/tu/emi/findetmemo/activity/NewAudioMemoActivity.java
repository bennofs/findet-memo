package tu.emi.findetmemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import tu.emi.findetmemo.R;
import tu.emi.findetmemo.data.AudioMemo;
import tu.emi.findetmemo.data.Memo;

public class NewAudioMemoActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_REQUEST = 1;

    private MediaRecorder recorder;
    private UUID uuid;
    private Handler handler;

    private ArrayList<File> outputFiles;
    private long outputFilesDuration = 0;
    private long lastRecordStartTime = -1;
    private File lastRecordOutputFile;

    private FloatingActionButton fab;
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
        fab = (FloatingActionButton) findViewById(R.id.fab_audiomemo);
        outputFiles = new ArrayList<>();
        handler = new Handler();
        viewRecordingDuration = (TextView) findViewById(R.id.textview_audiomemo_recording_duration);
        viewTitle = (TextView) findViewById(R.id.edittext_audiomemo_title);

        requestMicPermission();
    }

    private void requestMicPermission() {
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
        if(recorder == null) recorder = new MediaRecorder();

        final File outputFileName = getFileStreamPath("recording_" + uuid.toString() + "_" + Integer.toString(outputFiles.size()));

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(outputFileName.toString());

        try {
            recorder.prepare();
            recorder.start();

            lastRecordStartTime = System.nanoTime();
            lastRecordOutputFile = outputFileName;
            updateRecordingInfo();

            fab.setImageResource(R.drawable.ic_pause_white_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopRecording();
                }
            });
        } catch (IOException e) {
            Toast t = Toast.makeText(this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
            t.show();
        }



    }

    private void updateRecordingInfo() {
        if(lastRecordStartTime < 0) return;

        final long oneNanoSec = 1_000_000_000;
        final long time = (outputFilesDuration + (System.nanoTime() - lastRecordStartTime)) / oneNanoSec;
        final long seconds = time % 60;
        final long minutes = (time / 60) % 60;
        final long hours = time / 3600;

        viewRecordingDuration.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateRecordingInfo();
            }
        }, 100);
    }

    private void stopRecording() {
        if(recorder == null || lastRecordStartTime < 0) return;


        long lastRecordStopTime = System.nanoTime();
        recorder.stop();
        outputFiles.add(lastRecordOutputFile);
        outputFilesDuration += (lastRecordStopTime - lastRecordStartTime);


        lastRecordOutputFile = null;
        lastRecordStartTime = -1;

        fab.setImageResource(R.drawable.ic_fiber_manual_record_white_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });
    }

    @Override
    protected void onPause() {
        if(recorder != null) {
            stopRecording();
            recorder.release();
            recorder = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(recorder != null) {
            stopRecording();
            recorder.release();
            recorder = null;
        }
        for(File file : outputFiles) {
            System.out.println("deleting file");
            if(!file.delete()) {
                System.out.println("warning: deleting failed");
            }
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finishSuccess();
    }

    private void finishSuccess() {
        stopRecording();

        final long oneMilliSec = 1_000_000;

        final String title = viewTitle.getText().toString();
        final File target = getFileStreamPath("recording_" + uuid.toString());
        try {
            Intent result = new Intent();
            mergeAudioFiles(outputFiles, target);
            AudioMemo memo = AudioMemo.create(title, target, (int)(outputFilesDuration / oneMilliSec));
            result.putExtra(Memo.EXTRA_MEMO, memo);
            setResult(RESULT_OK, result);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finishCancel();
        }
    }

    private void finishCancel() {
        this.setResult(RESULT_CANCELED);
        finish();
    }

    private static void mergeAudioFiles(List<File> files, File target) throws IOException {
        ArrayList<Track> tracks = new ArrayList<>();
        for(File file : files) {
            Movie movie = MovieCreator.build(file.toString());
            for (Track track : movie.getTracks()) {
                tracks.add(track);
            }
        }

        Movie outputMovie = new Movie();
        if(!tracks.isEmpty()) outputMovie.addTrack(new AppendTrack(tracks.toArray(new Track[tracks.size()])));
        Container container = new DefaultMp4Builder().build(outputMovie);
        FileChannel outputChannel = new RandomAccessFile(target, "rw").getChannel();
        container.writeContainer(outputChannel);
        outputChannel.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home: {
                finishSuccess();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
