package com.qa.jstf.agent.entity.message;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TouchMsg extends WebSocketMsg {

    String type;

    String action;

    Integer x;

    Integer y;

    Integer fromX;

    Integer fromY;

    Integer toX;

    Integer toY;

    Double time;

    Double duration;

    String quality;
}
