package com.github.xitren.fx.signal_ui.chart;

import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

public class SelectableLineChart extends LineChartWithMarkers {
    private boolean dynamic = false;
    private boolean selection = false;
    private XYChart.Data<Number, Number> cursorMarker = new XYChart.Data<Number, Number>(0, 0);
    private XYChart.Data<Number, Number> rangeMarker;
    private XYChart.Data<Number, Number> rangeMax = new XYChart.Data<>(0, 0);
    private Runnable onScroll;
    private Runnable onCursor;
    private Runnable onProcessSelect;
    private boolean notated;

    public SelectableLineChart(Axis<Number> xAxis, Axis<Number> yAxis, boolean notated) {
        super(xAxis, yAxis);
        this.notated = notated;
        setLegendVisible(false);
        setCreateSymbols(false);
        lookup(".chart-plot-background").setOnMouseMoved((me)->{
            if (dynamic)
                return;
            Number timeValue = this.getXAxis().getValueForDisplay(me.getX());
            ObservableList<XYChart.Data<Number, Number>> series = getData().get(0).getData();
            Number value = 0;
            cursorMarker.setXValue(timeValue);
            cursorMarker.setYValue(value);
            this.setVerticalCursor(cursorMarker);
            if (onCursor != null)
                onCursor.run();
        });
        lookup(".chart-plot-background").setOnScroll((se)->{
            if (selection) {
                this.clearVerticalMarkers();
                Number mid = (rangeMarker.getXValue().doubleValue() + rangeMarker.getYValue().doubleValue()) / 2;
                Number nStart;
                Number nEnd;
                if (se.getDeltaY() < 0) {
                    nStart = (mid.doubleValue() - rangeMarker.getXValue().doubleValue()) * 2;
                    nEnd = (rangeMarker.getYValue().doubleValue() - mid.doubleValue()) * 2;
                } else {
                    nStart = (mid.doubleValue() - rangeMarker.getXValue().doubleValue()) / 2;
                    nEnd = (rangeMarker.getYValue().doubleValue() - mid.doubleValue()) / 2;
                }
                rangeMarker.setXValue(mid.doubleValue() - nStart.doubleValue());
                rangeMarker.setYValue(mid.doubleValue() + nEnd.doubleValue());
                if (rangeMarker.getXValue().doubleValue() < rangeMax.getXValue().doubleValue())
                    rangeMarker.setXValue(rangeMax.getXValue());
                if (rangeMax.getYValue().doubleValue() < rangeMarker.getYValue().doubleValue())
                    rangeMarker.setYValue(rangeMax.getYValue());
                XYChart.Data<Number, Number> startMarker = new XYChart.Data<Number, Number>(rangeMarker.getXValue(), 0);
                XYChart.Data<Number, Number> endMarker = new XYChart.Data<Number, Number>(rangeMarker.getYValue(), 0);
                this.addVerticalValueMarker(startMarker);
                this.addVerticalValueMarker(endMarker);
                this.addVerticalRangeMarker(rangeMarker, Color.STEELBLUE, notated);
                if (onScroll != null)
                    onScroll.run();
            }
        });
        lookup(".chart-plot-background").setOnMousePressed((me)->{
            if (dynamic)
                return;
            if (me.isSecondaryButtonDown()) {
                selection = false;
                rangeMarker.setXValue(0);
                rangeMarker.setYValue(0);
                this.clearVerticalMarkers();
                return;
            }
            if (me.isPrimaryButtonDown()) {
                Number timeValueSelectionStart = this.getXAxis().getValueForDisplay(me.getX());
                Number value = this.getYAxis().getValueForDisplay(me.getY());
                this.clearVerticalMarkers();
                rangeMarker = new XYChart.Data<Number, Number>(timeValueSelectionStart, timeValueSelectionStart);
                this.addVerticalRangeMarker(rangeMarker, Color.STEELBLUE, notated);
                selection = true;
            }
        });
        lookup(".chart-plot-background").setOnMouseReleased((me)->{
            if (dynamic)
                return;
            if (selection) {
                Number timeValueSelectionEnd = this.getXAxis().getValueForDisplay(me.getX());
                Number value = this.getYAxis().getValueForDisplay(me.getY());
                rangeMarker.setYValue(timeValueSelectionEnd);
                XYChart.Data<Number, Number> startMarker = new XYChart.Data<Number, Number>(rangeMarker.getXValue(), value);
                XYChart.Data<Number, Number> endMarker = new XYChart.Data<Number, Number>(rangeMarker.getYValue(), value);
                this.addVerticalValueMarker(startMarker);
                this.addVerticalValueMarker(endMarker);
                if (onScroll != null)
                    onScroll.run();
            }
        });
        lookup(".chart-plot-background").setOnMouseExited((me)->{
            if (!selection) {
                this.clearVerticalMarkers();
            }
        });
        lookup(".chart-plot-background").setOnMouseDragged((me)->{
            if (dynamic)
                return;
            if (selection) {
                Number timeValueSelection = this.getXAxis().getValueForDisplay(me.getX());
                rangeMarker.setYValue(timeValueSelection);
                if (onProcessSelect != null)
                    onProcessSelect.run();
            }
        });
    }

    public XYChart.Data<Number, Number> getCursorMarker() {
        return cursorMarker;
    }

    public XYChart.Data<Number, Number> getSelectedRange() {
        return rangeMarker;
    }

    public boolean isSelection() {
        return selection;
    }

    public void setSelection(XYChart.Data<Number, Number> sel) {
        selection = true;
        if (rangeMarker == null)
            rangeMarker = new XYChart.Data<Number, Number>(0, 0);
        rangeMarker.setXValue(sel.getXValue());
        rangeMarker.setYValue(sel.getYValue());
        this.clearVerticalMarkers();
        XYChart.Data<Number, Number> startMarker = new XYChart.Data<Number, Number>(rangeMarker.getXValue(), 0);
        XYChart.Data<Number, Number> endMarker = new XYChart.Data<Number, Number>(rangeMarker.getYValue(), 0);
        this.addVerticalValueMarker(startMarker);
        this.addVerticalValueMarker(endMarker);
        this.addVerticalRangeMarker(rangeMarker, Color.STEELBLUE, notated);
    }

    public void setInSelection(XYChart.Data<Number, Number> sel) {
        selection = true;
        if (rangeMarker == null)
            rangeMarker = new XYChart.Data<Number, Number>(0, 0);
        rangeMarker.setXValue(sel.getXValue());
        rangeMarker.setYValue(sel.getYValue());
        this.clearVerticalMarkers();
        this.addVerticalRangeMarker(rangeMarker, Color.STEELBLUE, false);
    }

    public void clearSelection() {
        this.clearVerticalMarkers();
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

    public void setDynamic() {
        this.clearVerticalMarkers();
        dynamic = true;
    }

    public void unsetDynamic() {
        dynamic = false;
    }
}
