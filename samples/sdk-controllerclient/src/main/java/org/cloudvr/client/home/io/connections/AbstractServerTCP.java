package org.cloudvr.client.home.io.connections;

import org.cloudvr.client.home.event.EventBus;
import org.cloudvr.client.home.event.Events;
import org.cloudvr.client.home.logging.LoggerBus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * This class is responsible for handling a TCP connection with the server.
 *
 * @author Pierfrancesco Soffritti
 */
public abstract class AbstractServerTCP {
    protected final String LOG_TAG = getClass().getSimpleName();

    protected Socket mSocket;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;

    /**
     * Opens the TCP connection with the server, with the corresponding Input and Output streams.
     * @param ip the server IP
     * @param port the server PORT
     * @throws IOException
     */
    protected AbstractServerTCP(String ip, int port) throws IOException {
        connect(ip, port);
        inputStream = getInputStream();
        outputStream = getOutputStream();
    }

    private void connect(String ip, int port) throws IOException {
        SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
        mSocket = new Socket();
        EventBus.getInstance().post(new Events.ServerConnecting());

        try {
            mSocket.connect(socketAddress, 5000);
        } catch (SocketTimeoutException e) {
            EventBus.getInstance().post(new Events.ServerDisconnected());
            throw new IOException(e);
        }

        LoggerBus.getInstance().post(new LoggerBus.Log("Connected to " + mSocket, LOG_TAG));
    }

    /**
     * close the TCP connection with the server
     */
    public void disconnect() {
        try {
            if(outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if(inputStream != null) {
                inputStream.close();
            }
            mSocket.close();

            onDisconnected();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private DataInputStream getInputStream() throws IOException {
        if(inputStream == null)
            inputStream = new DataInputStream(mSocket.getInputStream());
        return inputStream;
    }

    private DataOutputStream getOutputStream() throws IOException {
        if(outputStream == null)
            outputStream = new DataOutputStream(mSocket.getOutputStream());
        return outputStream;
    }

    /**
     * this method is called automatically after the TCP connection with the server has been closed successfully
     */
    private void onDisconnected() {
        LoggerBus.getInstance().post(new LoggerBus.Log("Ended connection with server.", LOG_TAG));
        EventBus.getInstance().post(new Events.ServerDisconnected());
    }

    public boolean isConnected() {
        return mSocket.isConnected();
    }
}