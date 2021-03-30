package io.github.xitren.fx.signal_ui.chart;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class GroupLineChart extends VBox implements InvalidationListener, Observable {
    private final ResourceBundle rb;
    private final ViewLineChart[] charts;
    private final String[] labels;
    private final boolean notated;
    private XYChart.Data<Number, Number> rangeMarker;
    private SelectableLineChart chartSelected;
    private Tool tool;
    private Runnable onModeChange;
    private Runnable onScroll;
    private Set<InvalidationListener> observers = new HashSet<>();

    public GroupLineChart(ResourceBundle rb, @NotNull String[] labels,
                          boolean notated) {
        this.tool = Tool.GROUP_SELECTOR;
        this.labels = labels;
        this.notated = notated;
        this.rb = rb;
        this.charts = new ViewLineChart[labels.length];
        ViewLineChart.GroupLineChartFactory(this, rb, notated, ()->{
            if (onScroll != null)
                onScroll.run();
        });
    }

    public void setData(double[][] data, double[][] time) {
        if (data.length != charts.length || time.length != charts.length)
            throw new RuntimeException("Array size does not meet the requirements");
        for (int i = 0;i < charts.length;i++) {
            charts[i].setData(data[i], time[i]);
        }
    }

    public void setData(int i, double[] data, double[] time) {
        if (!((0 <= i) && (i < charts.length)))
            throw new RuntimeException("Array size does not meet the requirements");
        charts[i].setData(data, time);
    }

    public SelectableLineChart getSelectedChart() {
        return chartSelected;
    }

    public ViewLineChart[] getCharts() {
        return charts;
    }

    public Tool getTool() {
        return tool;
    }

    public void setRangeMarker(XYChart.Data<Number, Number> selectedRange) {
        rangeMarker = selectedRange;
    }

    public void setSelected(SelectableLineChart slc) {
        chartSelected = slc;
    }

    public String[] getLabels() {
        return labels;
    }

    @Override
    public void invalidated(Observable observable) {
        if (observable instanceof ViewLineChart) {
            observers.forEach((e)->{
                e.invalidated(this);
            });
        }
    }

    @Override
    public void addListener(InvalidationListener listener) {
        observers.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        observers.remove(listener);
    }

    public enum Tool {
        GROUP_SELECTOR, UNI_SELECTOR, DISABLED
    }

    public int size() {
        return charts.length;
    }

    public void setOnChangeSelection(Runnable onScroll) {
        this.onScroll = onScroll;
    }

    public void setOnChangeMode(Runnable onModeChange) {
        this.onModeChange = onModeChange;
    }

    public void setMark(int i, XYChart.Data<Number, Number> range, String text, Color color, Color lable_color) {
        charts[i].addVerticalRangeLabel(range, color, lable_color, text);
    }

    public void clearMarks() {
        for (int n = 0; n < charts.length; n++) {
            charts[n].clearVerticalLabels();
        }
    }
}