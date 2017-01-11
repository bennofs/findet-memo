package tu.emi.findetmemo.data;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import tu.emi.findetmemo.view.AudioMemoSummary;
import tu.emi.findetmemo.view.ViewTemplate;

public class AudioMemo extends Memo {
    public final File audioFile;
    public final int duration;

    private AudioMemo(UUID uuid, Common common, File audioFile, int duration) {
        super(uuid, common);
        this.audioFile = audioFile;
        this.duration = duration;
    }

    public static AudioMemo create(String title, File audioFile, int duration) {
        final Date now = new Date();
        final Common common = new Common(title, now, now);
        return new AudioMemo(UUID.randomUUID(), common, audioFile, duration);
    }

    @Override
    public Memo withCommon(Common newCommon) {
        return new AudioMemo(uuid, newCommon, audioFile, duration);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ViewTemplate summaryViewTemplate() { return new AudioMemoSummary(); }
}
