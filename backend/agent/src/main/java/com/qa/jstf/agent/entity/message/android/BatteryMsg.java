package com.qa.jstf.agent.entity.message.android;

import com.qa.jstf.agent.entity.enumaration.EnumAndroidInfo;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BatteryMsg extends WebSocketMsg {
//    EnumAndroidInfo type = EnumAndroidInfo.Battery;

    String type;

    String health;

    Integer level;

    Integer scale;

    String source;

    String status;

    Double temp;

    Double voltage;
}
