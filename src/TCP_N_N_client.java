import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Scanner;

public class TCP_N_N_client {
    public static void main(String[] args) {
        try {
            Socket socket = TCP_PortScanning_2.portScanning();
            System.out.println("===== Connected to server =====");

            if (socket != null && socket.isConnected()) {
                Send send = new Send(socket);
                Receive receive = new Receive(socket);
                Thread sendThread = new Thread(send);
                Thread receiveThread = new Thread(receive);
                sendThread.start();
                receiveThread.start();
                sendThread.join();
            }
        } catch (Exception e) {
            System.out.println(">>> error" + e.getMessage());
        }
    }
}

class Send implements Runnable {
    Socket socket;
    PrintWriter out;
    Scanner sc;

    public Send(Socket socket) throws Exception {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.sc = new Scanner(System.in);
    }

    @Override
    public void run() {
        String input;

        while (true) {
            try {
                input = sc.nextLine();
                out.println(input);
                if (input.equals("/exit")) {
                    sc.close();
                    try {
                        socket.close();
                        System.out.println("===== Socket closed =====");
                    }  catch (Exception e) {
                        System.out.println(">>> error: " + e.getMessage());
                    }
                    break;
                }
            } catch (Exception e) {
                System.out.println(">>> error: " + e.getMessage());
                break;
            }
        }
    }
}

class Receive implements Runnable {
    BufferedReader in;

    public Receive(Socket socket) throws Exception {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        String message;

        while (true) {
            try {
                message = in.readLine();
                if (message == null) {
                    System.out.println("===== server closed =====");
                    break;
                }
                System.out.println(message);
            } catch (Exception e) {
                System.out.println(">>> error: " + e.getMessage());
                break;
            }
        }
    }
}

class TCP_PortScanning_2 {
    final static int PORT = 51235;
    final static int maxHostId = 254;
    static String fullIp = getLocalIp();
    static String networkId = fullIp.substring(0, fullIp.lastIndexOf('.') + 1);
    final static int timeout = 100;

    public static Socket portScanning() throws Exception {
        System.out.println(">> Network Id: " + networkId);

        int i;
        int failCount = 0;

        for(i = 1; i <= maxHostId; i++) {
            Socket tempSocket = new Socket();
            System.out.println(">> trying to connect to " + networkId + i);

            try {
                tempSocket.connect(new InetSocketAddress(networkId + i, PORT), timeout);
                return tempSocket;
            } catch (Exception e) {
                tempSocket.close();
                failCount++;
            }
        }

        if (failCount == 256) {
            System.out.println(">> There is no open communication.");
            return null;
        }
        return null;
    }

    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            return ">>> error" + e.getMessage();
        }
        return ">> No Site Local Address Found";
    }
}