package com.gusev.fx.data;

import com.gusev.data.*;
import com.gusev.fx.signal_ui.GroupLineChart;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class DataFXManager<T extends DataContainer> extends DataManager<T> {
    private final static int VIEW_SIZE = 1024;

    protected GroupLineChart glcOverview;
    protected GroupLineChart glcView;
    protected XYChart.Series<Number, Number>[] fullViewFX;
    protected XYChart.Data<Number, Number>[][] fullViewFXUpdater;
    protected XYChart.Series<Number, Number>[] customViewFX;
    private XYChart.Data<Number, Number>[][] customViewFXUpdater;

    public DataFXManager(int n) {
        super(n);
    }

    public DataFXManager(double[] ... data) {
        super(data);
        unbind();
    }

    public DataFXManager(String filename) throws IOException {
        super(filename);
        unbind();
    }

    @Override
    public void setFilterGlobal(double[] data) {
        super.setFilterGlobal(data);
        bindSeriesViewUnder(glcView);
    }

    public void unbind() {
        fullViewFX = new XYChart.Series[0];
        customViewFX = new XYChart.Series[0];
        glcOverview = null;
        glcView = null;
    }

    public void bindSeriesOverview(GroupLineChart glc) {
        glcOverview = glc;
        fullViewFX = glc.getSeries();
        boolean set_range = false;
        fullViewFXUpdater = new XYChart.Data[fullViewFX.length][DataLine.OVERVIEW_SIZE];
        for (int i = 0; i < fullViewFX.length; i++) {
            fullViewFX[i].getData().clear();
            double[] gto = getTimeOverview(i);
            double[] go = getOverview(i);
            for (int j=0;j < getOverview(i).length;j++) {
                XYChart.Data xyd = new XYChart.Data(gto[j], go[j]);
                fullViewFXUpdater[i][j] = xyd;
                fullViewFX[i].getData().add(xyd);
            }
            if (i == (fullViewFX.length - 1)) {
                glc.setRangeMax(gto[0], gto[gto.length - 1]);
            }
        }
        resetMarks(glc);
    }

    @Override
    public void clearMarks() {
        super.clearMarks();
        if (glcView != null)
            resetMarks(glcView);
        if (glcOverview != null)
            resetMarks(glcOverview);
    }

    public void resetMarks(GroupLineChart glc) {
        glc.clearMarks();
        for (Mark m : this.marks) {
            if ( (0 <= m.channel) && (m.channel < this.dataLines.size()) ) {
                glc.setMark(m.channel, new XYChart.Data(m.start, m.finish), m.name,
                        Color.valueOf(m.getWebColor()));
            } else {
                for (int i = 0;i < fullViewFX.length;i++) {
                    glc.setMark(i, new XYChart.Data(m.start, m.finish), m.name,
                            Color.valueOf(m.getWebColor()));
                }
            }
        }
    }

    protected void bindSeriesOverviewUnder(GroupLineChart glc) {
        glcOverview = glc;
        fullViewFX = glc.getSeries();
        for (int i = 0; i < customViewFX.length; i++) {
            double[] gtl = getTimeOverview(i);
            double[] gdl = getOverview(i);
            for (int j=0;j < gtl.length;j++) {
                fullViewFXUpdater[i][j].setXValue(gtl[j]);
                fullViewFXUpdater[i][j].setYValue(gdl[j]);
            }
            glc.setRangeMax(i, gtl[0], gtl[gtl.length - 1]);
        }
    }

    public void bindSeriesView(GroupLineChart glc) {
        glcView = glc;
        customViewFX = glc.getSeries();
        customViewFXUpdater = new XYChart.Data[customViewFX.length][DataLine.OVERVIEW_SIZE];
        for (int i = 0; i < customViewFX.length; i++) {
            customViewFX[i].getData().clear();
            double[] gtl = getTimeLine(i);
            double[] gdl = getDataLine(i);
            for (int j=0;j < gtl.length;j++) {
                XYChart.Data xyd = new XYChart.Data(gtl[j], gdl[j]);
                customViewFXUpdater[i][j] = xyd;
                customViewFX[i].getData().add(xyd);
            }
            if (i == (customViewFX.length - 1)) {
                glc.setRangeMax(gtl[0], gtl[getActiveView(i) - 1]);
            }
        }
        resetMarks(glc);
    }

    protected void bindSeriesViewUnder(GroupLineChart glc) {
        glcView = glc;
        customViewFX = glc.getSeries();
        for (int i = 0; i < customViewFX.length; i++) {
            double[] gtl = getTimeLine(i);
            double[] gdl = getDataLine(i);
            int av = getActiveView(i);
            if (customViewFX[i].getData().size() != av) {
                for (int j=customViewFX[i].getData().size();j < av;j++) {
                    customViewFX[i].getData().add(customViewFXUpdater[i][j]);
                }
            }
            for (int j=0;j < av;j++) {
                customViewFXUpdater[i][j].setXValue(gtl[j]);
                customViewFXUpdater[i][j].setYValue(gdl[j]);
            }
            for (int j=av;j < gdl.length;j++) {
                customViewFX[i].getData().remove(customViewFXUpdater[i][j]);
            }
            if ((av) <= 0)
                av = 1;
            glc.setRangeMax(i, gtl[0], gtl[av - 1]);
        }
    }

    public void setView(int start, int end) {
        for (int i=0;i < dataLines.size();i++) {
            dataLines.get(i).setView(start, end);
        }
        bindSeriesViewUnder(glcView);
    }

    public boolean isDynamic() {
        return false;
    }

    public void pause() {
    }

    public void start() {
    }

    private void fixMarks(XYChart.Data<Number, Number> xy) {
        int move = xy.getXValue().intValue();
        List<Mark> ll = new LinkedList<>();
        for (Mark m : this.marks) {
            m.start -= move;
            m.finish -= move;
            if (m.finish < 0)
                ll.add(m);
            else if (m.start < 0)
                m.start = 0;
        }
    }

    public void cut(XYChart.Data<Number, Number> xy) {
        super.cut(xy.getXValue().intValue(),
                xy.getYValue().intValue() - xy.getXValue().intValue());
        fixMarks(xy);
        updateOverview();
        bindSeriesOverview(glcOverview);
        bindSeriesView(glcView);
        xy.setYValue(xy.getYValue().intValue() - xy.getXValue().intValue());
        xy.setXValue(0);
        setView(xy);
    }

    public void setView(XYChart.Data<Number, Number> xy) {
        for (int i=0;i < dataLines.size();i++) {
            dataLines.get(i).setView(xy.getXValue().intValue(),
                    xy.getYValue().intValue());
        }
        bindSeriesViewUnder(glcView);
    }

    public ObservableList<XYChart.Data<Number, Number>> getVisualPoints(int i) {
        return fullViewFX[i].getData();
    }

    public void setMode(int i, ExtendedDataLine.Mode def) {
        dataLines.get(i).setMode(def);
    }

    public List<Mark> getMarks() {
        return this.marks;
    }
}
