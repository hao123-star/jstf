package com.qa.jstf.agent.entity.message.android;

import com.qa.jstf.agent.entity.enumaration.EnumAndroidInfo;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NetWorkMsg extends WebSocketMsg {
//    EnumAndroidInfo type = EnumAndroidInfo.NetWork;

    String type;

    boolean connected;

    boolean airPlaneMode;

    boolean failover;

    boolean roaming;

    String networkType;

    String subNetworkType;
}
