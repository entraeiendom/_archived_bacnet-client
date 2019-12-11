package no.entra.bacnet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

/*
Send Bacnet messages to the network
 */
public class Simulator {
    private static final Logger log = LoggerFactory.getLogger( Simulator.class );
    private DatagramSocket socket;
    public static final int BACNET_DEFAULT_PORT = 47808;
    private byte[] buf = new byte[2048];
    private InetAddress sendToAddress;

    public Simulator() throws SocketException, UnknownHostException {

        socket = new DatagramSocket(null);
        socket.setBroadcast(true);
        socket.setReuseAddress(true);
        SocketAddress inetAddress = new InetSocketAddress(BACNET_DEFAULT_PORT);
        sendToAddress = InetAddress.getByName("255.255.255.255");
        socket.bind(inetAddress);
    }

    public static void main(String[] args) {
        Simulator simulator = null;
        try {
            simulator = new Simulator();
            simulator.sendPeriodically(1);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            simulator.disconnect();
        }

    }

    private void disconnect() {
        if (socket != null && socket.isConnected()) {
            socket.disconnect();
            socket = null;
        }
    }

    void sendPeriodically(int everyNthSecond) throws IOException {
        String hexString = "810400180a3f510cbac00120ffff00ff10080a07ae1a07ae";
        do {
            buf = hexStringToByteArray(hexString);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, sendToAddress, BACNET_DEFAULT_PORT);
            log.debug("Sending: {}", packet);
            socket.send(packet);
            try {
                Thread.sleep(everyNthSecond * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);

    }

    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
}
