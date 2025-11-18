import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IpUtils {

    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // 1. 루프백(127.0.0.1), 2. 가상, 3. 비활성화 인터페이스 제외
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // 4. IPv4 주소이면서, 5. 사설 IP 대역인 경우
                    if (inetAddress instanceof Inet4Address && inetAddress.isSiteLocalAddress()) {
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
        System.out.println("My Local IP is: " + getLocalIp());
    }
}