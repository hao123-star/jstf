package com.qa.jstf.agent.entity.android;

import lombok.*;
import java.io.InputStream;
import java.net.Socket;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class STF {
    // 转发端口
    Integer port;

    // 转发socket
    Socket socket;

    // 启动流
    InputStream inputStream;

    boolean started;
}
