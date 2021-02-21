package com.qa.jstf.agent.entity.ios;

import lombok.*;
import java.net.URL;
import java.util.concurrent.BlockingDeque;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Mjpeg {

    Integer port;

    URL url;

    Process process;

    BlockingDeque<byte []> imgQueue;

    boolean running;
}
