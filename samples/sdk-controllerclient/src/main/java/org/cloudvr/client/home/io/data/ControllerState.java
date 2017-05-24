package org.cloudvr.client.home.io.data;

import android.graphics.PointF;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.Orientation;

/**
 * Created by Hepsilion on 2017/1/13.
 */
public class ControllerState {
    private long timestamp;
    private Orientation orientation;
    private boolean isTouching;
    private PointF touch;
    private boolean appButtonState;
    private boolean homeButtonState;
    private boolean clickButtonState;
    private boolean volumeUpButtonState;
    private boolean volumeDownButtonState;
    private float turnPos;

    private static ControllerState controllerState=new ControllerState();

    private ControllerState(){};

    public static ControllerState getInstance(Controller controller){
        controllerState.setTimestamp(controller.timestamp);
        controllerState.setOrientation(controller.orientation);
        controllerState.setTouching(controller.isTouching);
        controllerState.setTouch(controller.touch);
        controllerState.setAppButtonState(controller.appButtonState);
        controllerState.setHomeButtonState(controller.homeButtonState);
        controllerState.setClickButtonState(controller.clickButtonState);
        controllerState.setVolumeUpButtonState(controller.volumeUpButtonState);
        controllerState.setVolumeDownButtonState(controller.volumeDownButtonState);

        controllerState.setTurnPos(controller.touch.x);
        return controllerState;
    }

    public static ControllerState getIdleInstance(Controller controller){
        controllerState.setTimestamp(controller.timestamp);
        controllerState.setOrientation(controller.orientation);
        controllerState.setTouching(false);
        controllerState.setTouch(controller.touch);
        controllerState.setAppButtonState(false);
        controllerState.setHomeButtonState(false);
        controllerState.setClickButtonState(controller.clickButtonState);
        controllerState.setVolumeUpButtonState(false);
        controllerState.setVolumeDownButtonState(false);

        controllerState.setTurnPos(0.5f);
        return controllerState;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public boolean isTouching() {
        return isTouching;
    }

    public void setTouching(boolean touching) {
        isTouching = touching;
    }

    public PointF getTouch() {
        return touch;
    }

    public void setTouch(PointF touch) {
        this.touch = touch;
    }

    public boolean isAppButtonState() {
        return appButtonState;
    }

    public void setAppButtonState(boolean appButtonState) {
        this.appButtonState = appButtonState;
    }

    public boolean isHomeButtonState() {
        return homeButtonState;
    }

    public void setHomeButtonState(boolean homeButtonState) {
        this.homeButtonState = homeButtonState;
    }

    public boolean isClickButtonState() {
        return clickButtonState;
    }

    public void setClickButtonState(boolean clickButtonState) {
        this.clickButtonState = clickButtonState;
    }

    public boolean isVolumeUpButtonState() {
        return volumeUpButtonState;
    }

    public void setVolumeUpButtonState(boolean volumeUpButtonState) {
        this.volumeUpButtonState = volumeUpButtonState;
    }

    public boolean isVolumeDownButtonState() {
        return volumeDownButtonState;
    }

    public void setVolumeDownButtonState(boolean volumeDownButtonState) {
        this.volumeDownButtonState = volumeDownButtonState;
    }

    public float getTurnPos() {
        return turnPos;
    }

    public void setTurnPos(float turnPos) {
        this.turnPos = turnPos;
    }
}