package org.cloudvr.client.home.io.data;

import java.nio.ByteBuffer;

/**
 * Created by Hepsilion on 2017/2/14.
 */
public class TurnInput implements GameInput<ControllerState> {
    private static TurnInput ourInstance=new TurnInput();

    private ByteBuffer byteBuffer = ByteBuffer.allocate(PAYLOAD_SIZE);

    private TurnInput(){}

    public static TurnInput getInstance(){
        return ourInstance;
    }

    @Override
    public byte getType() {
        return TURN;
    }

    @Override
    public ByteBuffer getPayload() {
        return byteBuffer;
    }

    @Override
    public GameInput putPayload(ControllerState payload) {
        byteBuffer.clear();
        byteBuffer.putFloat(payload.isTouching()? payload.getTurnPos() :0.5f);
        return ourInstance;
    }
}
