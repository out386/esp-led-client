package gh.out386.lamp.network;

import android.util.Log;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class DatagramSingleton {
    static final int SERVER_PORT = 4258;

    private static DatagramSocket socket;
    private static InetAddress ip;

    private DatagramSingleton() {
    }

    static DatagramSocket getSocket() {
        if (socket == null)
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                //TODO: Propagate the errors to the UI
                Log.d("DatagramSingleton", e.getMessage());
            }

        return socket;
    }

    static void close() {
        if (socket != null)
            socket.close();
    }

    static InetAddress getIP(String serverIp) {
        if (ip == null)
            try {
                ip = InetAddress.getByName(serverIp);
            } catch (UnknownHostException e) {
                //TODO: Propagate the errors to the UI
                Log.d("DatagramSingleton", e.getMessage());
            }
        return ip;
    }
}
