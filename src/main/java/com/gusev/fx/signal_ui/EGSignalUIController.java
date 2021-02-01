package com.gusev.fx.signal_ui;

import javafx.event.ActionEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class EGSignalUIController extends SignalUIController {

    private Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    @Override
    public void OnSaveFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        //Set extension filter for text files
        FileChooser.ExtensionFilter extJSONFilter =
                new FileChooser.ExtensionFilter("JSON data files (*.json)", "*.json");
        FileChooser.ExtensionFilter extTXTFilter =
                new FileChooser.ExtensionFilter("TXT data files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().addAll(extJSONFilter, extTXTFilter);
        fileChooser.setInitialDirectory(new File("./"));
        //Show save file dialog
        File file = fileChooser.showSaveDialog(stage);
        Optional<String> ext = getExtensionByStringHandling(file.getName());
        try {
            if (file != null && ext.isPresent()) {
                if (ext.get().equals("txt")) {
                    this.datafx.saveToTXT(file.getAbsolutePath());
                } else {
                    this.datafx.saveToFile(file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
