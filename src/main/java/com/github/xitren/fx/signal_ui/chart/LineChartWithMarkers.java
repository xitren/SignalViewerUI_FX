package com.github.xitren.fx.signal_ui.chart;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

public class LineChartWithMarkers extends LineChart<Number, Number> {
    private ObservableList<Data<Number, Number>> verticalRangeLabels;
    private Data<Number, Number> verticalSelector = new Data<>(0, 0);
    private Data<Number, Number> verticalCursor = new Data<>(0, 0);

    public LineChartWithMarkers(Axis<Number> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        verticalRangeLabels = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        verticalRangeLabels = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
        verticalRangeLabels.addListener((InvalidationListener)observable -> layoutPlotChildren());
        createVerticalCursor();
        createVerticalSelector();
    }

    private void createVerticalSelector() {
        Rectangle rectangle = new Rectangle(0,0,0,0);
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setFill(Color.GRAY.deriveColor(1,1,1,0.35));
        verticalSelector.setNode(rectangle);
//        getPlotChildren().add(rectangle);
        Text ll = new Text("<>");
        ll.setFill(Color.BLACK);
        verticalSelector.setExtraValue(ll);
//        getPlotChildren().add(ll);
        setMouseTransparentToEverythingButBackground();
    }

    private void createVerticalCursor() {
        Line line = new Line();
        line.setFill(Color.GRAY);
        line.setStroke(Color.GRAY);
        verticalCursor.setNode(line);
        getPlotChildren().add(line);
        Text ll = new Text("<>");
        ll.setFill(Color.BLACK);
        verticalCursor.setExtraValue(ll);
        getPlotChildren().add(ll);
        setMouseTransparentToEverythingButBackground();
    }

    private static Number getValueByMarker(ObservableList<XYChart.Data<Number, Number>> series, Number timeValue) {
        for (int i = 0;i < series.size();i++) {
            XYChart.Data<Number, Number> num = series.get(i);
            if (num.getXValue().doubleValue() > timeValue.doubleValue()) {
                return series.get(i).getYValue();
            }
        }
        return 0;
    }

    public void setVerticalCursor(Data<Number, Number> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        verticalCursor.setXValue(marker.getXValue());
        if (this.getData().size() > 0) {
            verticalCursor.setYValue(getValueByMarker(this.getData().get(0).getData(), marker.getXValue()));
        }
        Line line = (Line) verticalCursor.getNode();
        Text text = (Text) verticalCursor.getExtraValue();
        layoutPlotChildren();
        getPlotChildren().remove(line);
        getPlotChildren().remove(text);
        getPlotChildren().add(line);
        getPlotChildren().add(text);
    }

