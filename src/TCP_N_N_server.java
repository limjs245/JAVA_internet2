import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

// 메인 class
public class TCP_N_N_server {
    public static boolean isRunning = true;
    // main 함수
    public static void main(String[] args) {
        // port: 51235
        try (ServerSocket serverSocket = new ServerSocket(51235); Scanner scanner = new Scanner(System.in)) {
            ClientManagement clientManagement = new ClientManagement();
            Running running = new Running(scanner, serverSocket, clientManagement);
            Thread runningThread = new Thread(running);
            runningThread.start();
            System.out.println("===== open server succeed =====");
            // client id는 클라이언트 접속 카운트로 지정
            // client가 접속을 끊었다 다시 접속 시 id 변경
            int clientCount = 0;

            while (isRunning) {
                Socket socket = serverSocket.accept();
                clientCount++;
                try {
                    ClientHandler clientHandler = new ClientHandler(socket, clientManagement, clientCount);
                    Thread clientTread = new Thread(clientHandler);
                    clientTread.start();
                    System.out.println(">> New connection established <<");
                } catch (Exception e) {
                    System.out.println(">>> error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(">>> error" + e.getMessage());
        }
    }
}

// client 관리 class
// thread가 아님
// thread 공유 리스트가 두개: clientHandlers 리스트, 유저 닉네임 리스트(Set)
// synchronized 키워드 관련은 배우지 않아서 AI 도움을 받음
class ClientManagement {
    private final List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();
    private final Set<String> userNicknameList = new HashSet<>();
    private final Object nicknameLock = new Object();

    public ClientManagement() {}

    // synchronized 키워드로 안전하게 clientHandler, nickname 추가
    // null이나 공백, 스페이스면 false 반환
    // 중복이어도 false 반환
    public boolean tryAddUser(ClientHandler clientHandler, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false;
        }
        String trimmedNickname = nickname.trim();
        synchronized (nicknameLock) {
            if (userNicknameList.contains(trimmedNickname)) {
                return false;
            }
            userNicknameList.add(trimmedNickname);
            clientHandlers.add(clientHandler);
        }
        return true;
    }

    // synchronized 키워드로 안전하게 리스트 받기
    public List<String> getUserNicknameList() {
        synchronized (nicknameLock) {
            return new ArrayList<>(userNicknameList);
        }
    }

    // synchronized 키워드로 안전하게 clientHandler와 해당 유저 닉네임 모두 삭제
    public void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        String nickname = clientHandler.nickname;
        try {
            if (nickname != null && !nickname.trim().isEmpty()) {
                synchronized (nicknameLock) {
                    userNicknameList.remove(nickname);
                }
            }
        }  catch (Exception e) {
            System.out.println(">>> error: " + e.getMessage());
        }
    }

    // clientHandlers 리스트에 있는 모든 client에게 메시지를 전송
    public void broadcastMessage(String message, ClientHandler clientHandler) {
        for (ClientHandler handler : clientHandlers) {
            if (handler ==  clientHandler) {
                continue;
            }
            handler.sendMessage(message);
        }
    }

    // 모든 client의 closeSocket 메소드 실행
    public void closeAllClients() {
        for (ClientHandler handler : clientHandlers) {
            handler.closeSocket();
        }
    }
}

// client 관리 class
// thread
// client가 server에 접속 시 새 스레드로 생성해 관리
class ClientHandler implements Runnable {
    private final Socket socket;
    private final ClientManagement clientManagement;
    private final PrintWriter out;
    private final BufferedReader in;
    public int id;
    public String nickname = "";

    public ClientHandler(Socket socket, ClientManagement clientManagement, int id) throws Exception {
        this.socket = socket;
        this.clientManagement = clientManagement;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.id = id;
    }

    // 해당 client에게 메시지 전송
    public void sendMessage(String message) {
        out.println(message);
    }

    // 해당 client의 메시지를 한 번 받기
    // 처음 유저 닉네임 받기 용도
    public String getMessageOnce() {
        try {
            return in.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    // 지속적으로 메시지를 받기 위한 루프
    // client가 '/exit' 입력 시 client 목록에서 client와 닉네임 삭제 및 해당 client와의 socket 종료
    // client가 '/list' 입력 시 client nickname 목록 전송
    private void getMessage() {
        while (true) {
            try {
                String message = in.readLine();
                if (message == null || message.equals("/exit")) {
                    System.out.println("===== " + nickname + " left =====");
                    clientManagement.broadcastMessage("===== " + nickname + " left =====", this);
                    clientManagement.removeClient(this);
                    closeSocket();
                    break;
                }
                if (message.equals("/list")) {
                    List<String> userNicknameList = clientManagement.getUserNicknameList();
                    sendMessage("===== user list =====");
                    for (String nickname : userNicknameList) {
                        sendMessage(nickname);
                    }
                    sendMessage("=====================");
                } else {
                    System.out.println(nickname + " > " + message);
                    clientManagement.broadcastMessage(nickname + " > " + message, this);
                }
            } catch (Exception e) {
                System.out.println("===== " + nickname + " left =====");
                clientManagement.broadcastMessage("===== " + nickname + " left =====", this);
                clientManagement.removeClient(this);
                closeSocket();
                break;
            }
        }
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println(">>> error: " + e.getMessage());
        }
    }

    // run 함수
    // 접속 시 clientManagement class의 tryAddUser 메소드 호출
    // 닉네임 입력 후 접속 메시지가 전 유저에게 전송
    @Override
    public void run() {
        while (true) {
            sendMessage(">>> Please set the nickname: ");
            String tempNickname = getMessageOnce();
            if (tempNickname == null) {
                System.out.println(">>> client disconnected");
                closeSocket();
                return;
            }

            if (clientManagement.tryAddUser(this, tempNickname)) {
                this.nickname = tempNickname.trim();
                break;
            } else  {
                System.out.println(">>> wrong nickname");
            }
        }
        System.out.println("===== " + nickname + " accessed =====");
        clientManagement.broadcastMessage("===== " + nickname + " accessed =====", this);
        try {
            getMessage();
        } catch (Exception e) {
            System.out.println(">>> error: " + e.getMessage());
        }
    }
}

// 서버 진행 중지를 위한 Running class
// thread로 작동
// '/exit' 입력 시 서버 종료
class Running implements Runnable {
    Scanner scanner;
    ServerSocket serverSocket;
    ClientManagement clientManagement;

    public Running(Scanner scanner, ServerSocket serverSocket, ClientManagement clientManagement) {
        this.scanner = scanner;
        this.serverSocket = serverSocket;
        this.clientManagement = clientManagement;
    }

    // '/exit' 입력 시:
    // 1. scanner 종료
    // 2. clientManagement class로 모든 client socket 닫기
    // 3. serverSocket 종료
    @Override
    public void run() {
        while (true) {
            String message = scanner.nextLine();
            if (message.equals("/exit")) {
                try {
                    TCP_N_N_server.isRunning = false;
                    scanner.close();
                    clientManagement.closeAllClients();
                    serverSocket.close();
                    System.out.println("===== server closed =====");
                    break;
                } catch (Exception e) {
                    System.out.println(">>> error: " + e.getMessage());
                }
            }
        }
    }
}