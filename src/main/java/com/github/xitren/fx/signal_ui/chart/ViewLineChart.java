package com.github.xitren.fx.signal_ui.chart;

import com.github.xitren.data.line.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class ViewLineChart extends HBox implements Observable {

    private static final double X_LABELS_HEIGHT = 40;
    private static final int X_VIEW = 2048;
    private final BooleanProperty dynamic = new SimpleBooleanProperty(false);
    private final XYChart.Data<Number, Number>[][] data;
    private ToggleButton btn_amp;
    private ToggleButton btn_freq;
    private ToggleButton btn_filt;
    private DataLineMode mode;
    private SelectableLineChart slc;
    private boolean notated;
    private boolean last;
    private final ResourceBundle rb;
    private Set<InvalidationListener> observers = new HashSet<>();
    protected XYChart.Series<Number, Number>[] series;
    private StringConverter<Number> sc = new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
            return String.format("%1.1f", object.doubleValue());
        }

        @Override
        public Number fromString(String string) {
            return 0;
        }
    };

    public ViewLineChart(ResourceBundle rb, String str, XYChart.Series<Number, Number>[] series, boolean notated, boolean last) {
        super();
        HBox.setHgrow(this, Priority.ALWAYS);
        this.data = new XYChart.Data[series.length][X_VIEW];
        this.series = series;
        for (int j = 0;j < series.length;j++) {
            for (int i = 0;i < X_VIEW;i++) {
                data[j][i] = new XYChart.Data(i, 0);
            }
            this.series[j].getData().addAll(data[j]);
        }
        this.rb = rb;
        this.notated = notated;
        this.last = last;
        this.slc = new SelectableLineChart(getXAxis(), getYAxis(str), notated);
        this.slc.dynamicProperty().bind(this.dynamic);
        this.slc.getData().addAll(this.series);
        this.slc.setAnimated(false);
        this.slc.setLegendVisible(false);
        if (!last)
            this.slc.setMinHeight(30);
        else
            this.slc.setMinHeight(30 + X_LABELS_HEIGHT);
        HBox.setHgrow(this.slc, Priority.ALWAYS);
        this.mode = DataLineMode.USUAL;
        if (this.notated)
            addButtons();
        getChildren().add(this.slc);
    }

    public void setData(int serve, double[] data, double[] time) {
        if (data == null)
            return;
        if (data.length != X_VIEW || time.length != X_VIEW || series.length <= serve)
            throw new RuntimeException("Array size does not meet the requirements");
        for (int i = 0;i < X_VIEW;i++) {
            this.data[serve][i].setXValue(time[i]);
            this.data[serve][i].setYValue(data[i]);
        }
    }

    public DataLineMode getMode() {
        return mode;
    }

    public SelectableLineChart getSlc() {
        return slc;
    }

    private static String concatStr(String[] sss){
        if (sss.length <= 0)
            return "";
        String ss = sss[0];
        for (int i = 1;i < sss.length;i++) {
            ss = ss + "/" + sss[i];
        }
        return ss;
    }

    public static void GroupLineChartFactory(@NotNull GroupLineChart glc,
                                             @NotNull ResourceBundle rb,
                                             boolean notated,
                                             Runnable onScroll) {
        String[][] labels = glc.getLabels();
        final ViewLineChart[] prep = glc.getCharts();
        final XYChart.Series<Number, Number>[][] series = glc.getSeries();
        for (int i = 0;i < labels.length;i++) {
            final ViewLineChart vlc = new ViewLineChart(rb, concatStr(labels[i]), series[i], notated, i == (labels.length - 1));
            prep[i] = vlc;
            glc.getChildren().add(vlc);
            vlc.addListener(glc);
            vlc.slc.setOnChangeSelection(()->{
                synchronizeSelection(prep, vlc, glc);
                if (onScroll != null)
                    onScroll.run();
            });
            vlc.slc.setOnSelection(()->{
                synchronizeProcessSelection(prep, vlc, glc);
            });
            vlc.slc.setOnCursor(()->{
                synchronizeCursor(prep, vlc);
            });
            final NumberAxis ax = (NumberAxis)vlc.slc.getXAxis();
            ax.lowerBoundProperty().bind(glc.startProperty());
            ax.upperBoundProperty().bind(glc.endProperty());
            ax.tickUnitProperty().bind(
                    ax.upperBoundProperty().subtract(ax.lowerBoundProperty()).divide(4));
            vlc.addListener((obs)->{
                switch (vlc.mode) {
                    case FOURIER:
                    case FILTERED_FOURIER:
                        if (ax.lowerBoundProperty().isBound())
                            ax.lowerBoundProperty().unbind();
                        if (ax.upperBoundProperty().isBound())
                            ax.upperBoundProperty().unbind();
                        if (ax.tickUnitProperty().isBound())
                            ax.tickUnitProperty().unbind();
                        ax.lowerBoundProperty().set(0);
                        ax.upperBoundProperty().set(X_VIEW / 2);
                        ax.tickUnitProperty().set(1);
                        ax.setTickLabelsVisible(true);
                        ax.setLabel(rb.getString("frequency_axis"));
                        ax.setAutoRanging(true);
                        ax.setForceZeroInRange(true);
                        break;
                    default:
                        ax.setAutoRanging(false);
                        ax.setForceZeroInRange(false);
                        if (!ax.lowerBoundProperty().isBound())
                            ax.lowerBoundProperty().bind(glc.startProperty());
                        if (!ax.upperBoundProperty().isBound())
                            ax.upperBoundProperty().bind(glc.endProperty());
                        if (!ax.tickUnitProperty().isBound())
                            ax.tickUnitProperty().bind(
                                    ax.upperBoundProperty().subtract(ax.lowerBoundProperty()).divide(4));

                        if (vlc.last) {
                            ax.setLabel(rb.getString("time_axis"));
                            ax.setTickLabelsVisible(true);
                        } else {
                            ax.setLabel(null);
                            ax.setTickLabelsVisible(false);
                        }
                        break;
                }
            });
        }
    }

    private static void synchronizeSelection(ViewLineChart[] prep, ViewLineChart vls, GroupLineChart glc) {
        glc.setRangeMarker(vls.slc.getSelectedRange());
        switch (glc.getTool()) {
            case UNI_SELECTOR:
                glc.setSelected(vls.slc);
                synchronizeProcessSelection(prep, vls, glc);
//                vls.slc.setInSelection(vls.slc.getSelectedRange());
                break;
            case GROUP_SELECTOR:
                glc.setSelected(null);
                synchronizeProcessSelection(prep, vls, glc);
                break;
        }
    }

    private static void synchronizeProcessSelection(ViewLineChart[] prep, ViewLineChart vls, GroupLineChart glc) {
        for (ViewLineChart ll : prep) {
            if (ll.equals(vls) && (glc.getTool() != GroupLineChart.Tool.DISABLED))
                continue;
            switch (glc.getTool()) {
                case UNI_SELECTOR:
                    ll.slc.clearSelection();
                    break;
                case GROUP_SELECTOR:
                    ll.slc.setSelectedRange(vls.slc.getSelectedRange());
                    break;
                case DISABLED:
                    ll.slc.clearSelection();
                    break;
                default:
                    ll.slc.clearSelection();
                    break;
            }
        }
    }

    private static void synchronizeCursor(ViewLineChart[] prep, ViewLineChart vls) {
        for (ViewLineChart ll : prep) {
            if (ll.slc.equals(vls))
                continue;
            ll.slc.setVerticalCursor(vls.slc.getCursorMarker());
        }
    }

    private NumberAxis getXAxis() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        xAxis.setAnimated(true);
        xAxis.setLabel(rb.getString("time_axis"));
        if (!last) {
            xAxis.setTickLabelsVisible(false);
            xAxis.setMinorTickVisible(false);
            xAxis.setTickMarkVisible(false);
            xAxis.setLabel(null);
        } else {
            xAxis.setTickLabelsVisible(true);
            xAxis.setMinorTickVisible(true);
            xAxis.setTickMarkVisible(true);
            xAxis.setTickLabelFormatter(sc);
        }
        xAxis.setPrefWidth(X_LABELS_HEIGHT);
        xAxis.setMinWidth(X_LABELS_HEIGHT);
        xAxis.setMaxWidth(X_LABELS_HEIGHT);
        xAxis.setMinorTickCount(2);
        xAxis.setForceZeroInRange(false);
        return xAxis;
    }

    private NumberAxis getYAxis(String str) {
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        yAxis.setAnimated(false);
        yAxis.setAutoRanging(true);
        if (!notated) {
            yAxis.setPrefWidth(0);
            yAxis.setMinWidth(0);
            yAxis.setMaxWidth(0);
            yAxis.setTickLabelsVisible(false);
            yAxis.setMinorTickVisible(false);
            yAxis.setTickMarkVisible(false);
            yAxis.setLabel(null);
        } else {
            yAxis.setPrefWidth(80);
            yAxis.setMinWidth(80);
            yAxis.setMaxWidth(80);
            yAxis.setLabel(str);
        }
        return yAxis;
    }

    private void addButtons() {
        VBox cbox = new VBox();
        VBox.setVgrow(cbox, Priority.ALWAYS);
        cbox.setMinWidth(38);
        cbox.setPrefWidth(38);
        cbox.setMaxWidth(38);
        cbox.setFillWidth(true);
        btn_amp = getAmplifierButton();
        btn_freq = getFrequencyButton();
        btn_filt = getFilterButton();
        btn_freq.setOnAction((act)->{
            if (btn_freq.isSelected()) {
                btn_amp.setSelected(false);
                if (btn_filt.isSelected()) {
                    mode = DataLineMode.FILTERED_FOURIER;
                } else {
                    mode = DataLineMode.FOURIER;
                }
            } else {
                if (btn_filt.isSelected()) {
                    mode = DataLineMode.FILTER;
                } else {
                    mode = DataLineMode.USUAL;
                }
            }
            changeMode();
        });
        btn_filt.setOnAction((act)->{
            if (btn_filt.isSelected()) {
                btn_amp.setSelected(false);
                if (btn_freq.isSelected()) {
                    mode = DataLineMode.FILTERED_FOURIER;
                } else {
                    mode = DataLineMode.FILTER;
                }
            } else {
                if (btn_freq.isSelected()) {
                    mode = DataLineMode.FOURIER;
                } else {
                    mode = DataLineMode.USUAL;
                }
            }
            changeMode();
        });
        btn_amp.setOnAction((act)->{
            if (btn_amp.isSelected()) {
                btn_freq.setSelected(false);
                btn_filt.setSelected(false);
                mode = DataLineMode.POWER;
            } else
                mode = DataLineMode.USUAL;
            changeMode();
        });
        cbox.getChildren().add(btn_freq);
        cbox.getChildren().add(btn_filt);
        cbox.getChildren().add(btn_amp);
        cbox.getStylesheets().add("/fxml/css/signal_ui_buttons.css");
        getChildren().add(cbox);
        setFillHeight(true);
    }

    private void changeMode() {
        observers.forEach((e)->{
            e.invalidated(this);
        });
    }

    private ToggleButton getFrequencyButton() {
        ToggleButton btn_freq = new ToggleButton();
        btn_freq.setTooltip(new Tooltip(rb.getString("tooltip_frequencies_mode")));
        btn_freq.setMinWidth(38);
        btn_freq.setPrefWidth(38);
        btn_freq.setMaxWidth(38);
        btn_freq.getStyleClass().add("freq");
        return btn_freq;
    }

    private ToggleButton getFilterButton() {
        ToggleButton btn_filt = new ToggleButton();
        btn_filt.setTooltip(new Tooltip(rb.getString("tooltip_filter_mode")));
        btn_filt.setMinWidth(38);
        btn_filt.setPrefWidth(38);
        btn_filt.setMaxWidth(38);
        btn_filt.getStyleClass().add("filter");
        return btn_filt;
    }

    private ToggleButton getAmplifierButton() {
        ToggleButton btn_amp = new ToggleButton();
        btn_amp.setTooltip(new Tooltip(rb.getString("tooltip_rms_mode")));
        btn_amp.setMinWidth(38);
        btn_amp.setPrefWidth(38);
        btn_amp.setMaxWidth(38);
        btn_amp.getStyleClass().add("amp");
        return btn_amp;
    }

    public void setMode(DataLineMode mode) {
        this.mode = mode;
        switch (mode) {
            case POWER:
                btn_freq.setSelected(false);
                btn_filt.setSelected(false);
                btn_amp.setSelected(true);
                break;
            case FILTERED_FOURIER:
                btn_freq.setSelected(true);
                btn_filt.setSelected(true);
                btn_amp.setSelected(false);
                break;
            case FILTER:
                btn_filt.setSelected(true);
                btn_freq.setSelected(false);
                btn_amp.setSelected(false);
                break;
            case FOURIER:
                btn_freq.setSelected(true);
                btn_filt.setSelected(false);
                btn_amp.setSelected(false);
                break;
            case USUAL:
            default:
                btn_freq.setSelected(false);
                btn_filt.setSelected(false);
                btn_amp.setSelected(false);
                break;
        }
        changeMode();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        observers.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        observers.remove(listener);
    }

    public void addVerticalRangeLabel(XYChart.Data<Number, Number> range, Color color, Color lable_color, String text) {
        slc.addVerticalRangeLabel(range, color, lable_color, text);
    }

    public void clearVerticalRangeLabels() {
        slc.clearVerticalRangeLabels();
    }

    public BooleanProperty dynamicProperty() {
        return dynamic;
    }
}
