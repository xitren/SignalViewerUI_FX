package com.gusev.fx.signal_ui;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

public class LineChartWithMarkers extends LineChart<Number, Number> {
    private ObservableList<Data<Number, Number>> horizontalMarkers;
    private ObservableList<Data<Number, Number>> verticalMarkers;
    private ObservableList<Data<Number, Number>> verticalRangeMarkers;
    private ObservableList<Data<Number, Number>> verticalRangeLabels;
    private Data<Number, Number> verticalCursor = new Data<>(0, 0);

    public LineChartWithMarkers(Axis<Number> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
        horizontalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
        verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
        verticalRangeMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        verticalRangeMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()}); // 2nd type of the range is X type as well
        verticalRangeMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
        verticalRangeLabels = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        verticalRangeLabels = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()}); // 2nd type of the range is X type as well
        verticalRangeLabels.addListener((InvalidationListener)observable -> layoutPlotChildren());
        createVerticalCursor();
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
    }

    public void addHorizontalValueMarker(Data<Number, Number> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (horizontalMarkers.contains(marker)) return;
        Line line = new Line();
        marker.setNode(line);
        getPlotChildren().add(line);
        horizontalMarkers.add(marker);
    }

    public void removeHorizontalValueMarker(Data<Number, Number> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        horizontalMarkers.remove(marker);
    }

    public void addVerticalValueMarker(Data<Number, Number> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalMarkers.contains(marker)) return;
        Line line = new Line();
        line.setFill(Color.GRAY);
        line.setStroke(Color.GRAY);
        marker.setNode(line);
        getPlotChildren().add(line);
        verticalMarkers.add(marker);
        setMouseTransparentToEverythingButBackground();
    }

    public void addVerticalValueMarker(Data<Number, Number> marker, Color col) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalMarkers.contains(marker)) return;
        Line line = new Line();
        line.setFill(col);
        line.setStroke(col);
        line.setStrokeWidth(2);
        marker.setNode(line );
        getPlotChildren().add(line);
        verticalMarkers.add(marker);
        setMouseTransparentToEverythingButBackground();
    }

    public void removeVerticalValueMarker(Data<Number, Number> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        verticalMarkers.remove(marker);

    }
    public void clearVerticalMarkers(){
        for(Data<Number, Number> d : verticalMarkers){
            getPlotChildren().remove(d.getNode());
        }
        for(Data<Number, Number> d : verticalRangeMarkers){
            getPlotChildren().remove(d.getNode());
            if (d.getExtraValue() != null) {
                getPlotChildren().remove(d.getExtraValue());
            }
        }
        verticalMarkers.clear();
        verticalRangeMarkers.clear();
    }

    public void clearVerticalLabels(){
        for(Data<Number, Number> d : verticalRangeLabels){
            getPlotChildren().remove(d.getNode());
            getPlotChildren().remove(d.getExtraValue());
        }
        verticalRangeLabels.clear();
    }

    public void clearHorizontalMarkers(){
        for(Data<Number, Number> d : horizontalMarkers){
            getPlotChildren().remove(d.getNode());
        }
        verticalRangeMarkers.clear();
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
                text.setText(String.format("<%1.2f, %1.2f>",
                        verticalCursor.getXValue().doubleValue(),
                        verticalCursor.getYValue().doubleValue()));
                text.setX(line.getStartX() + 20);
                text.setY(line.getStartY() + 40);
                text.setTextAlignment(TextAlignment.LEFT);
                text.setFont(new Font("Arial", 12));
                text.toFront();
            }
        }
        for (Data<Number, Number> horizontalMarker : horizontalMarkers) {
            Line line = (Line) horizontalMarker.getNode();
            line.setStartX(0);
            line.setEndX(getBoundsInLocal().getWidth());
            line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
            line.setEndY(line.getStartY());
            line.toFront();
        }
        for (Data<Number, Number> verticalMarker : verticalMarkers) {
            Line line = (Line) verticalMarker.getNode();
            line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5);  // 0.5 for crispness
            line.setEndX(line.getStartX());
            line.setStartY(0d);
            line.setEndY(getBoundsInLocal().getHeight());
            line.toFront();
        }
        for (Data<Number, Number> verticalRangeMarker : verticalRangeMarkers) {
            Rectangle rectangle = (Rectangle) verticalRangeMarker.getNode();
            rectangle.setX( getXAxis().getDisplayPosition(verticalRangeMarker.getXValue()) + 0.5);  // 0.5 for crispness
            rectangle.setWidth( getXAxis().getDisplayPosition(verticalRangeMarker.getYValue()) - getXAxis().getDisplayPosition(verticalRangeMarker.getXValue()));
            rectangle.setY(0d);
            rectangle.setHeight(getBoundsInLocal().getHeight());
            rectangle.toBack();
            if (verticalRangeMarker.getExtraValue() != null) {
                Text text = (Text) verticalRangeMarker.getExtraValue();
                text.setText(String.format("%1.2f : %1.2f",
                        verticalRangeMarker.getXValue().doubleValue(),
                        verticalRangeMarker.getYValue().doubleValue()));
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

    public void addVerticalRangeMarker(Data<Number, Number> marker, Color color, boolean notated) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalRangeMarkers.contains(marker)) return;

        Rectangle rectangle = new Rectangle(0,0,0,0);
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setFill(color.deriveColor(1, 1, 1, 0.2));

        marker.setNode(rectangle);

        if (notated) {
            Text ll = new Text("<>");
            ll.setFill(Color.BLACK);
            marker.setExtraValue(ll);
            getPlotChildren().add(ll);
        }
        getPlotChildren().add(rectangle);
        verticalRangeMarkers.add(marker);
        setMouseTransparentToEverythingButBackground();
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

    public void removeVerticalRangeMarker(Data<Number, Number> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            if (marker.getExtraValue() != null) {
                getPlotChildren().remove(marker.getExtraValue());
            }
            marker.setNode(null);
            marker.setExtraValue(null);
        }
        verticalRangeMarkers.remove(marker);
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
