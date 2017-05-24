package org.cloudvr.client.home.io.data;

import java.nio.ByteBuffer;

/**
 * Created by Hepsilion on 2017/2/14.
 */
public class SpeedInput implements GameInput<ControllerState> {
    private static SpeedInput ourInstance=new SpeedInput();

    private ByteBuffer byteBuffer = ByteBuffer.allocate(PAYLOAD_SIZE);

    private SpeedInput(){}

    public static SpeedInput getInstance(){
        return ourInstance;
    }

    @Override
    public byte getType() {
        return SPEED;
    }

    @Override
    public ByteBuffer getPayload() {
        return byteBuffer;
    }

    @Override
    public GameInput putPayload(ControllerState payload) {
        byteBuffer.clear();
        byteBuffer.putFloat(payload.isAppButtonState()? 1:payload.isHomeButtonState()? -1:0);
        return ourInstance;
    }
}
