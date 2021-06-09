package com.github.xitren.fx.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xitren.data.DataModelJson;
import com.github.xitren.data.Mark;
import com.github.xitren.data.container.DataContainer;
import com.github.xitren.data.container.DynamicDataContainer;
import com.github.xitren.data.container.StaticDataContainer;
import com.github.xitren.data.line.DataLineMode;
import com.github.xitren.data.line.OnlineDataLine;
import com.github.xitren.data.manager.DataManagerAction;
import com.github.xitren.data.manager.DataManagerMapper;
import com.github.xitren.data.window.WindowDynamicParser;
import com.github.xitren.fx.signal_ui.chart.GroupLineChart;
import com.github.xitren.fx.signal_ui.chart.ViewLineChart;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class DataFXManager<V extends OnlineDataLine<T>, T extends DataContainer>
        extends DataManagerMapper<V, T> implements InvalidationListener {
    protected GroupLineChart glcOverview;
    protected GroupLineChart glcView;
    protected double[][] values;
    protected double[][] time;
    protected int[] active;
    protected double[][] valuesOverview;
    protected double[][] timeOverview;
    protected ResourceBundle rb;
    protected int[][] swapperExt;

    public DataFXManager(ResourceBundle rb, V[] edl) {
        super(edl);
        this.rb = rb;
        if (8 < edl.length)
            swapperExt = new int[8][1];
        else
            swapperExt = new int[edl.length][1];
        for (int i = 0;i < swapperExt.length && i < 8;i++) {
            swapperExt[i][0] = i;
        }
        setConfiguration(swapperExt);
        glcOverview.dynamicProperty().set(true);
        glcView.dynamicProperty().set(true);
    }

    @Override
    public void pause(){
        super.pause();
        glcOverview.dynamicProperty().set(false);
        glcView.dynamicProperty().set(false);
    }

    @Override
    public void stop(){
        super.stop();
        glcOverview.dynamicProperty().set(false);
        glcView.dynamicProperty().set(false);
    }

    @Override
    public void start(){
        super.start();
        glcOverview.dynamicProperty().set(true);
        glcView.dynamicProperty().set(true);
    }

    public static DataFXManager<OnlineDataLine<StaticDataContainer>, StaticDataContainer> DataFXManagerFactory(
            ResourceBundle rb, String filename) throws IOException {
        InputStream inputStream = new FileInputStream(filename);
        Reader inputStreamReader = new InputStreamReader(inputStream);
        JsonParser ow = new ObjectMapper().reader().createParser(inputStream);
        DataModelJson data = ow.readValueAs(DataModelJson.class);
        inputStreamReader.close();
        OnlineDataLine[] odl = new OnlineDataLine[data.data.length];
        for (int i = 0;i < odl.length;i++) {
            odl[i] = new OnlineDataLine(new StaticDataContainer(data.data[i]), data.data_label[i]);
        }
        DataFXManager dm = new DataFXManager(rb, odl);
        for (int i=0;i < data.name.length;i++) {
            dm.marks.add(new Mark(data.channel[i], data.start[i], data.finish[i], data.name[i],
                    data.color[i], data.label_color[i]));
        }
        dm.swapper = new Integer[odl.length];
        for (int i=0;i < dm.swapper.length;i++) {
            dm.swapper[i] = i;
        }
        dm.rb = rb;
        dm.configureSwapper(dm.swapper);
        return dm;
    }

    public static DataFXManager<OnlineDataLine<DynamicDataContainer>, DynamicDataContainer> DataFXManagerFactory(
            ResourceBundle rb, String[] labels) {
        OnlineDataLine[] odl = new OnlineDataLine[labels.length];
        for (int i = 0;i < odl.length;i++) {
            odl[i] = new OnlineDataLine(new DynamicDataContainer(), labels[i]);
        }
        return new DataFXManager(rb, odl);
    }

    public void setModeToLine(int i, DataLineMode mode) {
        if (modes.length <= i)
            throw new ArrayIndexOutOfBoundsException();
        modes[i] = mode;
        glcView.setModeToLine(i, mode);
    }

    @Override
    protected void updateMarks() {
        if (glcView == null || glcOverview == null)
            return;
        Platform.runLater(()->{
            glcView.clearMarks();
            for (Mark mark : marks) {
                glcView.setMark(mark.channel, new XYChart.Data<Number, Number>(mark.start / getDiscretization(),
                                mark.finish / getDiscretization()), mark.name,
                        Color.web(mark.getWebColor()), Color.web(mark.getWebLabelColor()));
            }
            glcOverview.clearMarks();
            for (Mark mark : marks) {
                glcOverview.setMark(mark.channel, new XYChart.Data<Number, Number>(mark.start / getDiscretization(),
                                mark.finish / getDiscretization()), mark.name,
                        Color.web(mark.getWebColor()), Color.web(mark.getWebLabelColor()));
            }
        });
    }

    @Override
    protected void updateValues() {
        if (glcView == null || glcOverview == null)
            return;
        if (swapper.length != values.length || swapper.length != time.length)
            throw new IndexOutOfBoundsException();
        for (int i = 0;i < swapper.length;i++) {
            values[i] = dataLines[swapper[i]].getDataView(modes[i]);
            time[i] = dataLines[swapper[i]].getSecondsView(modes[i]);
            active[i] = dataLines[swapper[i]].getActiveView();
        }
        Platform.runLater(()->{
            glcView.setData(values, time);
            glcView.setRange(((double)view[0]) / getDiscretization(),
                    ((double)view[1]) / getDiscretization());
        });
    }

    @Override
    protected void updateOverviewValues() {
        if (glcView == null || glcOverview == null)
            return;
        if (swapper.length != valuesOverview.length || swapper.length != timeOverview.length)
            throw new IndexOutOfBoundsException();
        for (int i = 0;i < swapper.length;i++) {
            valuesOverview[i] = dataLines[swapper[i]].getDataOverview();
            timeOverview[i] = dataLines[swapper[i]].getSecondsOverview();
        }
        Platform.runLater(()->{
            glcOverview.setData(valuesOverview, timeOverview);
            glcOverview.setRange(0, getMaxView() / getDiscretization());
        });
    }

    public void configureSwapper(@NotNull Integer[] swapper) {
        super.setSwapper(swapper);
        values = new double[swapper.length][];
        time = new double[swapper.length][];
        active = new int[swapper.length];
        valuesOverview = new double[swapper.length][];
        timeOverview = new double[swapper.length][];
        if (glcView!= null)
            glcView.removeListener(this);
        glcView = new GroupLineChart(rb, getSwapperExtLabels(), true);
        glcView.addListener(this);
        glcOverview = new GroupLineChart(rb, getSwapperExtLabels(), false);
        glcOverview.setOnChangeSelection(()->{
            XYChart.Data<Number, Number> rangeMarker = glcOverview.getRangeMarker();
            setView(rangeMarker);
            setChanged();
            notifyObservers(DataManagerAction.SelectionChanged);
        });
        glcOverview.dynamicProperty().set(isOnline());
        glcView.dynamicProperty().set(isOnline());
        callViewUpdate();
        callOverviewUpdate();
    }

    @Override
    public void setSwapper(@NotNull Integer[] swapper) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public GroupLineChart getGlcOverview() {
        return glcOverview;
    }

    public GroupLineChart getGlcView() {
        return glcView;
    }

    public void addMark(int ch, XYChart.Data<Number, Number> xy, String name,
                        String color, String label_color) {
        xy.setXValue(xy.getXValue().doubleValue() * getDiscretization());
        xy.setYValue(xy.getYValue().doubleValue() * getDiscretization());
        super.addMark(ch, xy.getXValue().intValue(), xy.getYValue().intValue(),
                name, color, label_color);
    }

    public void addGlobalMark(XYChart.Data<Number, Number> xy, String name,
                                 String color, String label_color) {
        xy.setXValue(xy.getXValue().doubleValue() * getDiscretization());
        xy.setYValue(xy.getYValue().doubleValue() * getDiscretization());
        super.addGlobalMark(xy.getXValue().intValue(), xy.getYValue().intValue(),
                name, color, label_color);
    }

    private void fixMarks(XYChart.Data<Number, Number> xy) {
        xy.setXValue(xy.getXValue().doubleValue() * getDiscretization());
        xy.setYValue(xy.getYValue().doubleValue() * getDiscretization());
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
        super.cut((int)(xy.getXValue().intValue() * getDiscretization()),
                (int)((xy.getYValue().intValue() - xy.getXValue().intValue()) * getDiscretization()));
        fixMarks(xy);
        setMaxView();
    }

    public void setView(XYChart.Data<Number, Number> xy) {
        if (xy == null)
            return;
        XYChart.Data<Number, Number> xy2 = new XYChart.Data<>(xy.getXValue(), xy.getYValue());
        xy2.setXValue(xy2.getXValue().doubleValue() * getDiscretization());
        xy2.setYValue(xy2.getYValue().doubleValue() * getDiscretization());
        super.setView(xy2.getXValue().intValue(), xy2.getYValue().intValue());
        callViewUpdate();
    }

    public List<Mark> getMarks() {
        return this.marks;
    }

    @Override
    public void invalidated(Observable observable) {
        ViewLineChart[] vlc = glcView.getCharts();
        if (vlc.length != swapperExt.length)
            throw new IndexOutOfBoundsException();
        if (modes.length != values.length || modes.length != time.length)
            throw new IndexOutOfBoundsException();
        synchronized (modes) {
            int ss = 0;
            for (int i = 0; i < vlc.length; i++) {
                for (int k = 0; k < swapperExt[i].length; k++) {
                    modes[ss++] = vlc[i].getMode();
                }
            }
        }
        callViewUpdate();
    }

    public void addParser(WindowDynamicParser wer, int channel) {
        dataLines[channel].addParser(wer);
    }

    public void removeParser(WindowDynamicParser wer, int channel) {
        dataLines[channel].removeParser(wer);
    }

    public int[][] getConfiguration() {
        return swapperExt;
    }

    public void setConfiguration(int[][] swapperExt) {
        int size = 0;
        this.swapperExt = swapperExt;
        for (int i = 0;i < swapperExt.length;i++) {
            for (int j = 0; j < swapperExt[i].length; j++) {
                size++;
            }
        }
        Integer[] swapper = new Integer[size];
        size = 0;
        for (int i = 0;i < swapperExt.length;i++) {
            for (int j = 0; j < swapperExt[i].length; j++) {
                swapper[size++] = swapperExt[i][j];
            }
        }
        configureSwapper(swapper);
    }

    private String[][] getSwapperExtLabels() {
        String[] labels = getDataLabel();
        int[][] rt = getConfiguration();
        String[][] ret = new String[rt.length][];
        for (int i = 0;i < ret.length;i++) {
            ret[i] = new String[rt[i].length];
            for (int j = 0;j < ret[i].length;j++) {
                ret[i][j] = labels[rt[i][j]];
            }
        }
        return ret;
    }
}
