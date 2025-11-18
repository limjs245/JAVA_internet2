import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IpUtils2 {

    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

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