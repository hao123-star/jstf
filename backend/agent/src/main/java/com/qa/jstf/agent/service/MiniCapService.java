package com.qa.jstf.agent.service;

import se.vidstige.jadb.JadbException;
import java.io.IOException;

public interface MiniCapService {
    boolean install(String serial);

    void start(String serial) throws IOException, JadbException;

    boolean started(String serial);

    void stop(String serial) throws IOException;

    boolean removeForward(String serial);

    boolean forwardSocket(String serial);

    void receiveImg(String serial) throws IOException;

    void sendImg(String serial);

    boolean reset(String serial);
}
