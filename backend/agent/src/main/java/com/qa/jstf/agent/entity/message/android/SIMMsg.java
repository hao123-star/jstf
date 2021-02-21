package com.qa.jstf.agent.entity.message.android;

import com.qa.jstf.agent.entity.enumaration.EnumAndroidInfo;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SIMMsg extends WebSocketMsg {
//    EnumAndroidInfo type = EnumAndroidInfo.SIM;
    String type;

    String imei;

    String network;

    String imsi;

    String phoneNumber;

    String iccid;

    String operator;
}
