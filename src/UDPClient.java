import java.net.*;

public class UDPClient {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();

        String message = "Hello, Server!";
        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, address, 12345);
        socket.send(packet);

        byte[] buffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(responsePacket);

        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        System.out.println("Server: " + response);

        socket.close();
    }
}
