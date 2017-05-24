package org.cloudvr.client.home.io.data;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * Defines a game input
 *
 * @author Pierfrancesco Soffritti
 */
public interface GameInput<T> {
    /**
     *  Payloads of different {@link GameInputType} must be of the same size.
     */
    int PAYLOAD_SIZE = 4*4;

    byte GYRO = 0;
    byte TOUCH = 1;
    byte SPEED = 2;
    byte RESOLUTION = 3;
    byte TURN = 4;
    byte CONTROLLER = 5;

    /**
     * Defines the different inputs accepted by the game.
     */
    @IntDef({GYRO, TOUCH, SPEED, RESOLUTION, TURN, CONTROLLER})
    @Retention(RetentionPolicy.SOURCE)
    @interface GameInputType {}

    @GameInputType byte getType();
    ByteBuffer getPayload();
    GameInput<T> putPayload(T payload);
}