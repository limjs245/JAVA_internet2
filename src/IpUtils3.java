import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IpUtils3 {

    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            System.out.println("=== 네트워크 인터페이스 확인 ===");
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // 디버깅: 모든 인터페이스 정보 출력
                System.out.println("\n인터페이스: " + networkInterface.getName());
                System.out.println("  Display Name: " + networkInterface.getDisplayName());
                System.out.println("  isLoopback: " + networkInterface.isLoopback());
                System.out.println("  isVirtual: " + networkInterface.isVirtual());
                System.out.println("  isUp: " + networkInterface.isUp());

                // 1. 루프백(127.0.0.1), 2. 가상, 3. 비활성화 인터페이스 제외
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    System.out.println("  -> 제외됨");
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    System.out.println("  IP: " + inetAddress.getHostAddress());
                    System.out.println("    instanceof Inet4Address: " + (inetAddress instanceof Inet4Address));
                    System.out.println("    isSiteLocalAddress: " + inetAddress.isSiteLocalAddress());

                    // 4. IPv4 주소이면서, 5. 사설 IP 대역인 경우
                    if (inetAddress instanceof Inet4Address && inetAddress.isSiteLocalAddress()) {
                        System.out.println("  -> 선택됨!");
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            return ">>> error: " + e.getMessage();
        }
        return ">> No Site Local IPv4 Address Found";
    }

    public static void main(String[] args) {
        System.out.println("\n결과: " + getLocalIp());
    }
}