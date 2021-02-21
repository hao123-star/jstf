package com.qa.jstf.agent.service;

import se.vidstige.jadb.JadbException;
import javax.websocket.Session;
import java.io.IOException;

public interface RotationService {
    String install(String serial) throws IOException, JadbException;

    void start(String serial, Session session) throws IOException, JadbException;

    boolean started(String serial);

    void reset(String serial);
}
