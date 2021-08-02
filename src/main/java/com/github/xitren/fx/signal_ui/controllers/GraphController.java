package com.github.xitren.fx.signal_ui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class GraphController implements Initializable {
    @FXML private ChoiceBox<Integer> graph_positions;
    @FXML private VBox vbox_1;
    @FXML private VBox vbox_2;
    @FXML private VBox vbox_3;
    @FXML private VBox vbox_4;
    @FXML private VBox vbox_5;
    @FXML private VBox vbox_6;
    @FXML private VBox vbox_7;
    @FXML private VBox vbox_8;
    @FXML private VBox vbox_main;
    @FXML private Label graph_chart_1;
    @FXML private Label graph_chart_2;
    @FXML private Label graph_chart_3;
    @FXML private Label graph_chart_4;
    @FXML private Label graph_chart_5;
    @FXML private Label graph_chart_6;
    @FXML private Label graph_chart_7;
    @FXML private Label graph_chart_8;
    @FXML private ChoiceBox<Integer> graph_pos_1_1;
    @FXML private ChoiceBox<Integer> graph_pos_2_1;
    @FXML private ChoiceBox<Integer> graph_pos_3_1;
    @FXML private ChoiceBox<Integer> graph_pos_4_1;
    @FXML private ChoiceBox<Integer> graph_pos_5_1;
    @FXML private ChoiceBox<Integer> graph_pos_6_1;
    @FXML private ChoiceBox<Integer> graph_pos_7_1;
    @FXML private ChoiceBox<Integer> graph_pos_8_1;
    @FXML private ChoiceBox<Integer> graph_pos_1_2;
    @FXML private ChoiceBox<Integer> graph_pos_2_2;
    @FXML private ChoiceBox<Integer> graph_pos_3_2;
    @FXML private ChoiceBox<Integer> graph_pos_4_2;
    @FXML private ChoiceBox<Integer> graph_pos_5_2;
    @FXML private ChoiceBox<Integer> graph_pos_6_2;
    @FXML private ChoiceBox<Integer> graph_pos_7_2;
    @FXML private ChoiceBox<Integer> graph_pos_8_2;
    @FXML private ChoiceBox<Integer> graph_pos_1_3;
    @FXML private ChoiceBox<Integer> graph_pos_2_3;
    @FXML private ChoiceBox<Integer> graph_pos_3_3;
    @FXML private ChoiceBox<Integer> graph_pos_4_3;
    @FXML private ChoiceBox<Integer> graph_pos_5_3;
    @FXML private ChoiceBox<Integer> graph_pos_6_3;
    @FXML private ChoiceBox<Integer> graph_pos_7_3;
    @FXML private ChoiceBox<Integer> graph_pos_8_3;
    @FXML private ChoiceBox<Integer> graph_pos_1_4;
    @FXML private ChoiceBox<Integer> graph_pos_2_4;
    @FXML private ChoiceBox<Integer> graph_pos_3_4;
    @FXML private ChoiceBox<Integer> graph_pos_4_4;
    @FXML private ChoiceBox<Integer> graph_pos_5_4;
    @FXML private ChoiceBox<Integer> graph_pos_6_4;
    @FXML private ChoiceBox<Integer> graph_pos_7_4;
    @FXML private ChoiceBox<Integer> graph_pos_8_4;
    @FXML private ChoiceBox<Integer> graph_pos_1_5;
    @FXML private ChoiceBox<Integer> graph_pos_2_5;
    @FXML private ChoiceBox<Integer> graph_pos_3_5;
    @FXML private ChoiceBox<Integer> graph_pos_4_5;
    @FXML private ChoiceBox<Integer> graph_pos_5_5;
    @FXML private ChoiceBox<Integer> graph_pos_6_5;
    @FXML private ChoiceBox<Integer> graph_pos_7_5;
    @FXML private ChoiceBox<Integer> graph_pos_8_5;
    @FXML private ChoiceBox<Integer> graph_pos_1_6;
    @FXML private ChoiceBox<Integer> graph_pos_2_6;
    @FXML private ChoiceBox<Integer> graph_pos_3_6;
    @FXML private ChoiceBox<Integer> graph_pos_4_6;
    @FXML private ChoiceBox<Integer> graph_pos_5_6;
    @FXML private ChoiceBox<Integer> graph_pos_6_6;
    @FXML private ChoiceBox<Integer> graph_pos_7_6;
    @FXML private ChoiceBox<Integer> graph_pos_8_6;
    @FXML private ChoiceBox<Integer> graph_pos_1_7;
    @FXML private ChoiceBox<Integer> graph_pos_2_7;
    @FXML private ChoiceBox<Integer> graph_pos_3_7;
    @FXML private ChoiceBox<Integer> graph_pos_4_7;
    @FXML private ChoiceBox<Integer> graph_pos_5_7;
    @FXML private ChoiceBox<Integer> graph_pos_6_7;
    @FXML private ChoiceBox<Integer> graph_pos_7_7;
    @FXML private ChoiceBox<Integer> graph_pos_8_7;
    @FXML private ChoiceBox<Integer> graph_pos_1_8;
    @FXML private ChoiceBox<Integer> graph_pos_2_8;
    @FXML private ChoiceBox<Integer> graph_pos_3_8;
    @FXML private ChoiceBox<Integer> graph_pos_4_8;
    @FXML private ChoiceBox<Integer> graph_pos_5_8;
    @FXML private ChoiceBox<Integer> graph_pos_6_8;
    @FXML private ChoiceBox<Integer> graph_pos_7_8;
    @FXML private ChoiceBox<Integer> graph_pos_8_8;

    private final Integer[] graphs = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8};
    private ResourceBundle resources;
    private Runnable upd;
    private ChoiceBox<String>[][] graph_pos;
    private VBox[] graph_chart;
    private int[][] channels;
    private int[] selector;
    private String[] str;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        str = new String[]{""};
