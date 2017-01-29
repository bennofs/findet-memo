package tu.emi.findetmemo.view;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;

import tu.emi.findetmemo.R;
import tu.emi.findetmemo.activity.MainActivity;
import tu.emi.findetmemo.classes.SingleAudioPlayer;
import tu.emi.findetmemo.data.AudioMemo;

public class AudioMemoSummary extends ViewTemplate {
    private static class ViewHolder extends BaseViewHolder implements SingleAudioPlayer.AudioPlayerStateListener {
        private enum ButtonAction {Replay, Play, Pause}

        public final MainActivity parent;

        private final TextView viewTitle;
        private final TextView viewDate;
        private final Button viewShare;
        private final ImageButton viewPlay;
        private final SeekBar viewSeek;

        private ButtonAction nextAction = ButtonAction.Play;
        private SingleAudioPlayer player;

        private AudioMemo memo;


        public ViewHolder(View root, MainActivity parent) {
            super(root);
            this.parent = parent;

            viewTitle = (TextView) findViewById(R.id.textview_memosummary_title);
            viewDate = (TextView) findViewById(R.id.textview_memosummary_date);
            viewShare = (Button) findViewById(R.id.button_memosummary_share);
            viewPlay = (ImageButton) findViewById(R.id.button_memosummary_play);
            viewSeek = (SeekBar) findViewById(R.id.seekbar_memosummary);
        }

        @Override
        public void bind(final Object data) {
            memo = (AudioMemo) data;
            viewTitle.setText(memo.common.title);
            viewTitle.setVisibility(memo.common.title.isEmpty() ? View.GONE : View.VISIBLE);

            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(itemView.getContext().getApplicationContext());
            viewDate.setText(dateFormat.format(memo.common.creationDate));

            viewShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri contentUri = FileProvider.getUriForFile(parent, "tu.emi.findetmemo.recordings", memo.audioFile);

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.setType("audio/x-wav");
                    parent.startActivity(shareIntent);
                }
            });

            this.player = parent.getSingleAudioPlayer();

            viewSeek.setMax(memo.duration);
            viewSeek.setProgress(0);
            viewSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) return;
                    player.seek(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            viewPlay.setOnClickListener(new View.OnClickListener() {
                private void play(int position) {
                    player.play(memo.audioFile, position, ViewHolder.this);
                }

                @Override
                public void onClick(View v) {
                    switch (nextAction) {
                        case Play:
                            play(viewSeek.getProgress());
                            break;
                        case Replay:
                            play(0);
                            break;
                        case Pause:
                            player.stop();
                            break;
                    }
                }
            });
            player.rebindListenerForFile(memo.audioFile, this);
        }

        @Override
        public boolean handlesFile(File file) {
            return memo.audioFile == file;
        }

        @Override
        public void onPositionChanged(int pos) {
            viewSeek.setProgress(pos);
        }

        @Override
        public void onStart() {
            viewPlay.setImageResource(R.drawable.ic_pause_black_24dp);
            nextAction = ButtonAction.Pause;
        }

        @Override
        public void onPaused() {
            viewPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            nextAction = ButtonAction.Play;
        }

        @Override
        public void onCompleted() {
            viewPlay.setImageResource(R.drawable.ic_replay_black_24dp);
            viewSeek.setProgress(memo.duration);
            nextAction = ButtonAction.Replay;
        }

        @Override
        public void destroy() {
            if(player != null && nextAction == ButtonAction.Pause) {
                player.stop();
            }
        }
    }

    public AudioMemoSummary() {
        super(R.layout.layout_audio_memo_summary);
    }

    @Override
    public BaseViewHolder createViewHolder(View root, MainActivity parent) {
        return new ViewHolder(root, parent);
    }
}
