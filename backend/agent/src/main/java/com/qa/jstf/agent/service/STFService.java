package com.qa.jstf.agent.service;

import se.vidstige.jadb.JadbException;
import java.io.IOException;

public interface STFService {

    boolean install(String serial);

    boolean start(String serial) throws IOException, JadbException;

    boolean forwardSocket(String serial);

    void receiveCmd(String serial);

    void sendInfo(String serial) throws IOException;

    void readSTFInfo(String serial) throws IOException;

    void launch(String serial) throws IOException, JadbException;

    void reset(String serial);

    boolean started(String serial);
}
