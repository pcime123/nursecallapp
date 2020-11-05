package com.sscctv.nursecallapp.ui.settings;

import android.util.Log;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class DemoClient implements Runnable {

    private static final String TAG = DemoClient.class.getSimpleName();
    private static final int PORT = 59009;
    private String ip, str;

    public DemoClient(String ip, String str) {
        this.ip = ip;
        this.str = str;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(ip, PORT);

            Log.w(TAG, "<<  Demo Client running... >>");
            Log.i(TAG, "<<  IP: " + ip + " Port: " + PORT + "  >>");

            byte[] buf;
            String ip = str;
            buf = ip.getBytes();

            OutputStream os =socket.getOutputStream();
            os.write(buf, 0, buf.length);
            Log.w(TAG, "<<  Discovery Client send: " + str + " >>");

            os.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
