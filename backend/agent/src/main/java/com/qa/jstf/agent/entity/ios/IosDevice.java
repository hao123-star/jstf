package com.qa.jstf.agent.entity.ios;

import com.qa.jstf.agent.entity.Device;
import lombok.*;

import javax.websocket.Session;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class IosDevice extends Device {

    // 启动wda的进程
    Process process;

    Mjpeg mjpeg;

    WebDriverAgent webDriverAgent;

    String sessionId;

    Session webSocketSession;

//    LocalDateTime releaseTime;
}
