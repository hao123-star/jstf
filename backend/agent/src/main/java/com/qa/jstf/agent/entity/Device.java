package com.qa.jstf.agent.entity;

import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import lombok.Getter;
import lombok.Setter;

import javax.websocket.Session;
import java.time.LocalDateTime;

@Getter
@Setter
public abstract class Device {
    String serial;

    Integer rotation;

    Integer realWidth;

    Integer realHeight;

    EnumPhoneStatus status;

    Session webSocketSession;

    LocalDateTime releaseTime;
}