    protected void setVerticalSelection(Data<Number, Number> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getXValue().doubleValue() == 0 && marker.getYValue().doubleValue() == 0) {
            clearVerticalSelection();
            return;
        }
        verticalSelector.setXValue(marker.getXValue());
        verticalSelector.setYValue(marker.getYValue());
        Rectangle rect = (Rectangle) verticalSelector.getNode();
        Text text = (Text) verticalSelector.getExtraValue();
        layoutPlotChildren();
        getPlotChildren().remove(rect);
        getPlotChildren().remove(text);
        getPlotChildren().add(rect);
        getPlotChildren().add(text);
    }

    public void clearVerticalSelection() {
        Rectangle rect = (Rectangle) verticalSelector.getNode();
        Text text = (Text) verticalSelector.getExtraValue();
        layoutPlotChildren();
        getPlotChildren().remove(rect);
        getPlotChildren().remove(text);
    }

    public void clearVerticalRangeLabels() {
        for(Data<Number, Number> d : verticalRangeLabels){
            getPlotChildren().remove(d.getNode());
            getPlotChildren().remove(d.getExtraValue());
        }
        verticalRangeLabels.clear();
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        {
            Line line = (Line) verticalCursor.getNode();
            line.setStartX(getXAxis().getDisplayPosition(verticalCursor.getXValue()) + 0.5);  // 0.5 for crispness
            line.setEndX(line.getStartX());
            line.setStartY(0d);
            line.setEndY(getBoundsInLocal().getHeight());
            line.toFront();
            if (verticalCursor.getExtraValue() != null) {
                Text text = (Text) verticalCursor.getExtraValue();
                text.setText(String.format("%1.2f; %1.2f",
                        verticalCursor.getXValue().doubleValue(),
                        verticalCursor.getYValue().doubleValue()));
                text.setX(line.getStartX() + 20);
                text.setY(line.getStartY() + 40);
                text.setTextAlignment(TextAlignment.LEFT);
                text.setFont(new Font("Arial", 12));
                text.toFront();
            }
        }
        {
            Rectangle rectangle = (Rectangle) verticalSelector.getNode();
            rectangle.setX( getXAxis().getDisplayPosition(verticalSelector.getXValue()) + 0.5);  // 0.5 for crispness
            rectangle.setWidth( getXAxis().getDisplayPosition(verticalSelector.getYValue()) - getXAxis().getDisplayPosition(verticalSelector.getXValue()));
            rectangle.setY(0d);
            rectangle.setHeight(getBoundsInLocal().getHeight());
            rectangle.toBack();
            if (verticalSelector.getExtraValue() != null) {
                Text text = (Text) verticalSelector.getExtraValue();
                text.setText(String.format("%1.2f : %1.2f",
                        verticalSelector.getXValue().doubleValue(),
                        verticalSelector.getYValue().doubleValue()));
                text.setX(rectangle.getX() + 20);
                text.setY(rectangle.getY() + 20);
                text.setTextAlignment(TextAlignment.LEFT);
                text.setFont(new Font("Arial", 20));
                text.toFront();
            }
        }
        for (Data<Number, Number> verticalRangeMarker : verticalRangeLabels) {
            Rectangle rectangle = (Rectangle) verticalRangeMarker.getNode();
            rectangle.setX( getXAxis().getDisplayPosition(verticalRangeMarker.getXValue()) + 0.5);  // 0.5 for crispness
            rectangle.setWidth( getXAxis().getDisplayPosition(verticalRangeMarker.getYValue()) - getXAxis().getDisplayPosition(verticalRangeMarker.getXValue()));
            rectangle.setY(0d);
            rectangle.setHeight(getBoundsInLocal().getHeight());
            rectangle.toBack();
            Text text = (Text) verticalRangeMarker.getExtraValue();
            text.setX(rectangle.getX() + 20);
            text.setY(rectangle.getY() + 20);
            text.setTextAlignment(TextAlignment.CENTER);
            text.setFont(new Font("Arial", 20));
            text.toBack();
            rectangle.toBack();
        }
    }

    public void addVerticalRangeLabel(Data<Number, Number> marker, Color color, Color text_color, String str) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalRangeLabels.contains(marker)) return;

        Rectangle rectangle = new Rectangle(0,0,0,0);
        rectangle.setStroke(Color.BLACK);
        rectangle.setFill(color.deriveColor(1, 1, 1, 0.6));

        marker.setNode(rectangle);

        Text ll = new Text(str);
        ll.setFill(text_color);
        marker.setExtraValue(ll);

        getPlotChildren().add(rectangle);
        getPlotChildren().add(ll);
        verticalRangeLabels.add(marker);
        setMouseTransparentToEverythingButBackground();
    }

    public void setMouseTransparentToEverythingButBackground(){
        final Node chartBackground = lookup(".chart-plot-background");
        for (Node n: chartBackground.getParent().getChildrenUnmodifiable()) {
            if (n != chartBackground) {
                n.setMouseTransparent(true);
            }
        }
    }

    public void removeVerticalRangeLabel(Data<Number, Number> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            getPlotChildren().remove(marker.getExtraValue());
            marker.setNode(null);
            marker.setExtraValue(null);
        }
        verticalRangeLabels.remove(marker);
    }
}
