package com.github.xitren.fx.signal_ui.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class RECController implements Initializable {
    @FXML private TextField value;
    @FXML private Label name;
    @FXML private Circle back;
    @FXML private Circle selector;
    private DoubleProperty mValue = new SimpleDoubleProperty(5.0);
    private DoubleProperty mMin = new SimpleDoubleProperty(0.0);
    private DoubleProperty mMax = new SimpleDoubleProperty(29.9);
    private DoubleProperty mTurn = new SimpleDoubleProperty(10.0);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rebuildView();
        selector.setMouseTransparent(true);
    }

    public void OnToZero(ActionEvent actionEvent) {
        mValue.setValue(0.0);
        rebuildView();
    }

    private void rebuildView() {
        if (mMin.doubleValue() > mValue.doubleValue())
            mValue.setValue(mMin.doubleValue());
        if (mMax.doubleValue() < mValue.doubleValue())
            mValue.setValue(mMax.doubleValue());
        double vv = (mValue.doubleValue() % mTurn.doubleValue());
        double angle = Math.toRadians((vv * 360 / mTurn.doubleValue()));
        double x_center = Math.cos(angle);
        double y_center = Math.sin(angle);
        selector.setCenterX(back.getCenterX() + x_center * back.getRadius() * 0.6);
        selector.setCenterY(back.getCenterX() + y_center * back.getRadius() * 0.6);
        value.setText(String.format("%1.2f", mValue.doubleValue()));
    }

    public void OnMousePressed(MouseEvent mouseEvent) {
        double x = back.getRadius() - mouseEvent.getX();
        double y = back.getRadius() - mouseEvent.getY();
        double angle, mv;
        angle = getAngel(x, y);
        mv = angle * mTurn.doubleValue() / 360;
        mValue.setValue(mv);
        rebuildView();
    }

    public void OnMouseMoved(MouseEvent mouseEvent) {
        double x = back.getRadius() - mouseEvent.getX();
        double y = back.getRadius() - mouseEvent.getY();
        double angle, mv, diff;
        angle = getAngel(x, y);
        mv = angle * mTurn.doubleValue() / 360;
        if ((4 == getQuarter(0, mValue.getValue() % mTurn.doubleValue(), mTurn.doubleValue()))
                && (1 == getQuarter(0, mv, mTurn.doubleValue()))) {
            diff = mv + mTurn.doubleValue() - mValue.getValue() % mTurn.doubleValue();
            mValue.setValue(mValue.getValue() + diff);
        } else if ((1 == getQuarter(0, mValue.getValue() % mTurn.doubleValue(), mTurn.doubleValue()))
                && (4 == getQuarter(0, mv, mTurn.doubleValue()))) {
            diff = mv - mTurn.doubleValue() - mValue.getValue() % mTurn.doubleValue();
            mValue.setValue(mValue.getValue() + diff);
        } else {
            diff = mv - mValue.getValue() % mTurn.doubleValue();
            mValue.setValue(mValue.getValue() + diff);
        }
        rebuildView();
    }

    private double getAngel(double x, double y) {
        double angle;
        if (y >= 0) {
            angle = Math.toDegrees(Math.atan2(y, x)) + 180;
        } else {
            angle = Math.toDegrees(Math.atan2(-y, -x));
        }
        return angle;
    }

    private static int getQuarter(double min, double val, double max) {
        double size = max - min;
        if ((min <= val) && (val < (size / 4)))
            return 1;
        else if (((size / 4) <= val) && (val < (size * 2 / 4)))
            return 2;
        else if (((size * 2 / 4) <= val) && (val < (size * 3 / 4)))
            return 3;
        else
            return 4;
    }

    public void OnKey(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            try {
                mValue.setValue(Double.parseDouble(value.getText().replace(',','.')));
                rebuildView();
            } catch (NumberFormatException ex) {
                System.out.println(ex);
            }
        }
    }
}
