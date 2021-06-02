package com.github.xitren.fx.signal_ui.controllers;

import com.github.xitren.data.container.DynamicDataContainer;
import com.github.xitren.data.line.OnlineDataLine;
import com.github.xitren.fx.data.DataFXManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class TestRunner extends Application {
    private String windowTitle = "Tester";
    private static Stage stage;
    private ViewHolder view = getMainView();
    protected Timer timer = new Timer(false);
    protected DataFXManager<OnlineDataLine<DynamicDataContainer>, DynamicDataContainer> dataManager;
    private int dd = 0;
    private ResourceBundle rb;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle(windowTitle);
        stage.setScene(new Scene(view.getView()));
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.centerOnScreen();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
        String[] labels = new String[]{"test 1", "test 2", "test 3", "test 4", "test 5", "test 6", "test 7", "test 8"};
        dataManager = DataFXManager.DataFXManagerFactory(rb, labels);
//        dataManager.setSwapper(new Integer[]{0, 1, 2, 3});
        ((SignalUIController)view.getController()).bind(dataManager);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double[][] data = new double[dataManager.size()][16];
                for (int n = 0; n < 16; n++) {
                    double r_pi = 2 * Math.PI * (dd++) / 250;
                    for (int i = 0; i < dataManager.size(); i++) {
                        data[i][n] = 10 * Math.cos(3 * i * r_pi)
                                * 100 * Math.cos(3 * i / 100 * r_pi);
                    }
                }
                dataManager.addData(data);
            }
        }, 0, 16 * 4);
        stage.show();
    }

    public ViewHolder getMainView() {
        return loadView("fxml/signal_ui.fxml");
    }

    /**
     * Самый обыкновенный способ использовать FXML загрузчик.
     * Как раз-таки на этом этапе будет создан объект-контроллер,
     * произведены все FXML инъекции и вызван метод инициализации контроллера.
     */
    protected ViewHolder loadView(String url) {
        Locale loc = new Locale("ru", "RU");
        rb = ResourceBundle.getBundle("ui", loc);
        InputStream fxmlStream = null;
        try {
            try {
                fxmlStream = getClass().getClassLoader().getResourceAsStream(url);
                FXMLLoader loader = new FXMLLoader();
                SignalUIController suic = new SignalUIController();
                loader.setController(suic);
                loader.setResources(rb);
                loader.load(fxmlStream);
                ((Parent)loader.getRoot()).setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        switch (keyEvent.getCode()) {
                            case DIGIT0:
                            case DIGIT1:
                            case DIGIT2:
                            case DIGIT3:
                            case DIGIT4:
                            case DIGIT5:
                            case DIGIT6:
                            case DIGIT7:
                            case DIGIT8:
                            case DIGIT9:
                                dataManager.setCurrentMark(keyEvent.getCode().getName(), "#f2f2f2", "#99cc99");
                                suic.updateMarks();
                                break;
                            default:
                                break;
                        }
                    }
                });
                return new ViewHolder(loader.getRoot(), loader.getController());
            } finally {
                if (fxmlStream != null) {
                    fxmlStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Класс - оболочка: контроллер мы обязаны указать в качестве бина,
     * а view - представление, нам предстоит использовать в точке входа {@link Application}.
     */
    public class ViewHolder {
        private Parent view;
        private Object controller;

        public ViewHolder(Parent view, Object controller) {
            this.view = view;
            this.controller = controller;
        }

        public Parent getView() {
            return view;
        }

        public void setView(Parent view) {
            this.view = view;
        }

        public Object getController() {
            return controller;
        }

        public void setController(Object controller) {
            this.controller = controller;
        }
    }
}
