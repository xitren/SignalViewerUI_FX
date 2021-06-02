package com.github.xitren.fx.signal_ui.chart;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

public class SelectableLineChart extends LineChartWithMarkers {
    private final BooleanProperty dynamic = new SimpleBooleanProperty(false);
    private final BooleanProperty scroll = new SimpleBooleanProperty(false);
    private boolean selection = false;
    private boolean selected = false;
    private XYChart.Data<Number, Number> cursorMarker = new XYChart.Data<>(0, 0);
    private XYChart.Data<Number, Number> selectionMarker = new XYChart.Data<>(0, 0);
    private XYChart.Data<Number, Number> rangeMax = new XYChart.Data<>(0, 0);
    private Runnable onScroll;
    private Runnable onCursor;
    private Runnable onProcessSelect;
    private boolean notated;
    private Number selectionStart;

    public SelectableLineChart(Axis<Number> xAxis, Axis<Number> yAxis, boolean notated) {
        super(xAxis, yAxis);
        this.notated = notated;
        setLegendVisible(false);
        setCreateSymbols(false);
        lookup(".chart-plot-background").setOnMouseMoved((me)->{
            if (dynamic.get())
                return;
            Number timeValue = this.getXAxis().getValueForDisplay(me.getX());
            if (!selection) {
                cursorMarker.setXValue(timeValue);
                cursorMarker.setYValue(0);
                this.setVerticalCursor(cursorMarker);
                if (onCursor != null)
                    onCursor.run();
            } else {
                updateSelector(me.getX());
            }
        });
        lookup(".chart-plot-background").setOnScroll((se)->{
            if (scroll.get()) {
                Number mid = (selectionMarker.getXValue().doubleValue() + selectionMarker.getYValue().doubleValue()) / 2;
                Number nStart;
                Number nEnd;
                if (se.getDeltaY() < 0) {
                    nStart = (mid.doubleValue() - selectionMarker.getXValue().doubleValue()) * 2;
                    nEnd = (selectionMarker.getYValue().doubleValue() - mid.doubleValue()) * 2;
                } else {
                    nStart = (mid.doubleValue() - selectionMarker.getXValue().doubleValue()) / 2;
                    nEnd = (selectionMarker.getYValue().doubleValue() - mid.doubleValue()) / 2;
                }
                selectionMarker.setXValue(mid.doubleValue() - nStart.doubleValue());
                selectionMarker.setYValue(mid.doubleValue() + nEnd.doubleValue());
                if (selectionMarker.getXValue().doubleValue() < rangeMax.getXValue().doubleValue())
                    selectionMarker.setXValue(rangeMax.getXValue());
                if (rangeMax.getYValue().doubleValue() < selectionMarker.getYValue().doubleValue())
                    selectionMarker.setYValue(rangeMax.getYValue());
                this.setVerticalSelection(selectionMarker);
                if (onScroll != null)
                    onScroll.run();
            }
        });
        lookup(".chart-plot-background").setOnMousePressed((me)->{
            if (dynamic.get())
                return;
            if (me.isSecondaryButtonDown()) {
                selection = false;
                clearSelector();
                return;
            }
            if (me.isPrimaryButtonDown()) {
                selectionStart = this.getXAxis().getValueForDisplay(me.getX());
                selection = true;
                selected = false;
            }
        });
        lookup(".chart-plot-background").setOnMouseReleased((me)->{
            if (dynamic.get())
                return;
            selection = false;
            if (selected == false) {
                clearSelector();
            }
        });
        lookup(".chart-plot-background").setOnMouseExited((me)->{
//            if (selection) {
//                clearSelector();
//            }
//            selection = false;
        });
        lookup(".chart-plot-background").setOnMouseDragged((me)->{
            if (dynamic.get())
                return;
            if (selection) {
                updateSelector(me.getX());
                selected = true;
            }
        });
    }

    private final void updateSelector(double x) {
        Number timeValue = this.getXAxis().getValueForDisplay(x);
        if (selectionStart.doubleValue() < timeValue.doubleValue()) {
            selectionMarker.setXValue(selectionStart);
            selectionMarker.setYValue(timeValue);
        } else {
            selectionMarker.setXValue(timeValue);
            selectionMarker.setYValue(selectionStart);
        }
        this.setVerticalSelection(selectionMarker);
        if (onScroll != null)
            onScroll.run();
    }

    private final void clearSelector() {
        selectionStart = 0;
        selectionMarker.setXValue(0);
        selectionMarker.setYValue(0);
        this.clearVerticalSelection();
        if (onScroll != null)
            onScroll.run();
    }

    public XYChart.Data<Number, Number> getCursorMarker() {
        return cursorMarker;
    }

    public XYChart.Data<Number, Number> getSelectedRange() {
        return selectionMarker;
    }

    public void setSelectedRange(Data<Number, Number> selectedRange) {
        selectionMarker.setXValue(selectedRange.getXValue());
        selectionMarker.setYValue(selectedRange.getYValue());
        this.setVerticalSelection(selectionMarker);
    }

    public void clearSelection() {
        this.clearVerticalSelection();
    }

    public BooleanProperty dynamicProperty() {
        return dynamic;
    }

    public BooleanProperty scrollProperty() {
        return scroll;
    }

    public void setRangeMax(Number min, Number max) {
        rangeMax.setXValue(min);
        rangeMax.setYValue(max);
    }

    public void setOnChangeSelection(Runnable onScroll) {
        this.onScroll = onScroll;
    }

    public void setOnCursor(Runnable onCursor) {
        this.onCursor = onCursor;
    }

    public void setOnSelection(Runnable on) {
        this.onProcessSelect = on;
    }
}
