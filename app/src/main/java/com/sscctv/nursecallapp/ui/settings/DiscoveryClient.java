package com.sscctv.nursecallapp.ui.settings;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DiscoveryClient implements Runnable {

    private static final String TAG = DiscoveryClient.class.getSimpleName();
    private static final String IP = "239.21.218.197";
    private static final int PORT = 1235;
    private String str;

    public DiscoveryClient(String str) {
        this.str = str;
    }

    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(PORT);
            InetAddress byName = InetAddress.getByName(IP);
            socket.joinGroup(byName);

            Log.w(TAG, "<<  Discovery Client running... >>");
            Log.i(TAG, "<<  IP: " + IP + " Port: " + PORT + "  >>");

            byte[] buf;
            String ip = str;
            buf = ip.getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, byName, PORT);
            socket.send(packet);
            Log.w(TAG, "<<  Discovery Client send: " + str + " >>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
