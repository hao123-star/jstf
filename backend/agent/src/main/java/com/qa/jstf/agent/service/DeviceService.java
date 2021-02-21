package com.qa.jstf.agent.service;

import com.qa.jstf.agent.entity.Device;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import javax.websocket.Session;

public interface DeviceService {

    void sync();

    default boolean occupy(Device device) {
        if (null == device) {
            return false;
        }

        synchronized (this) {
            if (null != device.getStatus() && !device.getStatus().equals(EnumPhoneStatus.Idle)) {
                return false;
            } else {
                device.setStatus(EnumPhoneStatus.Occupied);
            }
        }

        return true;
    }

    void start(String serial, Session session);

    void toRelease(String serial);

    void handleCommand(String serial, WebSocketMsg webSocketMsg);

    void reset(String serial);
}
