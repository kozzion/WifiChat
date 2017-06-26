package nl.everlutions.wifichat.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

import nl.everlutions.wifichat.handler.ArrayTranscoderShortShort;

import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_PLAY_LOCAL;
import static nl.everlutions.wifichat.services.ServiceMain.SERVICE_MESSAGE_TYPE_RECORD_LOCAL;

/**
 * Created by jaapo on 26-5-2017.
 */

public class ServiceAudioSample {
    private static final int SAMPLE_RATE = 44100;
    private static final int QUEUE_CAPACITY = 1000;
    private static final int RECORD_QUEUE_CAPACITY = 1000;
    private final BroadcastReceiver mBroadCastReceiver;
    private final ServiceMain mServiceMain;

    public boolean mIsPlaying;
    public boolean mIsRecording;

    private ArrayBlockingQueue<short[]> mPlayQueue;
    private ArrayBlockingQueue<short[]> mRecordQueue;

    public ArrayTranscoderShortShort mTranscoderPlay;

    public int mBufferSizePlay;
    public int mBufferSizeRecord;


    private final String TAG = this.getClass().getSimpleName();


    public ServiceAudioSample(ServiceMain serviceMain) {
        mServiceMain = serviceMain;
        mBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleServiceMessage(intent);
            }
        };

        LocalBroadcastManager.getInstance(serviceMain).registerReceiver((mBroadCastReceiver),
                new IntentFilter(ServiceMain.FILTER_TO_SERVICE_AUDIO)
        );

        mIsPlaying = false;
        mIsRecording = false;
        mPlayQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        mRecordQueue = new ArrayBlockingQueue<>(RECORD_QUEUE_CAPACITY);


        mBufferSizeRecord = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (mBufferSizeRecord == AudioRecord.ERROR || mBufferSizeRecord == AudioRecord.ERROR_BAD_VALUE) {
            mBufferSizeRecord = SAMPLE_RATE * 2;
        }

        mBufferSizePlay = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (mBufferSizePlay == AudioTrack.ERROR || mBufferSizePlay == AudioTrack.ERROR_BAD_VALUE) {
            mBufferSizePlay = SAMPLE_RATE * 2;
        }

    }

    private void handleServiceMessage(Intent intent) {
        String serviceMessageType = intent.getStringExtra(ServiceMain.SERVICE_MESSAGE_TYPE);
        Log.e(TAG, "handleServiceMessage: " + serviceMessageType);
        switch (serviceMessageType) {
            case SERVICE_MESSAGE_TYPE_RECORD_LOCAL:
                if (mIsRecording) {
                    recordAudioStop();
                } else {
                    recordAudioStart();
                }
                break;
            case SERVICE_MESSAGE_TYPE_PLAY_LOCAL:
                if (mIsPlaying) {
                    playAudioStop();
                } else {
                    mTranscoderPlay = new ArrayTranscoderShortShort(mRecordQueue, mBufferSizePlay, mPlayQueue);
                    mTranscoderPlay.transCodeStart();
                    playAudioStart();
                }
                break;
            default:
                Log.e(TAG, "service message NOT handled");
        }
    }


    public void recordAudioStop() {
        mIsRecording = false;
    }


    public void recordAudioStart() {
        if (!mIsRecording) {
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
                        Log.e(TAG, "Audio Record can't initialize!");
                        return;
                    }

                    Log.e(TAG, "Start recording");
                    Log.e(TAG, "Buffers: " + mBufferSizePlay + " " + mBufferSizeRecord);
                    record.startRecording();
                    while (mIsRecording) {

                        int toWriteCount = record.read(audioRecordBuffer, 0, audioRecordBuffer.length);
                        short[] audioArray = new short[toWriteCount];
                        System.arraycopy(audioRecordBuffer, 0, audioArray, 0, toWriteCount);
                        mRecordQueue.offer(audioArray);
                        Log.e(TAG, "mRecordQueue: " + mRecordQueue.size());
                    }
                    record.stop();
                    record.release();
                }
            }).start();
        }
    }

    public void playAudioStop() {
        mIsPlaying = false;
    }

    public void playAudioStart() {
        if (!mIsPlaying) {
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

                    Log.e(TAG, "Audio streaming started");
                    Log.e(TAG, "mPlayQueue " + mPlayQueue.size());
                    while (mIsPlaying) {
                        try {
                            short[] buffer = mPlayQueue.take();
                            if (mBufferSizePlay != buffer.length) {
                                throw new RuntimeException("is: " + buffer.length + " should be " + mBufferSizePlay);
                            }
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


    public void onStop() {
        playAudioStop();
        recordAudioStop();
    }
}
