package com.qa.jstf.agent.entity.android;

import lombok.*;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.net.Socket;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MiniTouch {
    // socket转发端口
    Integer port;

    // 写入命令流
    BufferedWriter bufferedWriter;

    // 转发端口
    Socket socket;

    // mini touch是否已启动
    boolean started;

    // 是否已连接到mini touch
    boolean socketConnected;

    // 启动流
    InputStream inputStream;
}
