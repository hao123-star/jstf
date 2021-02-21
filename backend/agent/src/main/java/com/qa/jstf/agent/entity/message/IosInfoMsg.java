package com.qa.jstf.agent.entity.message;

import lombok.*;

/**
 * 屏幕信息
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class IosInfoMsg extends WebSocketMsg{
    Integer realX;

    Integer realY;

    Integer rotation;

    String sessionId;

    String type;
}
