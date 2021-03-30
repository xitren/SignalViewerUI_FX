package io.github.xitren.fx.data;

import com.sun.deploy.si.SingleInstanceImpl;
import io.github.xitren.data.Mark;
import io.github.xitren.data.container.DataContainer;
import io.github.xitren.data.container.DynamicDataContainer;
import io.github.xitren.data.line.OnlineDataLine;
import io.github.xitren.data.manager.DataManager;
import io.github.xitren.data.manager.DataManagerAction;
import io.github.xitren.data.manager.DataManagerMapper;
import io.github.xitren.fx.signal_ui.chart.GroupLineChart;
import io.github.xitren.fx.signal_ui.chart.ViewLineChart;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.chart.XYChart;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class DataFXManager<V extends OnlineDataLine<T>, T extends DataContainer>
        extends DataManagerMapper<V, T> implements InvalidationListener {
    private Runnable onChangeSelection;
    protected GroupLineChart glcOverview;
    protected GroupLineChart glcView;
    protected double[][] values;
    protected double[][] time;
    protected double[][] valuesOverview;
    protected double[][] timeOverview;
    protected ResourceBundle rb;

    public DataFXManager(ResourceBundle rb, V[] edl) {
        super(edl);
        this.rb = rb;
        setSwapper(swapper);
    }

    public static DataFXManager<OnlineDataLine<DynamicDataContainer>, DynamicDataContainer> DataFXManagerFactory(
            ResourceBundle rb, String[] labels) {
        OnlineDataLine[] odl = new OnlineDataLine[labels.length];
        for (int i = 0;i < odl.length;i++) {
            odl[i] = new OnlineDataLine(new DynamicDataContainer(), labels[i]);
        }
        return new DataFXManager(rb, odl);
    }

    @Override
    protected void updateValues() {
        if (swapper.length != values.length || swapper.length != time.length)
            throw new IndexOutOfBoundsException();
        for (int i = 0;i < swapper.length;i++) {
            values[i] = dataLines[swapper[i]].getDataView(modes[i]);
            time[i] = dataLines[swapper[i]].getTimeView(modes[i]);
        }
        glcView.setData(values, time);
    }

    @Override
    protected void updateOverviewValues() {
        if (swapper.length != valuesOverview.length || swapper.length != timeOverview.length)
            throw new IndexOutOfBoundsException();
        for (int i = 0;i < swapper.length;i++) {
            valuesOverview[i] = dataLines[swapper[i]].getDataOverview();
            timeOverview[i] = dataLines[swapper[i]].getTimeOverview();
        }
        glcOverview.setData(valuesOverview, timeOverview);
    }

    @Override
    public void setSwapper(@NotNull Integer[] swapper) {
        super.setSwapper(swapper);
        values = new double[swapper.length][];
        time = new double[swapper.length][];
        valuesOverview = new double[swapper.length][];
        timeOverview = new double[swapper.length][];
        if (glcView!= null)
            glcView.removeListener(this);
        glcView = new GroupLineChart(rb, getDataLabel(), true);
        glcView.addListener(this);
        glcOverview = new GroupLineChart(rb, getDataLabel(), false);
        glcOverview.setOnChangeSelection(()->{
            XYChart.Data<Number, Number> rangeMarker = glcOverview.getRangeMarker();
            setView(rangeMarker);
            setChanged();
            notifyObservers(DataManagerAction.SelectionChanged);
        });
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
        if (modes.length != vlc.length)
            throw new IndexOutOfBoundsException();
        if (modes.length != values.length || modes.length != time.length)
            throw new IndexOutOfBoundsException();
        synchronized (modes) {
            for (int i = 0; i < vlc.length; i++) {
                modes[i] = vlc[i].getMode();
            }
        }
        callViewUpdate();
    }
}
