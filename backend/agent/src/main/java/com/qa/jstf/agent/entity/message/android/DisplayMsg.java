package com.qa.jstf.agent.entity.message.android;

import com.qa.jstf.agent.entity.enumaration.EnumAndroidInfo;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DisplayMsg extends WebSocketMsg {

//    EnumAndroidInfo type = EnumAndroidInfo.Display;
    String type;

    boolean success;

    Integer width;

    Integer height;

    Float xdpi;

    Float ydpi;

    Float fps;

    Float density;

    Integer rotation;

    boolean secure;

    boolean status;

    boolean mounted;

}
