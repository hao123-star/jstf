package com.qa.jstf.agent.entity.message;

import lombok.*;

/**
 * 发送图片
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ImgMsg extends WebSocketMsg {

    String type;
}
