package com.qa.jstf.agent.entity.android;

import lombok.*;

import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MiniCap {
    // 转发端口
    Integer port;

    // 转发socket
    Socket socket;

    // 启动流
    InputStream inputStream;

    BlockingDeque<byte []> imgQueue;

    boolean started;

    /**
     * 0：不重启
     * 1： 准备重启
     * 2： 重启
     */
    short restarting;
}
