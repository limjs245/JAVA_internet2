import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCP_N_N_server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(51235)) {
            ClientManagement clientManagement = new ClientManagement();
            System.out.println("open server succeed");
            int clientCount = 0;

            while (true) {
                Socket socket = serverSocket.accept();
                clientCount++;
                try {
                    ClientHandler clientHandler = new ClientHandler(socket, clientManagement, clientCount);
                    clientManagement.addClient(clientHandler);
                    Thread clientTread = new Thread(clientHandler);
                    clientTread.start();
                    System.out.println("New connection established: " + "user" + clientCount);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("error" + e.getMessage());
        }
    }
}

class ClientManagement {
    private final List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();

    public ClientManagement() {}

    public void addClient(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }

    public void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    public void broadcastMessage(String message, ClientHandler clientHandler) {
        for (ClientHandler handler : clientHandlers) {
            if (handler ==  clientHandler) {
                continue;
            }
            handler.sendMessage(message);
        }
    }
}

class ClientHandler implements Runnable {
    private final ClientManagement clientManagement;
    private final PrintWriter out;
    private final BufferedReader in;
    public int id;

    public ClientHandler(Socket socket, ClientManagement clientManagement, int id) throws Exception {
        this.clientManagement = clientManagement;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.id = id;
        clientManagement.broadcastMessage("user" + id + " accessed.", this);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void getMessage() {
        while (true) {
            try {
                String message = in.readLine();
                if (message == null || message.equals("exit")) {
                    System.out.println("user" + id + " left.");
                    clientManagement.broadcastMessage("user" + id + " left.", this);
                    clientManagement.removeClient(this);
                    break;
                }
                System.out.println("user" + id + " > " + message);
                clientManagement.broadcastMessage("user" + id + " > " + message, this);
            } catch (Exception e) {
                System.out.println("user" + id + " left.");
                clientManagement.broadcastMessage("user" + id + " left.", this);
                clientManagement.removeClient(this);
                break;
            }
        }
    }

    @Override
    public void run() {
        try {
            getMessage();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}