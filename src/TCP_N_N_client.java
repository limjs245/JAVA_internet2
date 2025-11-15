import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Scanner;

// 메인 class
public class TCP_N_N_client {
    // main 함수
    // 실행 시 port scanning 진행
    // send와 receive를 별도의 스레드로 분리
    public static void main(String[] args) {
        try {
            // port scanning
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

// send class
// thread
class Send implements Runnable {
    Socket socket;
    PrintWriter out;
    Scanner sc;

    public Send(Socket socket) throws Exception {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.sc = new Scanner(System.in);
    }

    // 입력 받기 루프
    // '/exit' 입력 시 socket 종료
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

// receive class
// thread
class Receive implements Runnable {
    Socket socket;
    BufferedReader in;

    public Receive(Socket socket) throws Exception {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // 메시지 받기 루프
    // 메시지가 null이면 socket 종료 시도 후 코드 종료
    @Override
    public void run() {
        String message;

        while (true) {
            try {
                message = in.readLine();
                if (message == null) {
                    System.out.println("===== server closed =====");
                    try {
                        socket.close();
                    }  catch (Exception e1) {
                        System.out.println(">>> error: " + e1.getMessage());
                        break;
                    }
                    System.exit(0);
                    break;
                }
                System.out.println(message);
            } catch (Exception e) {
                System.out.println(">>> error: " + e.getMessage());
                try {
                    socket.close();
                }  catch (Exception e1) {
                    System.out.println(">>> error: " + e1.getMessage());
                    break;
                }
                break;
            }
        }
    }
}

// port scanning:
// 같은 wifi 내에서 열린 port를 찾아서 연결하는 기능
// -> 모든 host id를 탐색
// -> 각 탐색 시간은 200ms
// 1. 실행 시 getLocalIp 함수를 호출하여 ip 주소를 얻음
// 2. ip 주소에서 network id를 추출
// 3. 일일이 host id를 대입해서 열린 port를 탐색
// 4. 열린 port가 없으면 종료
// host id에서 0과 255는 다른 용도이므로 제외
class TCP_PortScanning_2 {
    final static int PORT = 51235;
    final static int maxHostId = 254;
    static String fullIp = getLocalIp();
    static String networkId = fullIp.substring(0, fullIp.lastIndexOf('.') + 1);
    final static int timeout = 200;

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

        if (failCount == maxHostId) {
            System.out.println(">> There is no open communication.");
            return null;
        }
        return null;
    }

    // ip 주소를 얻는 함수
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