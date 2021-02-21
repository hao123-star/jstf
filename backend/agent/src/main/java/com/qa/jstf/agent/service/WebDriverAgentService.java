package com.qa.jstf.agent.service;

import com.qa.jstf.agent.entity.ios.BatteryInfo;
import com.qa.jstf.agent.entity.message.TouchMsg;

import java.net.URISyntaxException;
import java.util.Map;

public interface WebDriverAgentService {

    void forwardSocket(String serial);

    void reset(String serial);

    void handleCmd(String serial, TouchMsg webSocketMsg) throws URISyntaxException;

    void handleMouseMove(String serial) throws InterruptedException, URISyntaxException;

    void handleMouseMove2(String serial) throws InterruptedException, URISyntaxException;

    void click(String url, String session, Integer x, Integer y);

    void drag(String url, String session, Integer fromX, Integer fromY, Integer toX, Integer toY, Double duration);

    String getSession(String url);

    BatteryInfo getBatterInfo(String url, String session);

    Map<String, Integer> getScreenInfo(String url, String serial);

    void changeOrientation(String url, String session, String orientation);

    void simulateHomeScreen(String url);

    void setScreenShotQuality(String url, String session, String quality);

    Integer getRotation(String url, String session);
}
