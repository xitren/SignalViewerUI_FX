package com.gusev.fx.data;

import com.gusev.data.DataContainer;
import com.gusev.data.ExtendedDataLine;
import com.gusev.data.window.op.WindowDynamicParser;
import com.gusev.fx.signal_ui.GroupLineChart;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.gusev.data.DataLine.OVERVIEW_SIZE;

public class DynamicDataFXManager<T extends DataContainer> extends DataFXManager<T> {
    private boolean online = true;
    private boolean pause = false;
    private boolean updateOnOverview = false;
    private boolean updateOnView = false;

    public DynamicDataFXManager(int n, ExtendedDataLine[] edl) {
        super(n, edl);
    }

    public DynamicDataFXManager(int n) {
        super(n);
    }

    public DynamicDataFXManager(String filename) throws IOException {
        super(filename);
    }

    public void addParser(WindowDynamicParser wdp, int channel) {
        this.dataLines.get(channel).addParser(wdp);
    }

    public void removeParser(WindowDynamicParser wdp, int channel) {
        this.dataLines.get(channel).removeParser(wdp);
    }

    public void addData(double[][] data) {
        if (!pause)
            super.addData(data);
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public void pause() {
        this.pause = true;
        this.online = false;
    }

    @Override
    public void start() {
        this.pause = false;
        this.online = true;
    }

    @Override
    protected void bindSeriesOverviewUnder(GroupLineChart glc) {
        if (glc == null)
            return;
        glcOverview = glc;
        fullViewFX = glc.getSeries();
        if (!updateOnOverview) {
            updateOnOverview = true;
            Platform.runLater(() -> {
                for (int i = 0; i < getSwapper().length && i < fullViewFXUpdater.length; i++) {
                    double[] gtl = getTimeOverview(i);
                    double[] gdl = getOverview(i);
                    final int st_i = i;
                    if (st_i < fullViewFXUpdater.length) {
                        for (int j = 0; j < gtl.length; j++) {
                            fullViewFXUpdater[st_i][j].setXValue(gtl[j] * getTimePeriod());
                            fullViewFXUpdater[st_i][j].setYValue(gdl[j]);
                        }
                        glc.setRangeMax(st_i, gtl[0] * getTimePeriod(), gtl[gtl.length - 1] * getTimePeriod());
                    }
                }
                updateOnOverview = false;
            });
        }
        if (fullViewFX.length > 0) {
            if (online) {
                setTailView();
            }
        }
    }

    private boolean runnerUnder = false;

    @Override
    protected void bindSeriesViewUnder(GroupLineChart glc) {
        if (glc == null)
            return;
        glcView = glc;
        customViewFX = glc.getSeries();
        if (!updateOnView) {
            updateOnView = true;
            Platform.runLater(() -> {
                for (int i = 0; i < customViewFX.length; i++) {
                    double[] gtl = getTimeLine(i, modes[i]);
                    double[] gdl = getDataLine(i, modes[i]);
                    final int av = getActiveView(i, modes[i]);
                    final int st_i = i;
                    if (customViewFX[st_i].getData().size() != av) {
                        for (int j = customViewFX[st_i].getData().size(); j < av; j++) {
                            customViewFX[st_i].getData().add(customViewFXUpdater[st_i][j]);
                        }
                    }
                    if (modes[st_i].equals(ExtendedDataLine.Mode.FOURIER)) {
                        for (int j = 0; j < av; j++) {
                            customViewFXUpdater[st_i][j].setXValue(gtl[j]);
                            customViewFXUpdater[st_i][j].setYValue(gdl[j]);
                        }
                    } else {
                        for (int j = 0; j < av; j++) {
                            customViewFXUpdater[st_i][j].setXValue(gtl[j] * getTimePeriod());
                            customViewFXUpdater[st_i][j].setYValue(gdl[j]);
                        }
                    }
                    for (int j = av; j < gdl.length; j++) {
                        customViewFX[st_i].getData().remove(customViewFXUpdater[st_i][j]);
                    }
                    //                if ((av) <= 0)
                    //                    av = 1;
                    if (modes[st_i].equals(ExtendedDataLine.Mode.FOURIER)) {
                        glc.setRangeMax(st_i, gtl[0], gtl[av - 1]);
                    } else {
                        glc.setRangeMax(st_i, gtl[0] * getTimePeriod(), gtl[av - 1] * getTimePeriod());
                    }
                }
                updateOnView = false;
            });
        }
    }
}