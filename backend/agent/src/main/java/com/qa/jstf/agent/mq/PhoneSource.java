package com.qa.jstf.agent.mq;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface PhoneSource {

    String OUTPUT = "phoneOutput";

    @Output(PhoneSource.OUTPUT)
    MessageChannel output();
}
