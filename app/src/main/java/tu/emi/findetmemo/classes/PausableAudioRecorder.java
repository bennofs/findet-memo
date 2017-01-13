package tu.emi.findetmemo.classes;


import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PausableAudioRecorder {
    // This is the number of frames (audio samples) that we capture per second.
    // According to the Android Developer Documentation, 44100 should be supported by all devices.
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final int MIN_BUF_SIZE
            = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final int FILE_BUF_SIZE = 1 << 12;
    private static final int BUF_SIZE = Math.max(FILE_BUF_SIZE, MIN_BUF_SIZE);

    private final Context context;
    private final int audioSource;
    private final String fileName;
    private final ByteBuffer buffer;

    private AudioRecord recorder;
    private FileOutputStream output;
    private Thread pollThread;

    public PausableAudioRecorder(Context context, int audioSource, String fileName) {
        this.context = context;
        this.audioSource = audioSource;
        this.fileName = fileName;
        this.buffer = ByteBuffer.allocateDirect(BUF_SIZE * 2);
    }

    public void startRecording() throws IOException {
        initializeOutput();
        initializeRecorder();

        if(recorder.getState() == AudioRecord.RECORDSTATE_RECORDING) return;

        pollThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(pollAudio()) {}
            }
        });

        recorder.startRecording();
        pollThread.start();
    }

    private boolean pollAudio() {
        int bytesRead = recorder.read(buffer, BUF_SIZE);
        if(bytesRead == 0) return false;

        try {
            output.write(buffer.array(), 0, bytesRead);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void pauseRecording() throws IOException {
        if(!isRecording()) return;

        recorder.stop();
        try {
            pollThread.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        updateSizeFields();
    }

    public boolean isRecording() {
        return recorder != null && recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    public void suspend() throws IOException {
        if(recorder != null) {
            pauseRecording();
            recorder.release();
        }
    }

    public void close() throws IOException {
        suspend();
        output.close();
    }

    private void initializeOutput() throws IOException {
        if(output != null) return;

        output = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        writeWaveHeader();
    }

    private void initializeRecorder() {
        if(recorder != null) return;

        recorder = new AudioRecord(audioSource, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUF_SIZE);
    }

    private void writeWaveHeader() throws IOException {
        final byte[] sampleRate = intToBytes(SAMPLE_RATE);
        final byte[] bytesPerSec = intToBytes(2 * SAMPLE_RATE);

        final byte[] WAVE_HEADER = {
                // RIFF HEADER (12 bytes)
                (byte) 'R', (byte) 'I', (byte) 'F', (byte) 'F',                 // chunk id (RIFF)
                (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,             // file size - 8 (filled in later)
                (byte) 'W', (byte) 'A', (byte) 'V', (byte) 'E',                 // riff type (WAVE)

                // FORMAT CHUNK (24 bytes)
                (byte) 'f', (byte) 'm', (byte) 't', (byte) ' ',                 // chunk id (fmt )
                (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00,             // chunk size - 8
                (byte) 0x01, (byte) 0x00,                                       // format tag (PCM)
                (byte) 0x01, (byte) 0x00,                                       // channels (1, MONO)
                sampleRate[0], sampleRate[1], sampleRate[2], sampleRate[3],     // sample rate
                bytesPerSec[0], bytesPerSec[1], bytesPerSec[2], bytesPerSec[3], // bytes/second
                (byte) 0x02, (byte) 0x00,                                       // frame size
                (byte) 0x10, (byte) 0x00,                                       // bits per sample per channel

                // DATA CHUNK HEADER (8 bytes)
                (byte) 'd', (byte) 'a', (byte) 't', (byte) 'a',                 // chunk id (data)
                (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,             // chunk size - 8 (filled in later)
                // Here follows the PCM encoded data, which we will write to the file
                // while recording
        };
        output.write(WAVE_HEADER);
    }

    private void updateSizeFields() throws IOException {
        final int FILE_SIZE_OFFSET = 4;
        final int DATA_SIZE_OFFSET = 12 /* HEADER */ + 24 /* FORMAT CHUNK */ + 4 /* DATA CHUNK ID */;

        output.flush();
        output.getChannel().write(
                ByteBuffer.wrap(intToBytes((int) output.getChannel().size() - FILE_SIZE_OFFSET - 4)),
                FILE_SIZE_OFFSET);
        output.getChannel().write(
                ByteBuffer.wrap(intToBytes((int) output.getChannel().size() - DATA_SIZE_OFFSET - 4)),
                DATA_SIZE_OFFSET);
    }

    private static byte[] intToBytes(int v) {
        return new byte[]{
                (byte) (v),
                (byte) (v >> 8),
                (byte) (v >> 16),
                (byte) (v >> 24)
        };
    }
}
