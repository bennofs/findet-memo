package tu.emi.findetmemo.classes;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class SingleAudioPlayer {
    private static final int UPDATE_INTERVAL_MS = 30;

    private final Handler handler;
    private final Context context;

    private MediaPlayer player;

    private AudioPlayerStateListener listener;
    private File file;

    private boolean seekInProgress = false;
    private boolean completed = false;

    public interface AudioPlayerStateListener {
        boolean handlesFile(File file);

        void onPositionChanged(int pos);

        void onCompleted();

        void onPaused();

        void onStart();
    }

    public SingleAudioPlayer(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    private void setListener(final File file, final AudioPlayerStateListener listener) {
        this.listener = listener;
        this.file = file;

        if (completed) {
            this.listener.onCompleted();
        } else if (!this.player.isPlaying()) {
            updateCurrentPosition();
            this.listener.onPaused();
        } else {
            this.listener.onStart();
            startUpdateLoop();
        }
    }


    public void rebindListenerForFile(final File file, final AudioPlayerStateListener listener) {
        if (this.file != file) {
            listener.onPaused();
            return;
        }

        setListener(file, listener);
    }

    private boolean hasListener() {
        if (listener == null) return false;

        if (!listener.handlesFile(this.file)) {
            listener = null;
            return false;
        }

        return true;
    }

    private void resetPlayer() {
        if (player == null) {
            player = new MediaPlayer();
        } else {
            player.reset();
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                completed = true;
                player.stop();
                if (hasListener()) listener.onCompleted();
            }
        });

        player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                seekInProgress = false;
                updateCurrentPosition();
            }
        });
    }


    public void play(final File file, final int offset, final AudioPlayerStateListener listener) {
        stop();

        if (player == null || this.file != file) {
            resetPlayer();
            try {
                player.setDataSource(context, Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
                Toast errMsg = Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
                errMsg.show();
                return;
            }
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                startAt(file, offset, listener);
            }
        });
        player.prepareAsync();
    }

    private void startAt(File file, int offset, AudioPlayerStateListener listener) {
        player.seekTo(offset);
        player.start();

        completed = false;
        setListener(file, listener);
    }

    public void stop() {
        if (player != null && player.isPlaying()) {
            player.stop();
            if (listener != null) listener.onPaused();
        }
        listener = null;
    }

    public void seek(int position) {
        if (player == null || !player.isPlaying()) return;
        seekInProgress = true;
        player.seekTo(position);
    }

    public void suspend() {
        if (player != null) player.release();
        player = null;
    }


    private void updateCurrentPosition() {
        if (player == null || !hasListener() || seekInProgress) return;

        listener.onPositionChanged(player.getCurrentPosition());
    }

    private void startUpdateLoop() {
        if (player == null || !hasListener() || completed) return;

        updateCurrentPosition();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startUpdateLoop();
            }
        }, UPDATE_INTERVAL_MS);
    }
}
