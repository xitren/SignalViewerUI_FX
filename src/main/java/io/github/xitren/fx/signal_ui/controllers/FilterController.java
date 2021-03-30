package io.github.xitren.fx.signal_ui.controllers;

import io.github.xitren.data.FIRUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;

public class FilterController implements Initializable {
    @FXML private LineChart graph_sample;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label discrete;
    @FXML private Slider ft_len;
    @FXML private Slider ft_low;
    @FXML private Slider ft_high;

    private XYChart.Series<Integer, Integer> series = new XYChart.Series<>();
    private ResourceBundle resources;
    private Runnable upd;
    private double[] filter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        graph_sample.setLegendVisible(false);
        graph_sample.setCreateSymbols(false);
        ft_len.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> source, Number oldValue, Number newValue) {
                updatePreview();
            }
        });
        ft_low.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> source, Number oldValue, Number newValue) {
                updatePreview();
            }
        });
        ft_high.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> source, Number oldValue, Number newValue) {
                updatePreview();
            }
        });
        graph_sample.getData().add(series);
        updatePreview();
    }

    public double[] getFilter() {
        return filter;
    }

    private void updatePreview() {
        series.getData().clear();
        int di = 0;
        int[] dd = new int[3];
        for (int d : dd)
            d = 0;
        for (Integer i = 0; i <= 100; i += 1) {
            int mid = 0;
            for (int d = 0; d < 3; d++)
                mid += dd[d];
            XYChart.Data<Integer, Integer> point;
            if ((i.doubleValue() < ft_low.getValue())
                    || (ft_high.getValue() < i.doubleValue()))
                point = new XYChart.Data(i, (mid + 0) / (di + 1));
            else
                point = new XYChart.Data(i, (mid + 100) / (di + 1));
            series.getData().add(point);
            if (di < 3)
                dd[di++] = point.getYValue();
            else {
                dd[0] = dd[1];
                dd[1] = dd[2];
                dd[2] = point.getYValue();
            }
        }
    }

    public void OnLoad(ActionEvent actionEvent) {
        filter = FIRUtils.createBandpass((int)ft_len.getValue(), ft_low.getValue(), ft_high.getValue(), 250);
        upd.run();
    }

    public void setOnUpdate(Runnable upd) {
        this.upd = upd;
    }
}
