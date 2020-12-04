package com.gusev.fx.signal_ui;

import com.gusev.data.Mark;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MarksController implements Initializable {
    @FXML private ColorPicker selection_color;
    @FXML private ColorPicker label_color;
    @FXML private TextField Name;
    @FXML private TableColumn<Mark, String> col_color;
    @FXML private TableColumn<Mark, String> col_label_color;
    @FXML private TableColumn<Mark, String> col_name;
    @FXML private TableView<Mark> marks;

    private Mark selected_mark = null;
    private Runnable onSelection;
    private ResourceBundle bundle;
    private Runnable onUpdate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;
        col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        col_label_color.setCellValueFactory(new PropertyValueFactory<>("webLabelColor"));
        col_label_color.setCellFactory(e -> new TableCell<Mark, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Color c;
                if (item == null)
                    c = Color.WHITE;
                else
                    c = Color.web(item);
                BackgroundFill background_fill = new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY);
                this.setBackground(new Background(background_fill));
            }
        });
        col_color.setCellValueFactory(new PropertyValueFactory<>("webColor"));
        col_color.setCellFactory(e -> new TableCell<Mark, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Color c;
                if (item == null)
                    c = Color.WHITE;
                else
                    c = Color.web(item);
                BackgroundFill background_fill = new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY);
                this.setBackground(new Background(background_fill));
            }
        });
    }

    public void setData(List<Mark> data) {
        marks.setItems(FXCollections.observableArrayList(data));
    }

    public Mark getSelectedMark() {
        return selected_mark;
    }

    public void setOnSelection(Runnable onSelection) {
        this.onSelection = onSelection;
    }

    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void OnLoad(ActionEvent actionEvent) {
    }

    public void OnClear(ActionEvent actionEvent) {
    }
}