//        vbox_main.getChildren().add(loadRECControl());
        graph_positions.getItems().addAll(graphs);
        graph_chart = new VBox[]{vbox_1, vbox_2, vbox_3, vbox_4, vbox_5, vbox_6, vbox_7, vbox_8};
        graph_pos = new ChoiceBox[][]{
                {graph_pos_1_1, graph_pos_2_1, graph_pos_3_1, graph_pos_4_1, graph_pos_5_1, graph_pos_6_1, graph_pos_7_1, graph_pos_8_1},
                {graph_pos_1_2, graph_pos_2_2, graph_pos_3_2, graph_pos_4_2, graph_pos_5_2, graph_pos_6_2, graph_pos_7_2, graph_pos_8_2},
                {graph_pos_1_3, graph_pos_2_3, graph_pos_3_3, graph_pos_4_3, graph_pos_5_3, graph_pos_6_3, graph_pos_7_3, graph_pos_8_3},
                {graph_pos_1_4, graph_pos_2_4, graph_pos_3_4, graph_pos_4_4, graph_pos_5_4, graph_pos_6_4, graph_pos_7_4, graph_pos_8_4},
                {graph_pos_1_5, graph_pos_2_5, graph_pos_3_5, graph_pos_4_5, graph_pos_5_5, graph_pos_6_5, graph_pos_7_5, graph_pos_8_5},
                {graph_pos_1_6, graph_pos_2_6, graph_pos_3_6, graph_pos_4_6, graph_pos_5_6, graph_pos_6_6, graph_pos_7_6, graph_pos_8_6},
                {graph_pos_1_7, graph_pos_2_7, graph_pos_3_7, graph_pos_4_7, graph_pos_5_7, graph_pos_6_7, graph_pos_7_7, graph_pos_8_7},
                {graph_pos_1_8, graph_pos_2_8, graph_pos_3_8, graph_pos_4_8, graph_pos_5_8, graph_pos_6_8, graph_pos_7_8, graph_pos_8_8}
        };
        for (int i = 0;i < graph_pos.length;i++) {
            for (int j = 0;j < (graph_pos[i].length - 1);j++) {
                final ChoiceBox<String> f = graph_pos[i][j + 1];
                final ChoiceBox<String> fl = graph_pos[i][j + 1];
                final int fi = i;
                graph_pos[i][j + 1].disableProperty().addListener((a, b, v)->{
                    if (v) {
                        graph_chart[fi].getChildren().remove(fl);
                    } else {
                        if (!graph_chart[fi].getChildren().contains(fl))
                            graph_chart[fi].getChildren().add(fl);
                    }
                });
                graph_pos[i][j + 1].disableProperty().bind(graph_pos[i][j].getSelectionModel().selectedIndexProperty().greaterThan(0).not());
                graph_pos[i][j].getSelectionModel().selectedIndexProperty().addListener((a, b, v)->{
                    if (v.intValue() <= 0) {
                        f.getSelectionModel().select(0);
                    }
                });
            }
        }
        graph_positions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            vbox_main.getChildren().removeAll(graph_chart);
            for (int i = 0;i < newValue;i++) {
                vbox_main.getChildren().add(graph_chart[i]);
            }
        });
        graph_positions.getSelectionModel().select(7);
        rebuild();
    }

    private void rebuild() {
        for (int i = 0;i < graph_pos.length;i++) {
            for (int j = 1; j < (graph_pos[i].length); j++) {
                if (graph_pos[i][j].isDisabled())
                    graph_chart[i].getChildren().remove(graph_pos[i][j]);
            }
        }
    }

    private Parent loadRECControl() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setResources(resources);
            InputStream is = this.getClass().getResource("/fxml/control_rec.fxml").openStream();
            Pane pane = fxmlLoader.load(is);
            return pane;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void setOnUpdate(Runnable upd) {
        this.upd = upd;
    }

    public int[][] getConfiguration() {
        selector = new int[graph_positions.getValue()];
        channels = new int[selector.length][];
        for (int i = 0;i < selector.length;i++) {
            int j;
            for (j = 0;j < graph_pos.length;j++) {
                if (graph_pos[i][j].getSelectionModel().getSelectedIndex() <= 0) {
                    break;
                }
            }
            selector[i] = j;
            channels[i] = new int[j];
            for (int k = 0;k < channels[i].length;k++) {
                channels[i][k] = graph_pos[i][k].getSelectionModel().getSelectedIndex() - 1;
            }
        }
        return channels;
    }

    public void setConfiguration(String[] str, int[][] current) {
        this.str = str;
        if (current.length >= 8) {
            graph_positions.setValue(8);
        } else {
            graph_positions.setValue(current.length);
        }
        for (int i = 0;i < graph_pos.length;i++) {
            for (int j = 0;j < graph_pos[i].length;j++) {
                graph_pos[i][j].getItems().clear();
                graph_pos[i][j].getItems().add("-");
                graph_pos[i][j].getItems().addAll(str);
                graph_pos[i][j].getSelectionModel().select(0);
            }
        }
        for (int i = 0;i < current.length && i < graph_pos.length;i++) {
            for (int j = 0;j < current[i].length && i < graph_pos[i].length;j++) {
                graph_pos[i][j].setValue(str[current[i][j]]);
            }
        }
    }

    public void OnLoad(ActionEvent actionEvent) {
        if (this.upd != null)
            this.upd.run();
    }
}
