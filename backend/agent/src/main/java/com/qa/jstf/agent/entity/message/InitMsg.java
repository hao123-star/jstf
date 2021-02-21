package com.qa.jstf.agent.entity.message;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InitMsg extends WebSocketMsg {
    String msg;

    boolean ready;

    String type;
}
