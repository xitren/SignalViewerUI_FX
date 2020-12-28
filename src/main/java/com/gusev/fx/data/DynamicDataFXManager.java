package com.gusev.fx.data;

import com.gusev.data.DataContainer;
import com.gusev.data.window.op.WindowDynamicParser;
import com.gusev.fx.signal_ui.GroupLineChart;
import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

import static com.gusev.data.DataLine.OVERVIEW_SIZE;

public class DynamicDataFXManager<T extends DataContainer> extends DataFXManager<T> {
    private Timer timer = new Timer();
    private boolean online = true;
    private boolean pause = false;

    private TimerTask task_data = new TimerTask() {
        public void run() {
            if (glcView != null) {
                Platform.runLater(()->{
                    bindSeriesOverviewUnder(glcOverview);
                });
            }
        }
    };

    public DynamicDataFXManager(int n) {
        super(n);
        timer.schedule(task_data, 2000, 1000);
    }

    public void addParser(WindowDynamicParser wdp, int channel) {
        this.dataLines.get(channel).addParser(wdp);
    }

    public void removeParser(WindowDynamicParser wdp, int channel) {
        this.dataLines.get(channel).removeParser(wdp);
    }

    public void addData(double[][] data) {
        if (!pause)
            for (int i=0;i < dataLines.size();i++) {
                dataLines.get(i).add(data[i]);
            }
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
        glcOverview = glc;
        fullViewFX = glc.getSeries();
        for (int i = 0; i < fullViewFX.length; i++) {
            double[] gtl = getTimeOverview(i);
            double[] gdl = getOverview(i);
            for (int j=0;j < gtl.length;j++) {
                fullViewFXUpdater[i][j].setXValue(gtl[j]);
                fullViewFXUpdater[i][j].setYValue(gdl[j]);
            }
            glc.setRangeMax(i, gtl[0], gtl[gtl.length - 1]);
        }
        if (online && (fullViewFX.length > 0)) {
            int end = getDataContainerSize(0);
            int start = end - OVERVIEW_SIZE - 1;
            if (start < 0)
                start = 0;
            setView(start, end);
        }
    }
}
