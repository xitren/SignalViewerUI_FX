package com.github.xitren.fx.signal_ui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;

import java.net.URL;
import java.util.ResourceBundle;

public class GraphController implements Initializable {
    @FXML private ChoiceBox<Integer> graph_positions;
    @FXML private ChoiceBox<String> graph_pos_1;
    @FXML private ChoiceBox<String> graph_pos_2;
    @FXML private ChoiceBox<String> graph_pos_3;
    @FXML private ChoiceBox<String> graph_pos_4;
    @FXML private ChoiceBox<String> graph_pos_5;
    @FXML private ChoiceBox<String> graph_pos_6;
    @FXML private ChoiceBox<String> graph_pos_7;
    @FXML private ChoiceBox<String> graph_pos_8;
    @FXML private ChoiceBox<Integer> graph_chart_1;
    @FXML private ChoiceBox<Integer> graph_chart_2;
    @FXML private ChoiceBox<Integer> graph_chart_3;
    @FXML private ChoiceBox<Integer> graph_chart_4;
    @FXML private ChoiceBox<Integer> graph_chart_5;
    @FXML private ChoiceBox<Integer> graph_chart_6;
    @FXML private ChoiceBox<Integer> graph_chart_7;
    @FXML private ChoiceBox<Integer> graph_chart_8;

    private final Integer[] graphs = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8};
    private final Integer[][] charts = new Integer[][]{
            {},
            {1},
            {1, 2},
            {1, 2, 3},
            {1, 2, 3, 4},
            {1, 2, 3, 4, 5},
            {1, 2, 3, 4, 5, 6},
            {1, 2, 3, 4, 5, 6, 7},
            {1, 2, 3, 4, 5, 6, 7, 8}};
    private ResourceBundle resources;
    private Runnable upd;
    private ChoiceBox<String>[] graph_pos;
    private ChoiceBox<Integer>[] graph_chart;
    private int[] channels;
    private String[] str;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        str = new String[]{""};
        graph_positions.getItems().addAll(graphs);
        graph_pos = new ChoiceBox[]{graph_pos_1, graph_pos_2, graph_pos_3, graph_pos_4,
                graph_pos_5, graph_pos_6, graph_pos_7, graph_pos_8};
        graph_chart = new ChoiceBox[]{graph_chart_1, graph_chart_2, graph_chart_3, graph_chart_4,
                graph_chart_5, graph_chart_6, graph_chart_7, graph_chart_8};
        graph_positions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            for (int i = 0;i < graph_pos.length;i++) {
                graph_pos[i].setDisable(true);
                graph_chart[i].setDisable(true);
                graph_chart[i].getItems().clear();
                graph_chart[i].getItems().addAll(charts[newValue]);
                graph_chart[i].getSelectionModel().select(i);
            }
            switch (newValue) {
                case 8:
                    graph_pos_8.setDisable(false);
//                    graph_chart_8.setDisable(false);
                case 7:
                    graph_pos_7.setDisable(false);
//                    graph_chart_7.setDisable(false);
                case 6:
                    graph_pos_6.setDisable(false);
//                    graph_chart_6.setDisable(false);
                case 5:
                    graph_pos_5.setDisable(false);
//                    graph_chart_5.setDisable(false);
                case 4:
                    graph_pos_4.setDisable(false);
//                    graph_chart_4.setDisable(false);
                case 3:
                    graph_pos_3.setDisable(false);
//                    graph_chart_3.setDisable(false);
                case 2:
                    graph_pos_2.setDisable(false);
//                    graph_chart_2.setDisable(false);
                case 1:
                    graph_pos_1.setDisable(false);
//                    graph_chart_1.setDisable(false);
                    break;
                default:
                    break;
            }
        });
        graph_positions.getSelectionModel().select(7);
    }

    public void setOnUpdate(Runnable upd) {
        this.upd = upd;
    }

    public int[] getConfiguration() {
        channels = new int[graph_positions.getValue()];
        for (int i = 0;i < channels.length;i++) {
            channels[i] = 0;
        }
        for (int i = 0;i < channels.length;i++) {
            channels[graph_chart[i].getSelectionModel().getSelectedIndex()]++;
        }
        return channels;
    }

    public Integer[] getConfigurationChannels() {
        Integer[] switcher = new Integer[graph_positions.getValue()];
        for (int i = 0;i < switcher.length;i++) {
            switcher[i] = graph_pos[i].getSelectionModel().getSelectedIndex();
        }
        return switcher;
    }

    public String[] getConfigurationLabels() {
        String[] ret = new String[graph_positions.getValue()];
        for (int i = 0;i < ret.length;i++) {
            ret[i] = str[graph_pos[i].getSelectionModel().getSelectedIndex()];
        }
        return ret;
    }

    public void setConfiguration(int[] conf, String[] str) {
        int size = 0;
        for (int h : conf) {
            size += h;
        }
        if (size > str.length)
            throw new IndexOutOfBoundsException("Found not enough labels than expected!");
        graph_positions.getSelectionModel().select(str.length - 1);
        this.str = str;
        for (int i = 0;i < graph_pos.length;i++) {
            graph_pos[i].getItems().clear();
            graph_pos[i].getItems().addAll(str);
        }
        for (int i = 0;i < graph_pos.length;i++) {
            graph_chart[i].getSelectionModel().select(i);
            if (i < str.length) {
                graph_pos[i].setValue(str[i]);
            } else {
                graph_pos[i].setValue(str[0]);
            }
        }
    }

    public void OnLoad(ActionEvent actionEvent) {
        if (this.upd != null)
            this.upd.run();
    }
}
