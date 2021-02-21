package com.qa.jstf.agent.service;

import com.qa.jstf.agent.entity.Device;


public interface AndroidDeviceService extends DeviceService{

    void noticeServer();

    void restartMiniCap(String serial);

    void reset(String serial);
}
