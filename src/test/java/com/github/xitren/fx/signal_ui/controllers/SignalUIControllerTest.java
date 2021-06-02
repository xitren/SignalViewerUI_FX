package com.github.xitren.fx.signal_ui.controllers;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class SignalUIControllerTest  {

    public SignalUIControllerTest() {
        super();
    }

    @Test
    public void testA() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                new JFXPanel(); // Initializes the JavaFx Platform
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        new TestRunner().start(new Stage()); // Create and
                    }
                });
            }
        });
        thread.start();// Initialize the thread
        Thread.sleep(10000000); // Time to use the app, with out this, the thread
        // will be killed before you can tell.
    }
}