package gh.out386.lamp.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by J on 11/9/2017.
 */

public class RequestRunnable implements Runnable {
    private String data;

    public RequestRunnable(String data) {
        this.data = data;
    }

    @Override
    public void run() {
        sendData(data);
    }

    public void sendData(String data) {
        InetAddress ip = DatagramSingleton.getIP();
        DatagramSocket socket = DatagramSingleton.getSocket();
        if (ip == null || socket == null || data == null) {
            return;
        }

        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                ip, DatagramSingleton.SERVER_PORT);

        try {
            socket.send(packet);
        } catch(IOException ignored) {
        }
    }
}
