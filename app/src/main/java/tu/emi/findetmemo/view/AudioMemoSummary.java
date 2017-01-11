package tu.emi.findetmemo.view;

import android.view.View;
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
        private enum ButtonAction { Replay, Play, Pause }

        private final TextView viewTitle;
        private final TextView viewDate;
        private final ImageButton viewPlay;
        private final SeekBar viewSeek;

        private ButtonAction nextAction = ButtonAction.Play;

        public AudioMemo memo;


        public ViewHolder(View root) {
            super(root);


            viewTitle = (TextView) findViewById(R.id.textview_memosummary_title);
            viewDate = (TextView) findViewById(R.id.textview_memosummary_date);
            viewPlay = (ImageButton) findViewById(R.id.button_memosummary_play);
            viewSeek = (SeekBar) findViewById(R.id.seekbar_memosummary);
        }

        @Override
        public void bind(final Object data, final MainActivity parent) {
            memo = (AudioMemo)data;
            viewTitle.setText(memo.common.title);
            viewTitle.setVisibility(memo.common.title.isEmpty() ? View.GONE : View.VISIBLE);

            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(itemView.getContext().getApplicationContext());
            viewDate.setText(dateFormat.format(memo.common.creationDate));

            final SingleAudioPlayer player = parent.getSingleAudioPlayer();

            viewSeek.setMax(memo.duration);
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
                    switch(nextAction) {
                        case Play: play(viewSeek.getProgress()); break;
                        case Replay: play(0); break;
                        case Pause: player.stop(); break;
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
    }

    public AudioMemoSummary() {
        super(R.layout.layout_audio_memo_summary);
    }

    @Override
    public BaseViewHolder createViewHolder(View root) {
        return new ViewHolder(root);
    }
}
