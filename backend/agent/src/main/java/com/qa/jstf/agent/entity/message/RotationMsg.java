package com.qa.jstf.agent.entity.message;

import lombok.*;

/**
 * 旋转屏幕
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RotationMsg extends WebSocketMsg{
    Integer rotation;

    String type;
}
