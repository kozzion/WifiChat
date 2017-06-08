package nl.everlutions.wifichat.services;

import static android.R.attr.offset;

/**
 * Created by jaapo on 31-5-2017.
 */

public class CorrelatorPearson
{
    short [][] buffers;
    short [][] write_indexes;

    public CorrelatorPearson(int buffer_count, int buffer_size)
    {

    }

    public double getCorrelation(int buffer_0, int buffer_1, int offset_1)
    {
        return getCorrelation(buffers[buffer_0],buffers[buffer_1],  offset_1);
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
