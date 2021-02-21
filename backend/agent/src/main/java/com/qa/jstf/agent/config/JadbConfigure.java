package com.qa.jstf.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import se.vidstige.jadb.JadbConnection;

@Component
@ConfigurationProperties(prefix = "adb")
public class JadbConfigure {

    String host = "localhost";

    Integer port = 5037;

    @Bean
    public JadbConnection jadbConnection() {
        return new JadbConnection(host, port);
    }
}
