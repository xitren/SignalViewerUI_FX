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
    private double discretisation = 250;
    private double timePeriod = 0.004;

    public DataFXManager(int n, ExtendedDataLine[] edl) {
        super(n, edl);
    }

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
                XYChart.Data xyd = new XYChart.Data(gto[j] * getTimePeriod(), go[j]);
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
                glc.setMark(m.channel, new XYChart.Data(m.start * getTimePeriod(), m.finish * getTimePeriod()),
                        m.name, Color.valueOf(m.getWebColor()), Color.valueOf(m.getWebLabelColor()));
            } else {
                for (int i = 0;i < fullViewFX.length;i++) {
                    glc.setMark(i, new XYChart.Data(m.start * getTimePeriod(), m.finish * getTimePeriod()),
                            m.name, Color.valueOf(m.getWebColor()), Color.valueOf(m.getWebLabelColor()));
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
                fullViewFXUpdater[i][j].setXValue(gtl[j] * getTimePeriod());
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
                XYChart.Data xyd;
                if (getMode(i).equals(ExtendedDataLine.Mode.FOURIER))
                    xyd = new XYChart.Data(gtl[j], gdl[j]);
                else
                    xyd = new XYChart.Data(gtl[j] * getTimePeriod(), gdl[j]);
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
            if (getMode(i).equals(ExtendedDataLine.Mode.FOURIER)) {
                for (int j = 0; j < av; j++) {
                    customViewFXUpdater[i][j].setXValue(gtl[j]);
                    customViewFXUpdater[i][j].setYValue(gdl[j]);
                }
            } else {
                for (int j = 0; j < av; j++) {
                    customViewFXUpdater[i][j].setXValue(gtl[j] * getTimePeriod());
                    customViewFXUpdater[i][j].setYValue(gdl[j]);
                }
            }
            for (int j=av;j < gdl.length;j++) {
                customViewFX[i].getData().remove(customViewFXUpdater[i][j]);
            }
            if ((av) <= 0)
                av = 1;
            if (getMode(i).equals(ExtendedDataLine.Mode.FOURIER)) {
                glc.setRangeMax(i, gtl[0], gtl[av - 1]);
            } else {
                glc.setRangeMax(i, gtl[0] * getTimePeriod(), gtl[av - 1] * getTimePeriod());
            }
        }
    }

    protected void setView(int start, int end) {
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

    public void addMark(int ch, XYChart.Data<Number, Number> xy, String name,
                           String color, String label_color) {
        xy.setXValue(xy.getXValue().doubleValue() * getDiscretisation());
        xy.setYValue(xy.getYValue().doubleValue() * getDiscretisation());
        super.addMark(ch, xy.getXValue().intValue(), xy.getYValue().intValue(),
                name, color, label_color);
    }

    public void addGlobalMark(XYChart.Data<Number, Number> xy, String name,
                                 String color, String label_color) {
        xy.setXValue(xy.getXValue().doubleValue() * getDiscretisation());
        xy.setYValue(xy.getYValue().doubleValue() * getDiscretisation());
        super.addGlobalMark(xy.getXValue().intValue(), xy.getYValue().intValue(),
                name, color, label_color);
    }

    private void fixMarks(XYChart.Data<Number, Number> xy) {
        xy.setXValue(xy.getXValue().doubleValue() * getDiscretisation());
        xy.setYValue(xy.getYValue().doubleValue() * getDiscretisation());
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
        super.cut((int)(xy.getXValue().intValue() * getDiscretisation()),
                (int)((xy.getYValue().intValue() - xy.getXValue().intValue()) * getDiscretisation()));
        fixMarks(xy);
        unsetOverview();
        updateOverview();
        bindSeriesOverview(glcOverview);
        bindSeriesView(glcView);
        setMaxView();
    }

    protected void setMaxView() {
        for (int i=0;i < dataLines.size();i++) {
            dataLines.get(i).setMaxView();
        }
        bindSeriesViewUnder(glcView);
    }

    public void setView(XYChart.Data<Number, Number> xy) {
        XYChart.Data<Number, Number> xy2 = new XYChart.Data<>(xy.getXValue(), xy.getYValue());
        xy2.setXValue(xy2.getXValue().doubleValue() * getDiscretisation());
        xy2.setYValue(xy2.getYValue().doubleValue() * getDiscretisation());
        setViewP(xy2);
    }

    protected void setViewP(XYChart.Data<Number, Number> xy) {
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

    public double getDiscretisation() {
        return discretisation;
    }

    public void setDiscretisation(double discretisation) {
        this.timePeriod = 1 / discretisation;
        this.discretisation = discretisation;
    }

    public double getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(double timePeriod) {
        this.timePeriod = timePeriod;
        this.discretisation = 1 / timePeriod;
    }
}
