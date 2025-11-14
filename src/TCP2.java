import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TCP2 {
    public static void main(String[] args) {
        try {
            TCPServer2 tcpServer = new TCPServer2();
            TCPClient2 tcpClient = new TCPClient2();
            Thread tcpServerThread = new Thread(tcpServer);
            Thread tcpClientThread = new Thread(tcpClient);
            tcpServerThread.start();
            tcpClientThread.start();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
    }
}

class TCPServer2 implements Runnable {
    public TCPServer2() throws Exception {}

    private ServerSocket serverSocket = null;
    private Socket socket = null;

    private void openServerSocket() throws Exception {
        serverSocket = new ServerSocket(51235);
        socket = serverSocket.accept();
    }

    private void outputCommunication() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String clientMessage = in.readLine();
        System.out.println("user2: " + clientMessage);

        while (true) {
            if (clientMessage != null && clientMessage.equals("exit")) {
                break;
            } else {
                clientMessage = in.readLine();
                System.out.println("user2: " + clientMessage);
            }
        }

        socket.close();
        serverSocket.close();
    }

    @Override
    public void run() {
        try {
            openServerSocket();
            System.out.println("socket2: connected");
            outputCommunication();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class TCPClient2 implements Runnable {
    public TCPClient2() {}

    private Socket socket = null;

    private void connectSocket() throws Exception{
        socket = new Socket("localhost", 51234);
    }

    private void inputCommunication() throws Exception {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String input = "";
        Scanner sc = new Scanner(System.in);

        while (true) {
            if (input.equals("exit")) {
                break;
            } else {
                input = sc.nextLine();
                out.println(input);
            }
        }

        socket.close();
    }

    @Override
    public void run() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                connectSocket();
                scheduler.shutdownNow();
                System.out.println("socket1: connected");
                inputCommunication();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(task, 1, 5, TimeUnit.SECONDS);
    }
}