import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 51234);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String input = "";
        String serverMessage = "";
        Scanner sc = new Scanner(System.in);

        while (true) {
            if (input.equals("exit") || (serverMessage != null && serverMessage.equals("exit"))) {
                break;
            } else {
                // 한 번에 29639개의 메시지(ln)만 보낼 수 있음
                System.out.print("입력: ");
                input = sc.nextLine();
                out.println(input);
                serverMessage = in.readLine();
                System.out.println("Server: " + serverMessage);
            }
        }

        socket.close();
    }
}