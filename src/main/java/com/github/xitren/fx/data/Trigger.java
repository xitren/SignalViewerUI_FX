package com.github.xitren.fx.data;

import com.github.xitren.data.NMath;

public class Trigger {
    protected int defaultDelayInSamples;

    private int m_nLength;

    private double m_Level;

    private double[] m_afBuffer;

    private int m_nBufferIndex;

    private Runnable m_Task;
    private boolean m_Trigger;

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
