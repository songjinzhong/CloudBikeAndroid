package org.cloudvr.client.home.io.connections;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.cloudvr.client.controller.ImageGzip;
import org.cloudvr.client.home.io.data.GameInput;
import org.cloudvr.client.home.logging.LoggerBus;

import java.io.IOException;
import java.nio.ByteBuffer;

import rx.Observable;
import rx.functions.Action1;

/**
 * This class is responsible for sending/receiving data to/from the server, on a TCP connection.
 *
 * @author Pierfrancesco Soffritti
 */
public class ServerTCP extends AbstractServerTCP implements ServerIO {
    protected final String LOG_TAG = getClass().getSimpleName();
    public static final int DEFAULT_PORT = 2099;

    /**
     * Creates a ServerConnection object and opens the TCP connection with the server, with the corresponding Input and Output streams.
     * @param ip the server IP
     * @param port the server PORT
     */
    public ServerTCP(String ip, int port) throws IOException {
        super(ip, port);
    }

    @Override
    public void sendScreenResolution(int screenWidth, int screenHeight) throws IOException {
        outputStream.writeInt(screenWidth);
        outputStream.writeInt(screenHeight);
    }

    @Override
    public void sendGameInput(GameInput gameInput) throws IOException {
        outputStream.write(gameInput.getType());
        outputStream.write(gameInput.getPayload().array());
    }

    @Override
    public Observable<Bitmap> getServerOutput() {
        Observable.OnSubscribe<Bitmap> onSubscribe = subscriber -> {
            try {
                // receive images
                int dim;
                int d1;
                while ((dim = inputStream.readInt()) > 0) {
                    byte[] img = new byte[dim];
                    inputStream.readFully(img, 0, dim);
                    //d1 = img.length;
                    //System.out.println("-------" + img.length);
                    //img = ImageGzip.uncompress(img);
                    //System.out.println(">------" + (img.length - d1));
                    // notify the subscribers that a new image is ready
                    subscriber.onNext(BitmapFactory.decodeByteArray(img, 0, img.length));
                }
            } catch (Exception e) {
                LoggerBus.getInstance().post(new LoggerBus.Log("Exception: " + e.getClass(), LOG_TAG, LoggerBus.Log.ERROR));
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
                disconnect();
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public Action1<GameInput> getServerInput() {
        return gameInput -> {
            try {
                outputStream.write(gameInput.getType());
                outputStream.write(gameInput.getPayload().array());
            } catch (Exception e) {
                throw new RuntimeException("Can't send game input", e);
            }
        };
    }
}