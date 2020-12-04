package com.gusev.fx.signal_ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GroupLineChart extends VBox {
    final static int X_LABELS_HEIGHT = 20;

    private Chart[] charts;
    private Runnable onScroll;
    private Runnable onModeChange;
    private XYChart.Data<Number, Number> rangeMarker;
    private Tool current;
    private int size;
    private int rows;
    private DoubleProperty height = new SimpleDoubleProperty();
    private SelectableLineChart chartSelected;

    public Object[] getSelectedChannels() {
        if (chartSelected == null)
            return null;
        Set<Integer> ll = new HashSet<>();
        for (Chart c : charts) {
            if (chartSelected.equals(c.chart)) {
                ll.add(c.n);
            }
        }
        return ll.toArray();
    }

    public SelectableLineChart getSelectedChart() {
        return chartSelected;
    }

    public enum Tool {
        GROUP_SELECTOR, UNI_SELECTOR, DISABLED
    }

    public enum Mode {
        USUAL, FOURIER, FILTER, POWER
    }

    class Chart {
        private XYChart.Series series;
        private SelectableLineChart chart;
        private HBox box;
        private Mode mode;
        private int n;
        private int num;
        private Chart(int n, SelectableLineChart chart) {
            this.chart = chart;
            this.n = n;
            this.num = num;
            mode = Mode.USUAL;
            series = new XYChart.Series();
            chart.getData().add(series);
        }

        public void setMode(Mode mode) {
            this.mode = mode;
            switch (this.mode) {
                case FOURIER:
                    chart.getXAxis().setTickLabelsVisible(true);
                    ((NumberAxis)chart.getXAxis()).setMinorTickVisible(true);
                    chart.getXAxis().setTickMarkVisible(true);
                    break;
                default:
                    if (this.n != (this.num - 1)) {
                        chart.getXAxis().setTickLabelsVisible(false);
                        ((NumberAxis)chart.getXAxis()).setMinorTickVisible(false);
                        chart.getXAxis().setTickMarkVisible(false);
                    } else {
                        chart.getXAxis().setTickLabelsVisible(true);
                        ((NumberAxis)chart.getXAxis()).setMinorTickVisible(true);
                        chart.getXAxis().setTickMarkVisible(true);
                    }
                    break;
            }
            if (onModeChange != null)
                onModeChange.run();
        }

        public Mode getMode() {
            return mode;
        }
    }

    public void setCurrent(Tool current) {
        this.current = current;
    }

    public Tool getCurrent() {
        return current;
    }

    public SelectableLineChart getChart(boolean last) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        if (!last) {
            xAxis.setTickLabelsVisible(false);
            xAxis.setMinorTickVisible(false);
            xAxis.setTickMarkVisible(false);
        }
        xAxis.setPrefWidth(X_LABELS_HEIGHT);
        xAxis.setMinWidth(X_LABELS_HEIGHT);
        xAxis.setMaxWidth(X_LABELS_HEIGHT);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        yAxis.setPrefWidth(50);
        yAxis.setMinWidth(50);
        yAxis.setMaxWidth(50);
        yAxis.setAutoRanging(true);
        SelectableLineChart chart = new SelectableLineChart(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setMinHeight(30);
        chart.setOnChangeSelection(()->{
            synchronizeSelection(chart);
            if (onScroll != null)
                onScroll.run();
        });
        chart.setOnSelection(()->{
            synchronizeProcessSelection(chart);
        });
        return chart;
    }

    public GroupLineChart(int[] num, boolean controlled) {
        super();
        chartSelected = null;
        this.setFillWidth(true);
        int i = 0;
        int j = 0;
        size = 0;
        rows = num.length;
        for (int h : num) {
            size += h;
        }
        charts = new Chart[size];
        for (int h : num) {
            SelectableLineChart slc;
            if (j == (num.length - 1))
                slc = getChart(true);
            else
                slc = getChart(false);

            HBox box = getControlButtons(slc);
            for (int n = 0; n < h; n++) {
                charts[i] = new Chart(i, slc);
                charts[i].box = box;
                i++;
            }
            if (controlled) {
                this.getChildren().add(box);
            } else {
                this.getChildren().add(slc);
            }
            j++;
        }
        current = Tool.GROUP_SELECTOR;
    }

    private void switchMode(SelectableLineChart scl, Mode mode) {
        for (int n = 0; n < charts.length; n++) {
            if (charts[n].chart.equals(scl)) {
                charts[n].setMode(mode);
            }
        }
    }

    private HBox getControlButtons(SelectableLineChart scl){
        HBox box = new HBox();
        VBox cbox = new VBox();
        cbox.setMinWidth(38);
        cbox.setPrefWidth(38);
        cbox.setMaxWidth(38);
        cbox.setFillWidth(true);
        VBox.setVgrow(cbox, Priority.ALWAYS);
        ToggleButton btn_freq = new ToggleButton();
        ToggleButton btn_filt = new ToggleButton();
        ToggleButton btn_amp = new ToggleButton();
        ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(btn_freq, btn_filt, btn_amp);
        btn_freq.setMinWidth(38);
        btn_freq.setPrefWidth(38);
        btn_freq.setMaxWidth(38);
        btn_freq.getStyleClass().add("freq");
        btn_freq.setOnAction((act)->{
            if (btn_freq.isSelected())
                switchMode(scl, Mode.FOURIER);
            else
                switchMode(scl, Mode.USUAL);
        });
        cbox.getChildren().add(btn_freq);
        btn_filt.setMinWidth(38);
        btn_filt.setPrefWidth(38);
        btn_filt.setMaxWidth(38);
        btn_filt.getStyleClass().add("filter");
        btn_filt.setOnAction((act)->{
            if (btn_filt.isSelected())
                switchMode(scl, Mode.FILTER);
            else
                switchMode(scl, Mode.USUAL);
        });
        cbox.getChildren().add(btn_filt);
        btn_amp.setMinWidth(38);
        btn_amp.setPrefWidth(38);
        btn_amp.setMaxWidth(38);
        btn_amp.getStyleClass().add("amp");
        btn_amp.setOnAction((act)->{
            if (btn_amp.isSelected())
                switchMode(scl, Mode.POWER);
            else
                switchMode(scl, Mode.USUAL);
        });
        cbox.getChildren().add(btn_amp);
        cbox.getStylesheets().add("/fxml/css/signal_ui_buttons.css");
        box.getChildren().add(cbox);
        box.getChildren().add(scl);
        HBox.setHgrow(box, Priority.ALWAYS);
        HBox.setHgrow(scl, Priority.ALWAYS);
        box.setFillHeight(true);
        box.minHeightProperty().bind(height);
        box.prefHeightProperty().bind(height);
        box.maxHeightProperty().bind(height);
        return box;
    }

    public void setHeight(double height){
        setPrefHeight(height);
        setMinHeight(0);
        setMaxHeight(1000);
        height -= X_LABELS_HEIGHT;
        this.height.setValue(height / rows);
        for (Chart ll : charts) {
            ll.chart.setPrefHeight(height / rows);
            ll.chart.setMinHeight(height / rows);
            ll.chart.setMaxHeight(1000);
        }
        charts[charts.length - 1].chart.setPrefHeight((height / rows) + X_LABELS_HEIGHT);
        charts[charts.length - 1].chart.setMinHeight((height / rows) + X_LABELS_HEIGHT);
        charts[charts.length - 1].chart.setMaxHeight(1000);
    }

    public XYChart.Series<Number, Number>[] getSeries() {
        XYChart.Series<Number, Number>[] ret = new XYChart.Series[charts.length];
        for (int n = 0; n < charts.length; n++) {
            ret[n] = charts[n].series;
        }
        return ret;
    }

    public Chart[] getCharts() {
        return charts;
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

    public void setRangeMax(Number min, Number max) {
        for (Chart ct : charts) {
            if (ct.getMode() == Mode.FOURIER)
                continue;
            NumberAxis ll = (NumberAxis)ct.chart.getXAxis();
            ll.setLowerBound(min.doubleValue());
            ll.setUpperBound(max.doubleValue());
            ll.setTickUnit((max.doubleValue() - min.doubleValue()) / 20);
            ll.setMinorTickCount(2);
            ct.chart.setRangeMax(min, max);
        }
    }

    public void setRangeMax(int i, Number min, Number max) {
        NumberAxis ll = (NumberAxis)charts[i].chart.getXAxis();
        ll.setLowerBound(min.doubleValue());
        ll.setUpperBound(max.doubleValue());
        ll.setTickUnit((max.doubleValue() - min.doubleValue()) / 20);
        ll.setMinorTickCount(2);
        charts[i].chart.setRangeMax(min, max);
    }

    public void setMark(int i, XYChart.Data<Number, Number> range, String text, Color color, Color lable_color) {
        charts[i].chart.addVerticalRangeLabel(range, color, lable_color, text);
    }

    public void clearMarks() {
        for (int n = 0; n < charts.length; n++) {
            charts[n].chart.clearVerticalLabels();
        }
    }

    public void clear() {
        for (int n = 0; n < charts.length; n++) {
            charts[n].series.getData().clear();
        }
    }

    public XYChart.Data<Number, Number> getSelectedRange() {
        return rangeMarker;
    }

    public void synchronizeSelection(SelectableLineChart slc) {
        rangeMarker = slc.getSelectedRange();
        switch (current) {
            case UNI_SELECTOR:
                chartSelected = slc;
                break;
            case GROUP_SELECTOR:
                chartSelected = null;
                break;
        }
        synchronizeProcessSelection(slc);
    }

    public void synchronizeProcessSelection(SelectableLineChart slc) {
        for (Chart ll : charts) {
            if (ll.chart.equals(slc) && (current != Tool.DISABLED))
                continue;
            switch (current) {
                case UNI_SELECTOR:
                    ll.chart.clearSelection();
                    break;
                case GROUP_SELECTOR:
                    ll.chart.setInSelection(slc.getSelectedRange());
                    break;
                case DISABLED:
                    ll.chart.clearSelection();
                    break;
                default:
                    ll.chart.clearSelection();
                    break;
            }
        }
    }
}
