package org.cloudvr.client.home.io.data;

import java.nio.ByteBuffer;

/**
 * Created by Hepsilion on 2017/2/14.
 */
public class ResolutionInput implements GameInput<ControllerState> {
    private static ResolutionInput ourInstance=new ResolutionInput();

    private ByteBuffer byteBuffer = ByteBuffer.allocate(PAYLOAD_SIZE);

    private ResolutionInput(){}

    public static ResolutionInput getInstance(){
        return ourInstance;
    }

    @Override
    public byte getType() {
        return RESOLUTION;
    }

    @Override
    public ByteBuffer getPayload() {
        return byteBuffer;
    }

    @Override
    public GameInput putPayload(ControllerState payload) {
        byteBuffer.clear();
        byteBuffer.putFloat(payload.isVolumeUpButtonState()? 1:payload.isVolumeDownButtonState()? -1:0);
        return ourInstance;
    }
}
