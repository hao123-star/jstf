package com.qa.jstf.agent.service;

import se.vidstige.jadb.JadbException;

import java.io.IOException;

public interface MiniTouchService {
    boolean install(String serial);

    void start(String serial) throws IOException, JadbException;

    void stop(String serial) throws IOException;

    boolean started(String serial);

    boolean forwardSocket(String serial);

    void connectSocket(String serial) throws IOException;

    void forwardCommand(String serial, String command);

    boolean reset(String serial);
}
