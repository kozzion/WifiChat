package nl.everlutions.wifichat.services;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jaapo on 31-5-2017.
 */

public class ServiceAudioCorrelatorPearson
{
    private final String TAG = this.getClass().getSimpleName();


    BlockingQueue<short[]> local_queue;
    BlockingQueue<short[]> remote_queue;

    boolean mIsRunning;

    public ServiceAudioCorrelatorPearson()
    {
        local_queue = new ArrayBlockingQueue<>(1000);
        remote_queue = new ArrayBlockingQueue<>(1000);

        mIsRunning = false;
    }

    public void Run()
    {
        mIsRunning = true;
        while(mIsRunning)
        {
            // Change offset options

            // Check correlations

        }
    }

    public void Test(short [] data, int offset, int  [] offset_array) {
        short [] unshifted = new short[data.length - offset];
        short [] shifted = new short[data.length - offset];
        for (int i = 0; i < data.length - offset; i++) {
            unshifted[i] = data[i];
            shifted[i] = data[i + offset];
        }
        for (int i = 0; i < offset_array.length; i++) {
            double corr = getCorrelation(unshifted, shifted, offset_array[i]);
            Log.e(TAG, "Correlation for " + offset_array[i] + " " +  corr);
        }


    }

    public double getCorrelation(int buffer_0, int buffer_1, int offset_1)
    {
        return 0;//getCorrelation(buffers[buffer_0],buffers[buffer_1],  offset_1);
    }

    public double getCorrelation(short  [] buffer_0, short [] buffer_1, int  [] offset_array)
    {
        return getCorrelation(buffer_0,buffer_1,  offset_array[0]);
    }

    public double getCorrelation(short  [] buffer_0, short [] buffer_1, int offset_1) {

        int [] sample_indexes = new int [0];
        double m_0 = 0;
        double m_1 = 0;

        for (int i = 0; i < sample_indexes.length; i++) {
            m_0 += buffer_0[sample_indexes[i]];
            m_1 += buffer_1[sample_indexes[i] + offset_1];
        }
        m_0 /= sample_indexes.length;
        m_1 /= sample_indexes.length;

        double e_01 = 0;
        double s_01 = 0;
        for (int i = 0; i < sample_indexes.length; i++) {
            e_01 +=  (buffer_0[sample_indexes[i]] - m_0) * (buffer_1[sample_indexes[i] + offset_1] - m_1);
            s_01 +=  sqr(buffer_0[sample_indexes[i]] - m_0) * sqr(buffer_1[sample_indexes[i] + offset_1] - m_1);
        }
        s_01 = Math.sqrt(s_01);
        return e_01 / s_01;
    }

    public double sqr(double input)
    {
        return input * input;
    }
}
