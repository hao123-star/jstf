package com.qa.jstf.agent.service;

import javax.websocket.Session;

public interface ImgService {

    default boolean install(String serial) {
        return true;
    }

    default void start(String serial) {}

    default boolean started(String serial) {
        return true;
    }

    default void stop(String serial) {
        return;
    }

    void forwardSocket(String serial);

    boolean removeSocket(String serial);

    void receiveImg(String serial);

    void sendImg(String serial);

    void reset(String serial);
}
