package io.github.xitren.fx.data;

import io.github.xitren.data.NMath;

public class Trigger {
    protected int defaultDelayInSamples;

    /**
     * The length of the trigger (number of coefficients).
     */
    private int m_nLength;

    /**
     * The level of the trigger .
     */
    private double m_Level;

    /**
     * The buffer for past input values.
     * This stores the input values needed for convolution.
     * The buffer is used as a circular buffer.
     */
    private double[] m_afBuffer;

    /**
     * The index into m_afBuffer.
     * Since m_afBuffer is used as a circular buffer,
     * a buffer pointer is needed.
     */
    private int m_nBufferIndex;

    private Runnable m_Task;
    private boolean m_Trigger;

    /**
     * Init a FIR filter with coefficients.
     *
     * @param nLength The array of filter length.
     */
    public Trigger(int nLength, double Level, Runnable task) {
        initialize(nLength, Level, task);
    }

    private void initialize(int nLength, double Level, Runnable task) {
        m_nLength = nLength;
        m_Level = Level;
        m_Task = task;
        m_afBuffer = new double[m_nLength];
        m_nBufferIndex = 0;
        defaultDelayInSamples = nLength / 2;
        m_Trigger = false;
    }

    /**
     * Process an input sample and calculate an output sample.
     * Call this method to use the filter.
     *
     * @param fInput
     * @return
     */
    public void process(double fInput) {
        m_nBufferIndex = (m_nBufferIndex + 1) % m_nLength;
        m_afBuffer[m_nBufferIndex] = fInput;
        double max = NMath.max(m_afBuffer);
        boolean nnm_Trigger = (fInput / max) > m_Level;
        if (!m_Trigger && nnm_Trigger)
            m_Task.run();
        m_Trigger = nnm_Trigger;
    }
}
