package com.qa.jstf.agent.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.jstf.agent.entity.message.ImgMsg;
import com.qa.jstf.agent.entity.message.WebSocketMsg;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class MessageEncoder implements Encoder.Text<WebSocketMsg> {
    @Override
    public String encode(WebSocketMsg webSocketMsg) throws EncodeException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(webSocketMsg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
