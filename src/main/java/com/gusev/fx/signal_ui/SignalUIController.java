package com.gusev.fx.signal_ui;

import com.gusev.data.ExtendedDataLine;
import com.gusev.data.Mark;
import com.gusev.fx.data.DataFXManager;
import com.gusev.fx.data.DynamicDataFXManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class SignalUIController implements Initializable {
    @FXML private ToggleButton tool_hide1;
    @FXML private SplitPane split;
    @FXML private ToggleButton tool_hide;
    @FXML private BorderPane board;
    @FXML private ToggleButton tool_pause;
    @FXML private Button cut;
    @FXML private Button stop;
    @FXML private ToggleButton tool_sel;
    @FXML private ToggleButton tool_uni_sel;
    @FXML private Button comm;
    @FXML private Button clr_comm;
    @FXML private Button save;
    @FXML private Button load;
    @FXML private ToggleButton pars;
    @FXML private ToggleButton filt_pars;
    @FXML private ToggleButton comm_pars;
    @FXML private ToggleButton graph_pars;
    @FXML private ToggleButton pars1;
    @FXML private ToggleButton filt_pars1;
    @FXML private ToggleButton comm_pars1;
    @FXML private ToggleButton graph_pars1;
    @FXML private VBox p_mini;
    @FXML private VBox p_graph;
    @FXML private VBox p_tool;
    @FXML private HBox p_top;
    @FXML private HBox p_top0;
    @FXML private VBox p_top1;
    @FXML private VBox p_top2;
    @FXML private VBox p_top3;

    private GroupLineChart lcwm;
    private GroupLineChart lcwm_small;
    private Runnable onChangeSelection;
    private Runnable onStop;
    protected DataFXManager datafx;
    protected Stage stage = null;
    private Parent controlTool = null;
    private Parent filterTool = null;
    private Parent marksTool = null;
    private Parent graphTool = null;
    private FilterController filterCtrl = null;
    private GraphController graphCtrl = null;
    private MarksController marksCtrl = null;
    private ResourceBundle resources;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        filterTool = loadFilterControl();
        marksTool = loadMarksControl();
        graphTool = loadGraphControl();
        new ToggleGroup().getToggles().addAll(pars, filt_pars, comm_pars, graph_pars);
        new ToggleGroup().getToggles().addAll(tool_sel, tool_uni_sel);
        tool_sel.setSelected(true);
        p_top.getChildren().remove(p_top0);
        pars1.selectedProperty().bindBidirectional(pars.selectedProperty());
        filt_pars1.selectedProperty().bindBidirectional(filt_pars.selectedProperty());
        comm_pars1.selectedProperty().bindBidirectional(comm_pars.selectedProperty());
        graph_pars1.selectedProperty().bindBidirectional(graph_pars.selectedProperty());
        tool_hide1.selectedProperty().bindBidirectional(tool_hide.selectedProperty());
    }

    public XYChart.Data<Number, Number> getOverviewRange() {
        return lcwm_small.getSelectedRange();
    }

    private void reLCWM(int[] mini, int[] full, Integer[] configurationChannels, String[] labels) {
        lcwm = new GroupLineChart(full, labels, true, true, resources);
        lcwm_small = new GroupLineChart(mini, false, false, resources);
        lcwm_small.setOnChangeSelection(()->{
            XYChart.Data<Number, Number> rangeMarker = lcwm_small.getSelectedRange();
            if (onChangeSelection != null)
                onChangeSelection.run();
            if (datafx != null) {
                datafx.setView(rangeMarker);
            }
        });
        datafx.setSwapper(configurationChannels);
        lcwm.setOnChangeMode(()->{
            if (datafx != null) {
                GroupLineChart.Chart[] crt = lcwm.getCharts();
                for (int i = 0; i < crt.length; i++) {
                    switch (crt[i].getMode()) {
                        case FILTERED_FOURIER:
                            datafx.setMode(i, ExtendedDataLine.Mode.FILTERED_FOURIER);
                            break;
                        case FOURIER:
                            datafx.setMode(i, ExtendedDataLine.Mode.FOURIER);
                            break;
                        case FILTER:
                            datafx.setMode(i, ExtendedDataLine.Mode.FILTER);
                            break;
                        case POWER:
                            datafx.setMode(i, ExtendedDataLine.Mode.POWER);
                            break;
                        case USUAL:
                        default:
                            datafx.setMode(i, ExtendedDataLine.Mode.USUAL);
                            break;
                    }
                }
                XYChart.Data<Number, Number> rangeMarker = lcwm_small.getSelectedRange();
                if (rangeMarker == null)
                    rangeMarker = new XYChart.Data<>(0,
                            datafx.getActiveView(0, ExtendedDataLine.Mode.USUAL));
                datafx.setView(rangeMarker);
            }
        });
        p_graph.getChildren().clear();
        p_graph.getChildren().add(lcwm);
        p_mini.getChildren().clear();
        p_mini.getChildren().add(lcwm_small);
        cut.setDisable(false);
        tool_pause.setDisable(true);
        stop.setDisable(true);
        if (datafx instanceof DynamicDataFXManager) {
            tool_pause.setDisable(false);
            stop.setDisable(false);
        }
        lcwm.clear();
        lcwm_small.clear();
        datafx.bindSeriesOverview(lcwm_small);
        datafx.bindSeriesView(lcwm);
        marksCtrl.setData(this.datafx.getMarks());
    }

    public void bind(int[] mini, int[] full, String[] labels, DataFXManager datafx) {
        graphCtrl.setConfiguration(full, datafx.getDataLabel());
        p_mini.getChildren().clear();
        p_graph.getChildren().clear();
        board.setRight(controlTool);
        this.datafx = datafx;
        reLCWM(mini, full, datafx.getSwapper(), labels);
    }

    public void OnLoad(ActionEvent actionEvent) {
    }

    public void setOnChangeSelection(Runnable on) {
        onChangeSelection = on;
    }

    public void setOnStop(Runnable on) {
        onStop = on;
    }

    public void setControlTool(Parent tool) {
        controlTool = tool;
        board.setRight(controlTool);
    }

    public void setDynamicView(Parent view) {
        board.setLeft(view);
    }

    public void OnChangeToolMulti(ActionEvent actionEvent) {
        setMultiSelectMode();
    }

    public void OnChangeToolUni(ActionEvent actionEvent) {
        setUniSelectMode();
    }

    public void OnMarkArea(ActionEvent actionEvent) {
    }

    public void OnClearMarks(ActionEvent actionEvent) {
        datafx.clearMarks();
    }

    public void OnSaveFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON data files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File("./"));
        //Show save file dialog
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                this.datafx.saveToFile(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void OnLoadFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON data files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File("./"));
        //Show open file dialog
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                datafx = new DataFXManager(file.getAbsolutePath());
                bind(new int[]{1, 1, 1, 1, 1, 1, 1, 1}, new int[]{1, 1, 1, 1, 1, 1, 1, 1},
                        new String[]{"", "", "", "", "", "", "", ""},
                        datafx);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMultiSelectMode() {
        lcwm_small.setCurrent(GroupLineChart.Tool.GROUP_SELECTOR);
        lcwm.setCurrent(GroupLineChart.Tool.GROUP_SELECTOR);
        cut.setDisable(false);
    }

    public void setUniSelectMode() {
        lcwm_small.setCurrent(GroupLineChart.Tool.GROUP_SELECTOR);
        lcwm.setCurrent(GroupLineChart.Tool.UNI_SELECTOR);
        cut.setDisable(true);
    }

    public void setOnlineMode() {
        lcwm_small.setCurrent(GroupLineChart.Tool.DISABLED);
        lcwm.setCurrent(GroupLineChart.Tool.DISABLED);
        cut.setDisable(true);
    }

    public void OnPause(ActionEvent actionEvent) {
        if (((ToggleButton)actionEvent.getSource()).isSelected()) {
            datafx.pause();
            setMultiSelectMode();
        } else {
            datafx.start();
            setOnlineMode();
        }
    }

    public void OnStopData(ActionEvent actionEvent) {
        datafx.pause();
        this.tool_pause.setDisable(true);
        this.stop.setDisable(true);
        setMultiSelectMode();
        if (onStop != null) {
            onStop.run();
        }
    }

    public void OnCutData(ActionEvent actionEvent) {
        XYChart.Data<Number, Number> rangeMarker = lcwm_small.getSelectedRange();
        if (rangeMarker.getXValue().intValue() < rangeMarker.getYValue().intValue())
            datafx.cut(rangeMarker);
    }

    private Parent loadFilterControl() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setResources(resources);
            InputStream is = this.getClass().getResource("/fxml/control_filter.fxml").openStream();
            Pane pane = fxmlLoader.load(is);
            filterCtrl = fxmlLoader.<FilterController>getController();
            filterCtrl.setOnUpdate(()->{
                datafx.setFilterGlobal(filterCtrl.getFilter());
            });
            return pane;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Parent loadGraphControl() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setResources(resources);
            InputStream is = this.getClass().getResource("/fxml/control_graph.fxml").openStream();
            Pane pane = fxmlLoader.load(is);
            graphCtrl = fxmlLoader.<GraphController>getController();
            graphCtrl.setOnUpdate(()->{
                reLCWM(graphCtrl.getConfiguration(), graphCtrl.getConfiguration(),
                        graphCtrl.getConfigurationChannels(), graphCtrl.getConfigurationLabels());
            });
            return pane;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Parent loadMarksControl() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setResources(resources);
            InputStream is = this.getClass().getResource("/fxml/control_marks.fxml").openStream();
            Pane pane = fxmlLoader.load(is);
            marksCtrl = fxmlLoader.<MarksController>getController();
            marksCtrl.setOnSelection(()->{
                Mark mm = marksCtrl.getSelectedMark();
                XYChart.Data<Number, Number> rangeMarker = new XYChart.Data<Number, Number>(mm.start, mm.finish);
                onChangeSelection.run();
                if (datafx != null) {
                    datafx.setView(rangeMarker);
                }
            });
            marksCtrl.setOnUpdate(()->{
                XYChart.Data<Number, Number> rangeMarker = lcwm.getSelectedRange();
                Object[] channelsSelected = lcwm.getSelectedChannels();
                if (channelsSelected == null) {
                    Mark mm = marksCtrl.getNewMark();
                    datafx.addGlobalMark(rangeMarker, mm.name, mm.color, mm.label_color);
                } else {
                    for (Object i : channelsSelected) {
                        Mark mm = marksCtrl.getNewMark();
                        datafx.addMark((Integer) i, rangeMarker, mm.name, mm.color, mm.label_color);
                    }
                }
                marksCtrl.setData(datafx.getMarks());
                datafx.resetMarks(lcwm);
                datafx.resetMarks(lcwm_small);
            });
            marksCtrl.setOnClear(()->{
                datafx.clearMarks();
                marksCtrl.setData(datafx.getMarks());
            });
            return pane;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void hideOverview() {
        tool_hide.setSelected(true);
        p_top.getChildren().clear();
        p_top.getChildren().addAll(p_top0);
        split.setDividerPosition(0, 0.);
    }

    public void OnHide(ActionEvent actionEvent) {
        p_top.getChildren().clear();
        if (((ToggleButton)actionEvent.getSource()).isSelected()) {
            p_top.getChildren().addAll(p_top0);
            split.setDividerPosition(0, 0.);
        } else {
            p_top.getChildren().addAll(p_top1, p_top2, p_mini, p_top3);
            split.setDividerPosition(0, 0.25);
        }
    }

    public void OnGraphParams(ActionEvent actionEvent) {
        if (((ToggleButton)actionEvent.getSource()).isSelected()) {
            board.setRight(graphTool);
        } else {
            board.setRight(null);
        }
    }

    public void OnPanelParams(ActionEvent actionEvent) {
        if (((ToggleButton)actionEvent.getSource()).isSelected()) {
            board.setRight(controlTool);
        } else {
            board.setRight(null);
        }
    }

    public void OnFilterParams(ActionEvent actionEvent) {
        if (((ToggleButton)actionEvent.getSource()).isSelected()) {
            board.setRight(filterTool);
        } else {
            board.setRight(null);
        }
    }

    public void OnMarksParams(ActionEvent actionEvent) {
        if (((ToggleButton)actionEvent.getSource()).isSelected()) {
            board.setRight(marksTool);
        } else {
            board.setRight(null);
        }
    }
}