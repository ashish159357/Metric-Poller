package org.example.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class IpRangeGenerator {

    public static List<String> getAllIpsInRange(String ipRange) throws UnknownHostException {
        List<String> ipList = new ArrayList<>();

        String[] parts = ipRange.split("-");
        String startIp = parts[0];
        String[] startIpParts = startIp.split("\\.");

        int lastOctet = Integer.parseInt(startIpParts[3]);
        int endLastOctet = Integer.parseInt(parts[1]);

        long start = ipToLong(InetAddress.getByName(startIp));

        for (int i = lastOctet; i <= endLastOctet; i++) {
            startIpParts[3] = String.valueOf(i);
            String currentIp = String.join(".", startIpParts);
            ipList.add(currentIp);
        }
        return ipList;
    }

    public static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xFF;
        }
        return result;
    }

    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                (ip & 0xFF);
    }
}
