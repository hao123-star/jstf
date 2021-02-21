package com.qa.jstf.agent.entity.android;

import com.qa.jstf.agent.entity.Device;
import com.qa.jstf.agent.entity.enumaration.EnumAndroidState;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import lombok.*;
import se.vidstige.jadb.JadbDevice;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AndroidDevice extends Device {
    String serial;

    JadbDevice jadbDevice;

    EnumAndroidState androidState;

    MiniCap miniCap;

    MiniTouch miniTouch;

    STF stf;

    RotationWatcher rotationWatcher;

    Integer realWidth;

    Integer realHeight;

    LocalDateTime createTime;

    LocalDateTime updateTime;

    LocalDateTime downTime;

//    EnumPhoneStatus phoneStatus;
}
