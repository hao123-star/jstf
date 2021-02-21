package com.qa.jstf.agent.ws;

import com.qa.jstf.agent.entity.message.InitMsg;
import com.qa.jstf.agent.entity.message.TouchMsg;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.MiniTouchService;
import com.qa.jstf.agent.service.WebDriverAgentService;
import com.qa.jstf.agent.service.impl.android.AndroidDeviceServiceImpl;
import com.qa.jstf.agent.service.impl.ios.IosDeviceServiceImpl;
import com.qa.jstf.agent.service.impl.ios.WebDriverAgentServiceImpl;
import com.qa.jstf.agent.utils.SpringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.URISyntaxException;

@ServerEndpoint(value = "/{type}/{serial}", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
@Component
@Slf4j
@Getter
public class WebSocketServer {

    static DeviceRepo deviceRepo;

    @Autowired
    public void setDeviceRepo(DeviceRepo deviceRepo) {
        WebSocketServer.deviceRepo = deviceRepo;
    }

    @OnOpen
    public void onOpen(@PathParam("type") String type, @PathParam("serial") String serial, Session session) {
    }

    @OnClose
    public void onClose(@PathParam("type") String type, @PathParam("serial") String serial, Session session) {
        if ("ios".equalsIgnoreCase(type)) {
            IosDeviceServiceImpl iosDeviceService = SpringUtils.getBean(IosDeviceServiceImpl.class);
            iosDeviceService.toRelease(serial);
        } else if ( "android".equalsIgnoreCase(type)) {
            AndroidDeviceServiceImpl androidDeviceService = SpringUtils.getBean(AndroidDeviceServiceImpl.class);
            androidDeviceService.toRelease(serial);
        }
    }

    @OnError
    public void OnError(@PathParam("type") String type, @PathParam("serial") String serial, Session session, Throwable throwable) {
        if ("ios".equalsIgnoreCase(type)) {
            IosDeviceServiceImpl iosDeviceService = SpringUtils.getBean(IosDeviceServiceImpl.class);
            iosDeviceService.toRelease(serial);
        } else if ( "android".equalsIgnoreCase(type)) {
            AndroidDeviceServiceImpl androidDeviceService = SpringUtils.getBean(AndroidDeviceServiceImpl.class);
            androidDeviceService.toRelease(serial);
        }
    }

    @OnMessage
    public void onMessage(@PathParam("type") String type, @PathParam("serial") String serial, Session session, WebSocketMsg webSocketMsg) {
        if ("ios".equalsIgnoreCase(type)) {
            if (webSocketMsg instanceof InitMsg) { // 启动
                IosDeviceServiceImpl iosDeviceService = SpringUtils.getBean(IosDeviceServiceImpl.class);
                iosDeviceService.start(serial, session);
                return;
            }

            if (webSocketMsg instanceof TouchMsg) { // 操作屏幕
                TouchMsg touchMsg = (TouchMsg) webSocketMsg;
                WebDriverAgentService webDriverAgentService = SpringUtils.getBean(WebDriverAgentServiceImpl.class);

                try {
                    webDriverAgentService.handleCmd(serial, touchMsg);
                } catch (URISyntaxException e) {
                    log.error("{}", e);
                }

                return;
            }
        } else if ("android".equalsIgnoreCase(type)) {
            if (webSocketMsg instanceof InitMsg) {
                AndroidDeviceServiceImpl androidDeviceService = SpringUtils.getBean(AndroidDeviceServiceImpl.class);
                androidDeviceService.start(serial, session);
                return;
            }

            if (webSocketMsg instanceof TouchMsg) {
                TouchMsg touchMsg = (TouchMsg) webSocketMsg;
                MiniTouchService miniTouchService = SpringUtils.getBean(MiniTouchService.class);
                miniTouchService.forwardCommand(serial, touchMsg.getAction());
                return;
            }
        }
    }
}
