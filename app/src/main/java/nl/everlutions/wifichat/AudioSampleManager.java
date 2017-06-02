package nl.everlutions.wifichat;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jaapo on 26-5-2017.
 */

public class AudioSampleManager implements IMessageHandler
{

    private static final int SAMPLE_RATE = 44100;
    private static final int QUEUE_CAPACITY = 1000;

    public boolean mIsPlaying;
    public boolean mIsRecording;

    private BlockingQueue<short []> mPlayQueue;

    public int mBufferSizePlay;
    public int mBufferSizeRecord;

    public ArrayTranscoderShortShort mTranscoderPlay;
    public ArrayTranscoderByteShort mTranscoderPlayBytes;


    public MainActivity mMainActivity;


    public AudioSampleManager(MainActivity mainActivity)
    {
        mMainActivity = mainActivity;
        mIsPlaying = false;
        mIsRecording = false;
        mPlayQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);


        mBufferSizeRecord = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (mBufferSizeRecord == AudioRecord.ERROR || mBufferSizeRecord == AudioRecord.ERROR_BAD_VALUE) {
            mBufferSizeRecord = SAMPLE_RATE * 2;
        }

        mBufferSizePlay = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (mBufferSizePlay == AudioTrack.ERROR || mBufferSizePlay == AudioTrack.ERROR_BAD_VALUE) {
            mBufferSizePlay = SAMPLE_RATE * 2;
        }

        mTranscoderPlay = new ArrayTranscoderShortShort(mBufferSizePlay, this);
        mTranscoderPlayBytes = new ArrayTranscoderByteShort(mBufferSizePlay, mTranscoderPlay);
    }


    void recordAudioStop() {
        mIsRecording = false;
    }


    void recordAudioStart()
    {
        if (!mIsRecording)
        {
            mIsRecording = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                    short[] audioRecordBuffer = new short[mBufferSizeRecord / 2];

                    AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            mBufferSizeRecord);

                    if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                        Log.e("AudioSampleManager", "Audio Record can't initialize!");
                        return;
                    }


                    Log.e("AudioSampleManager", "Start recording");
                    Log.e("AudioSampleManager", "Buffers: " + mBufferSizePlay + " " + mBufferSizeRecord);
                    record.startRecording();
                    while (mIsRecording) {

                        int toWriteCount = record.read(audioRecordBuffer, 0, audioRecordBuffer.length);
                        //mTranscoderPlay.transCode(audioRecordBuffer, toWriteCount);

                        mMainActivity.mCommunicationManagerNDS.queueRecording(audioRecordBuffer, toWriteCount);

                    }
                    record.stop();
                    record.release();
                }
            }).start();
        }
    }

    void playAudioStop() {
        mIsPlaying = false;
    }

    public void playAudioStart() {
        if(!mIsPlaying) {
            mIsPlaying = true;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                    AudioTrack playTrack = new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            mBufferSizePlay,
                            AudioTrack.MODE_STREAM);

                    playTrack.play();

                    Log.e("Blieb","Audio streaming started");
                    Log.e("Blieb","mPlayQueue " + mPlayQueue.size());
                    while (mIsPlaying)
                    {
                        try {
                            short []  buffer = mPlayQueue.take();
                            playTrack.write(buffer, 0, buffer.length);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                    playTrack.stop();
                    playTrack.release();

                    // Log.v(LOG_TAG, "Audio streaming finished. Samples written: " + totalWritten);
                }

            }).start();
        }
    }

    @Override
    public void handleMessage(short[] samples) {
        if (mBufferSizePlay != samples.length)
        {
            throw new RuntimeException("is: "  + samples.length + " should be "  + mBufferSizePlay);
        }
        else
        {
            mPlayQueue.offer(samples);
            Log.e("Blieb","mPlayQueue " + mPlayQueue.size());
            Log.e("Blieb","mPlayQueue " + samples.length);
        }
    }

    @Override
    public void handleMessage(byte[] messageBytes) {

    }

    public void onStop() {
        playAudioStop();
        recordAudioStop();
    }
}
