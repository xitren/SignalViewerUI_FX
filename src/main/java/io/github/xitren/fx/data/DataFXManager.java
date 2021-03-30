package io.github.xitren.fx.data;

import io.github.xitren.data.Mark;
import io.github.xitren.data.container.DataContainer;
import io.github.xitren.data.line.OnlineDataLine;
import io.github.xitren.data.manager.DataManagerMapper;
import io.github.xitren.fx.signal_ui.chart.GroupLineChart;
import io.github.xitren.fx.signal_ui.chart.ViewLineChart;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.chart.XYChart;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class DataFXManager<V extends OnlineDataLine<T>, T extends DataContainer>
        extends DataManagerMapper<V, T> implements InvalidationListener {
    protected GroupLineChart glcOverview;
    protected GroupLineChart glcView;

    public DataFXManager(ResourceBundle rb, V[] edl) {
        super(edl);
        glcView = new GroupLineChart(rb, getDataLabel(), true);
        glcView.addListener(this);
        glcOverview = new GroupLineChart(rb, getDataLabel(), false);
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
        XYChart.Data<Number, Number> xy2 = new XYChart.Data<>(xy.getXValue(), xy.getYValue());
        xy2.setXValue(xy2.getXValue().doubleValue() * getDiscretization());
        xy2.setYValue(xy2.getYValue().doubleValue() * getDiscretization());
        super.setView(xy2.getXValue().intValue(), xy2.getYValue().intValue());
    }

    public List<Mark> getMarks() {
        return this.marks;
    }

    @Override
    public void invalidated(Observable observable) {
        ViewLineChart[] vlc = glcView.getCharts();
        if (modes.length != vlc.length)
            throw new IndexOutOfBoundsException();
        for (int i = 0;i < vlc.length;i++) {
            modes[i] = vlc[i].getMode();
        }
    }
}
