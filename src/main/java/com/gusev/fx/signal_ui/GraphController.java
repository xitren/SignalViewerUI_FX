package com.gusev.fx.signal_ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class GraphController implements Initializable {
    @FXML private CheckBox graph_hide_1;
    @FXML private Label graph_label_1;
    @FXML private ChoiceBox<Integer> graph_pos_1;
    @FXML private CheckBox graph_hide_2;
    @FXML private Label graph_label_2;
    @FXML private ChoiceBox<Integer> graph_pos_2;
    @FXML private CheckBox graph_hide_3;
    @FXML private Label graph_label_3;
    @FXML private ChoiceBox<Integer> graph_pos_3;
    @FXML private CheckBox graph_hide_4;
    @FXML private Label graph_label_4;
    @FXML private ChoiceBox<Integer> graph_pos_4;
    @FXML private CheckBox graph_hide_5;
    @FXML private Label graph_label_5;
    @FXML private ChoiceBox<Integer> graph_pos_5;
    @FXML private CheckBox graph_hide_6;
    @FXML private Label graph_label_6;
    @FXML private ChoiceBox<Integer> graph_pos_6;
    @FXML private CheckBox graph_hide_7;
    @FXML private Label graph_label_7;
    @FXML private ChoiceBox<Integer> graph_pos_7;
    @FXML private CheckBox graph_hide_8;
    @FXML private Label graph_label_8;
    @FXML private ChoiceBox<Integer> graph_pos_8;

    private ResourceBundle resources;
    private Runnable upd;
    private CheckBox[] graph_hide;
    private Label[] graph_label;
    private ChoiceBox<Integer>[] graph_pos;
    private Integer[] graphs;
    private int[] channels = new int[8];
    private String[] str;
    private List<String> strP = new LinkedList<>();
    private String[][] mapStr = new String[8][8];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        graphs = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8};
        graph_hide = new CheckBox[]{graph_hide_1, graph_hide_2, graph_hide_3, graph_hide_4,
                graph_hide_5, graph_hide_6, graph_hide_7, graph_hide_8};
        graph_pos = new ChoiceBox[]{graph_pos_1, graph_pos_2, graph_pos_3, graph_pos_4,
                graph_pos_5, graph_pos_6, graph_pos_7, graph_pos_8};
        graph_label = new Label[]{graph_label_1, graph_label_2, graph_label_3, graph_label_4,
                graph_label_5, graph_label_6, graph_label_7, graph_label_8};
        for (int i = 0;i < graph_pos.length;i++) {
            channels[i] = 0;
            graph_pos[i].getItems().clear();
            graph_pos[i].getItems().addAll(graphs);
            graph_pos[i].setValue(graphs[i]);
            graph_pos[i].disableProperty().bind(graph_hide[i].selectedProperty());
        }
    }

    public void setOnUpdate(Runnable upd) {
        this.upd = upd;
    }

    public int[] getConfiguration() {
        strP.clear();
        for (int i = 0;i < channels.length;i++) {
            channels[i] = 0;
            for (int j = 0;j < 8;j++)
                mapStr[i][j] = null;
        }
        for (int i = 0;i < graph_pos.length;i++) {
            if (!graph_hide[i].isSelected()) {
                mapStr[graph_pos[i].getValue() - 1][channels[graph_pos[i].getValue() - 1]] = str[i];
                channels[graph_pos[i].getValue() - 1]++;
            }
        }
        for (int i = 0;i < channels.length;i++) {
            for (int j = 0;j < 8;j++)
                if (mapStr[i][j] != null)
                    strP.add(mapStr[i][j]);
        }
        return channels;
    }

    public String[] getConfigurationLabels() {
        String[] ret = new String[strP.size()];
        return strP.toArray(ret);
    }

    public void setConfiguration(int[] conf, String[] str) {
        int size = 0;
        for (int h : conf) {
            size += h;
        }
        if (size != str.length)
            throw new IndexOutOfBoundsException("Found more/not enough labels than expected!");
        this.str = str;
        int k = 0;
        for (int i = 0;i < conf.length && i < graph_hide.length;i++) {
            for (int j = 0;j < conf[i];j++) {
                graph_hide[k].setSelected(false);
                graph_pos[k].setValue(i + 1);
                graph_label[k].setText(str[k]);
                k++;
            }
        }
        for (;k < graph_hide.length;k++) {
            graph_hide[k].setSelected(true);
            graph_pos[k].setValue(1);
        }
    }

    public void OnLoad(ActionEvent actionEvent) {
        if (this.upd != null)
            this.upd.run();
    }
}
