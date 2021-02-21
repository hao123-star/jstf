package com.qa.jstf.agent.entity.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qa.jstf.agent.entity.message.android.BatteryMsg;
import com.qa.jstf.agent.entity.message.android.DisplayMsg;
import com.qa.jstf.agent.entity.message.android.NetWorkMsg;
import com.qa.jstf.agent.entity.message.android.SIMMsg;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ImgMsg.class, name = "imgMsg"),
        @JsonSubTypes.Type(value = InitMsg.class, name = "initMsg"),
        @JsonSubTypes.Type(value = IosInfoMsg.class, name = "iosInfoMsg"),
        @JsonSubTypes.Type(value = TouchMsg.class, name = "touchMsg"),
        @JsonSubTypes.Type(value = RotationMsg.class, name = "rotationMsg"),
        @JsonSubTypes.Type(value = BatteryMsg.class, name = "batteryMsg"),
        @JsonSubTypes.Type(value = DisplayMsg.class, name = "displayMsg"),
        @JsonSubTypes.Type(value = NetWorkMsg.class, name = "netWorkMsg"),
        @JsonSubTypes.Type(value = SIMMsg.class, name = "simMsg")
})
public class WebSocketMsg {
}
