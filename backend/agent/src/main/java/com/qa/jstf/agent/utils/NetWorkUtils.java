package com.qa.jstf.agent.utils;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

@Slf4j
public class NetWorkUtils {
    // 分配端口
    public static Integer allocatePort() {
        boolean flag = false;
        Socket socket = null;
        int port = 0;

        while (false == flag) {
            double random = Math.random();
            port = (int) (random * 100);
            port = port + 4000;

            try {
                socket = new Socket(InetAddress.getLocalHost(), port);
                socket.close();
                log.warn("端口{}已被占用", port);
            } catch (IOException e) {
                flag = true;
                log.info("端口{}未被占用", port);
            }
        }

        return port;

    }

    // 创建socket
    public static Socket createLocalSocket(int port) throws IOException{
        Socket socket = new Socket("localhost", port);
        return socket;
    }

}
