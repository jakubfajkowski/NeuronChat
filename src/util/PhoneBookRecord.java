package util;

import java.net.InetAddress;

/**
 * Created by FajQa on 12.01.2017.
 */
public class PhoneBookRecord {
    InetAddress ip;
    int port;

    public PhoneBookRecord(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
