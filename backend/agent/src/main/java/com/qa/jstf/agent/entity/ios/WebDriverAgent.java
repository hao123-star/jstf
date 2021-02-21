package com.qa.jstf.agent.entity.ios;

import com.qa.jstf.agent.entity.message.TouchMsg;
import lombok.*;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WebDriverAgent {

    Integer port;

    URL url;

    Process process;

    BlockingQueue<TouchMsg> touchMsgQueue;

    boolean mouseMoving;

    boolean running;
}
