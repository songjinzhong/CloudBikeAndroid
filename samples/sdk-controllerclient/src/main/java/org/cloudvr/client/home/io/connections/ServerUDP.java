package org.cloudvr.client.home.io.connections;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.cloudvr.client.home.event.EventBus;
import org.cloudvr.client.home.event.Events;
import org.cloudvr.client.home.io.data.GameInput;
import org.cloudvr.client.home.logging.LoggerBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import rx.Observable;
import rx.functions.Action1;

/**
 * This class represents a UPD endpoint to the server.<br/><br/>
 * <b>No form of quality/reliability control is implemented.</b><br/>
 * The current scope of the application is to work in a LAN, so it's good enough.
 * For communication outside of a LAN a better protocol may be needed.
 *
 * @author Pierfrancesco Soffritti
 */
public class ServerUDP implements ServerIO {
    protected final String LOG_TAG = getClass().getSimpleName();

    private final static int SOCKET_TIMEOUT = 2000;

    private SocketAddress serverAddress;

    private DatagramSocket socket;
    private DatagramPacket inputPacket;
    private DatagramPacket outputPacket;

    /**
     * Create and instance of a UDP endpoint to the server.<br/>
     * The UDP server utilizes two ports. One for initiating new sessions and the other for inter-session communications.
     * @param serverIP the IP of the server.
     * @param initSessionsPort the port on which the server is listening for new sessions.
     * @throws IOException if is not possible to establish a session with the server.
     */
    public ServerUDP(String serverIP, int initSessionsPort) throws  IOException {
        socket = new DatagramSocket();
        socket.setSoTimeout(SOCKET_TIMEOUT);

        EventBus.getInstance().post(new Events.ServerConnecting());

        sendInitPacket(serverIP, initSessionsPort);
    }

    /**
     * the init packet will initiate a session with the server.
     * @param serverIP the IP of the server.
     * @param initSessionsPort the port on which the server is listening for new sessions.
     * @throws IOException if, after 3 attempts, no answer is received.
     */
    private void sendInitPacket(String serverIP, int initSessionsPort) throws IOException {
        int attempts = 0;
        while (true) {
            DatagramPacket initPacket = new DatagramPacket(new byte[0], 0, new InetSocketAddress(InetAddress.getByName(serverIP), initSessionsPort));
            socket.send(initPacket);
            try {
                socket.receive(initPacket);
                // init endpoints
                serverAddress = new InetSocketAddress(initPacket.getAddress(), initPacket.getPort());
                outputPacket = new DatagramPacket(new byte[GameInput.PAYLOAD_SIZE], GameInput.PAYLOAD_SIZE, serverAddress);
                inputPacket = new DatagramPacket(new byte[100000], 100000, serverAddress);
                break;
            } catch (SocketTimeoutException e) {
                attempts++;
                if (attempts >= 3) {
                    disconnect();
                    throw new IOException("can't connect");
                }
            }
        }
    }

    @Override
    public void sendScreenResolution(int screenWidth, int screenHeight) throws IOException {
        int attempts = 0;

        byte[] resolution = ByteBuffer.allocate(8).putInt(screenWidth).putInt(screenHeight).array();
        while (true) {
            DatagramPacket resolutionPacket = new DatagramPacket(resolution, resolution.length, serverAddress);
            socket.send(resolutionPacket);
            try {
                socket.receive(resolutionPacket);
                Log.d(LOG_TAG, "res");
                break;
            } catch (SocketTimeoutException e) {
                Log.d(LOG_TAG, "res timeout");
                attempts ++;
                if(attempts >= 3)
                    throw new IOException("can't send resolution");
            }
        }
    }

    @Override
    public void sendGameInput(GameInput gameInput) throws IOException {

    }

    @Override
    public Observable<Bitmap> getServerOutput() {
        Observable.OnSubscribe<Bitmap> onSubscribe = subscriber -> {
            try {
                while(true) {
                    socket.receive(inputPacket);
                    byte[] img;

                    ByteBuffer wrapped = ByteBuffer.wrap(inputPacket.getData());
                    int length = wrapped.getInt();
                    img = new byte[length];
                    wrapped.get(img, 0, length);
                    subscriber.onNext(BitmapFactory.decodeByteArray(img, 0, img.length));
                }
            } catch (Exception e) {
                if(e instanceof SocketTimeoutException)
                    disconnect();

                LoggerBus.getInstance().post(new LoggerBus.Log("Exception: " + e.getClass(), LOG_TAG, LoggerBus.Log.ERROR));
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public Action1<GameInput> getServerInput() {
        byte[] data = new byte[1+GameInput.PAYLOAD_SIZE];
        return gameInput -> {
            try {
                data[0] = gameInput.getType();
                for(int i = 0; i<GameInput.PAYLOAD_SIZE; i++)
                    data[i+1] = gameInput.getPayload().get(i);
                outputPacket.setData(data);
                socket.send(outputPacket);
            } catch (Exception e) {
                throw new RuntimeException("Can't send game input", e);
            }
        };
    }

    @Override
    public void disconnect() {
        socket.disconnect();
        socket.close();

        LoggerBus.getInstance().post(new LoggerBus.Log("Ended connection with server.", LOG_TAG));
        EventBus.getInstance().post(new Events.ServerDisconnected());
    }
}