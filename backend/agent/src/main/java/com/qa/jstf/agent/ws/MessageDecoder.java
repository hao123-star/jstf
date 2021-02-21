package com.qa.jstf.agent.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class MessageDecoder implements Decoder.Text<WebSocketMsg> {

    @Override
    public WebSocketMsg decode(String s) throws DecodeException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(s, WebSocketMsg.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
