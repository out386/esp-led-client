package gh.out386.lamp.network;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

public class DiscoverSsdp {
    private Thread thread;
    private OnDeviceListener listener;
    private Handler handler = new Handler();

    private class Scanner implements Runnable {
        String searchStr = "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\n" +
                "MAN: \"ssdp:discover\"\r\nMX: 3\r\nST: urn:schemas-upnp-org:device:EspLamp:1\r\n\r\n";
        byte[] outData = searchStr.getBytes();
        byte[] inData = new byte[1024];

        @Override
        public void run() {
            SocketAddress localAddress = new InetSocketAddress(getFirstWiFiAddress(), 0);

            DatagramSocket clientSocket;
            try {
                clientSocket = new DatagramSocket(localAddress);
                clientSocket.setSoTimeout(3000);
            } catch (SocketException e) {
                e.printStackTrace();
                handler.post(() -> listener.onFailure(e));
                return;
            }

            InetSocketAddress remoteAddress = new InetSocketAddress("239.255.255.250", 1900);
            DatagramPacket sendPacket = new DatagramPacket(outData, outData.length, remoteAddress);

            while (!Thread.currentThread().isInterrupted()) {

                DatagramPacket inDataPacket = new DatagramPacket(inData, inData.length);

                try {
                    clientSocket.send(sendPacket);
                    clientSocket.receive(inDataPacket);
                } catch (java.net.SocketTimeoutException e) {
                    Log.i("ssdp", "main: Skip");
                    continue;
                } catch (IOException e) {
                    Log.e("ssdp", "run: ", e);
                }

                //TODO: Use the description.xml in this response
                //String response = new String(inDataPacket.getData());
                //Log.i("ssdp", "main: " + response);
                listener.onDevice(inDataPacket.getAddress().getHostAddress());
            }
            clientSocket.close();
        }
    }

    public void scan(OnDeviceListener listener) {
        this.listener = listener;
        if (thread != null)
            thread.interrupt();

        thread = new Thread(new Scanner());
        thread.start();
    }

    public void stop() {
        if (thread != null)
            thread.interrupt();
    }

    private InetAddress getFirstWiFiAddress() {
        String WIFI_INTERFACE_NAME = "^w.*[0-9]$";
        try {
            Enumeration<NetworkInterface> interfaces;
            for (interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.getName().matches(WIFI_INTERFACE_NAME)) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses;
                for (inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                    InetAddress address = inetAddresses.nextElement();
                    if (!address.isSiteLocalAddress()) {
                        continue;
                    }
                    if (address instanceof Inet4Address) {
                        return address;
                    }
                }
            }
        } catch (SocketException ignored) {

        }
        return null;
    }

    public interface OnDeviceListener {
        void onDevice(String data);

        void onFailure(Exception e);
    }
}
