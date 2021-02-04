package com.gusev.fx.signal_ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class GroupLineChart extends VBox {
    final static int X_LABELS_HEIGHT = 40;
    private final ResourceBundle rb;

    private Chart[] charts;
    private Runnable onScroll;
    private Runnable onModeChange;
    private XYChart.Data<Number, Number> rangeMarker;
    private Tool current;
    private int size;
    private int rows;
    private DoubleProperty height = new SimpleDoubleProperty();
    private SelectableLineChart chartSelected;
    private boolean notated;

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
        USUAL, FOURIER, FILTER, FILTERED_FOURIER, POWER
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
            this.series = new XYChart.Series();
            this.chart.getData().add(this.series);



        }

        public void setMode(Mode mode) {
            this.mode = mode;
            switch (this.mode) {
                case FILTERED_FOURIER:
                case FOURIER:
                    chart.getXAxis().setLabel(rb.getString("frequency_axis"));
                    chart.getXAxis().setTickLabelsVisible(true);
                    ((NumberAxis)chart.getXAxis()).setMinorTickVisible(true);
                    chart.getXAxis().setTickMarkVisible(true);
                    break;
                default:
                    if (this.n != (this.num - 1)) {
                        chart.getXAxis().setLabel(null);
                        chart.getXAxis().setTickLabelsVisible(false);
                        ((NumberAxis)chart.getXAxis()).setMinorTickVisible(false);
                        chart.getXAxis().setTickMarkVisible(false);
                    } else {
                        chart.getXAxis().setLabel(rb.getString("time_axis"));
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
        xAxis.setLabel(rb.getString("time_axis"));
        if (!last) {
            xAxis.setTickLabelsVisible(false);
            xAxis.setMinorTickVisible(false);
            xAxis.setTickMarkVisible(false);
            xAxis.setLabel(null);
        }
        xAxis.setPrefWidth(X_LABELS_HEIGHT);
        xAxis.setMinWidth(X_LABELS_HEIGHT);
        xAxis.setMaxWidth(X_LABELS_HEIGHT);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        yAxis.setAnimated(false);
        yAxis.setPrefWidth(50);
        yAxis.setMinWidth(50);
        yAxis.setMaxWidth(50);
        yAxis.setAutoRanging(true);

        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                int length = object.toString().length();
                if(length > 8) { //MAKE SOMETHING BETTER HERE
                    //System.out.println("Was string: " + object.toString() + " length: " + length + " num " + object);
                    String nValue = object.toString().substring(0,2) +"...." +object.toString().substring(length-4, length);
                    //System.out.println("Now: " + nValue);
                    return nValue;
                }
                return object.toString();
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });


        SelectableLineChart chart = new SelectableLineChart(xAxis, yAxis, notated);
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
        HBox.setHgrow(this, Priority.ALWAYS);



//            NumberAxis yAxis = new NumberAxis(20, 90, 10);
//            yAxis.;







        return chart;
    }

    public GroupLineChart(int[] num, String[] labels, boolean controlled, boolean notated, ResourceBundle rb) {
        super();
        chartSelected = null;
        this.notated = notated;
        this.rb = rb;
        this.setFillWidth(true);
        this.setMinHeight(0);
        this.setMaxHeight(1000);
        int i = 0;
        size = 0;
        rows = num.length;
        for (int h : num) {
            size += h;
        }
        if (size != labels.length)
            throw new IndexOutOfBoundsException("Found more/not enough labels than expected!");
        charts = new Chart[size];
        for (int j = 0;j < num.length;j++) {
            int h = num[j];
            if (h == 0)
                continue;
            SelectableLineChart slc;
            if (j == (num.length - 1))
                slc = getChart(true);
            else
                slc = getChart(false);
            HBox box = getControlButtons(slc);
            String str = labels[i];
            for (int n = 0; n < h; n++) {
                if (n != 0) {
                    str = str.concat(" / " + labels[i]);
                }
                charts[i] = new Chart(i, slc);
                charts[i].box = box;
                i++;
            }
            slc.getYAxis().setLabel(str);
            if (controlled) {
                this.getChildren().add(box);
            } else {
                this.getChildren().add(slc);
            }
        }
        current = Tool.GROUP_SELECTOR;
        VBox.setVgrow(this, Priority.ALWAYS);
        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            this.setHeight(newValue.doubleValue());
        });
    }

    public GroupLineChart(int[] num, boolean controlled, boolean notated, ResourceBundle rb) {
        super();
        chartSelected = null;
        this.notated = notated;
        this.rb = rb;
        this.setFillWidth(true);
        this.setMinHeight(0);
        this.setMaxHeight(1000);
        int i = 0;
        int j = 0;
        size = 0;
        rows = num.length;
        for (int h : num) {
            size += h;
        }
        charts = new Chart[size];
        for (int h : num) {
            if (h == 0)
                continue;
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
        VBox.setVgrow(this, Priority.ALWAYS);
        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            this.setHeight(newValue.doubleValue());
        });
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
        btn_freq.setMinWidth(38);
        btn_freq.setPrefWidth(38);
        btn_freq.setMaxWidth(38);
        btn_freq.getStyleClass().add("freq");
        btn_freq.setOnAction((act)->{
            if (btn_freq.isSelected()) {
                btn_amp.setSelected(false);
                if (btn_filt.isSelected()) {
                    switchMode(scl, Mode.FILTERED_FOURIER);
                } else {
                    switchMode(scl, Mode.FOURIER);
                }
            } else {
                if (btn_filt.isSelected()) {
                    switchMode(scl, Mode.FILTER);
                } else {
                    switchMode(scl, Mode.USUAL);
                }
            }
        });
        cbox.getChildren().add(btn_freq);
        btn_filt.setMinWidth(38);
        btn_filt.setPrefWidth(38);
        btn_filt.setMaxWidth(38);
        btn_filt.getStyleClass().add("filter");
        btn_filt.setOnAction((act)->{
            if (btn_filt.isSelected()) {
                btn_amp.setSelected(false);
                if (btn_freq.isSelected()) {
                    switchMode(scl, Mode.FILTERED_FOURIER);
                } else {
                    switchMode(scl, Mode.FILTER);
                }
            } else {
                if (btn_freq.isSelected()) {
                    switchMode(scl, Mode.FOURIER);
                } else {
                    switchMode(scl, Mode.USUAL);
                }
            }
        });
        cbox.getChildren().add(btn_filt);
        btn_amp.setMinWidth(38);
        btn_amp.setPrefWidth(38);
        btn_amp.setMaxWidth(38);
        btn_amp.getStyleClass().add("amp");
        btn_amp.setOnAction((act)->{
            if (btn_amp.isSelected()) {
                btn_freq.setSelected(false);
                btn_filt.setSelected(false);
                switchMode(scl, Mode.POWER);
            } else
                switchMode(scl, Mode.USUAL);
        });
        cbox.getChildren().add(btn_amp);
        cbox.getStylesheets().add("/fxml/css/signal_ui_buttons.css");
        box.getChildren().add(cbox);
        box.getChildren().add(scl);
        HBox.setHgrow(box, Priority.ALWAYS);
        HBox.setHgrow(scl, Priority.ALWAYS);
        box.setFillHeight(true);
        return box;
    }

    public void setHeight(double height){
        height -= X_LABELS_HEIGHT;
        for (Chart ll : charts) {
            ll.chart.setPrefHeight(height / this.getChildren().size());
            ll.chart.setMinHeight(height / this.getChildren().size());
            ll.chart.setMaxHeight(1000);
        }
        charts[charts.length - 1].chart.setPrefHeight((height / this.getChildren().size()) + X_LABELS_HEIGHT);
        charts[charts.length - 1].chart.setMinHeight((height / this.getChildren().size()) + X_LABELS_HEIGHT);
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
        try {
            for (int n = 0; n < charts.length; n++) {
                if (charts[n].series == null)
                    continue;
                charts[n].series.getData().clear();
            }
        } catch (NullPointerException ex) {
            System.out.println();
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
