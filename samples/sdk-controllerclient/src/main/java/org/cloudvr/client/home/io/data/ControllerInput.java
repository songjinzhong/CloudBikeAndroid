package org.cloudvr.client.home.io.data;

import java.nio.ByteBuffer;

/**
 * Created by Hepsilion on 2017/1/13.
 */
public class ControllerInput implements GameInput<ControllerState> {
    private static ControllerInput ourInstance=new ControllerInput();

    private ByteBuffer byteBuffer = ByteBuffer.allocate(PAYLOAD_SIZE);

    private ControllerInput(){}

    public static ControllerInput getInstance(){
        return ourInstance;
    }

    @Override
    public byte getType() {
        return CONTROLLER;
    }

    @Override
    public ByteBuffer getPayload() {
        return byteBuffer;
    }

    @Override
    public GameInput putPayload(ControllerState payload) {
        byteBuffer.clear();
        byteBuffer
//                .putLong(payload.getTimestamp())
//                .putFloat(payload.getOrientation().x)
//                .putFloat(payload.getOrientation().y)
//                .putFloat(payload.getOrientation().z)
//                .putFloat(payload.getOrientation().w)
//                .putInt(payload.isTouching()? 1:0)
                .putFloat(payload.isTouching()?payload.getTouch().x:0.5f)   //x
                //.putFloat(payload.isTouching()?payload.getTouch().y:0.5f)
                .putFloat(payload.isAppButtonState()? 1:payload.isHomeButtonState()? -1:0)//s
                .putFloat(payload.isVolumeUpButtonState()? 1:payload.isVolumeDownButtonState()? -1:0);//c
        //      .putInt(payload.isClickButtonState()? 1:0)
        return ourInstance;
    }
}
