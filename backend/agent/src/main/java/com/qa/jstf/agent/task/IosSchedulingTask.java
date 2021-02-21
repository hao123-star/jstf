package com.qa.jstf.agent.task;

import com.qa.jstf.agent.entity.ios.IosDevice;
import com.qa.jstf.agent.entity.message.RotationMsg;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.IosDeviceService;
import com.qa.jstf.agent.service.WebDriverAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.websocket.EncodeException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class IosSchedulingTask {

    @Autowired
    DeviceRepo deviceRepo;

    @Autowired
    WebDriverAgentService webDriverAgentService;

    @Autowired
    IosDeviceService iosDeviceService;

    @Scheduled(fixedDelay = 10000)
    public void getRotation() {
        Map<String, IosDevice> iosDeviceMap = deviceRepo.getIosDeviceMap();

        iosDeviceMap.forEach((serial, iosDevice) -> {
            if (null == iosDevice || null == iosDevice.getWebSocketSession() || null == iosDevice.getWebDriverAgent()
                    || null == iosDevice.getWebDriverAgent().getUrl() || null == iosDevice.getSessionId()) {
                return;
            }

            try {
                synchronized (iosDevice.getSessionId()) {
                    Integer rotation =
                            webDriverAgentService.getRotation(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                                    iosDevice.getSessionId());

                    if (!rotation.equals(iosDevice.getRotation())) {
                        iosDevice.setRotation(rotation);
                        RotationMsg rotationMsg = RotationMsg.builder().rotation(rotation)
                                .type("rotationMsg")
                                .build();

                        if (null != iosDevice.getWebSocketSession()) {
                            synchronized (iosDevice.getWebSocketSession()) {
                                iosDevice.getWebSocketSession().getBasicRemote().sendObject(rotationMsg);
                            }
                        }
                    }
                }
            } catch (URISyntaxException | IOException | EncodeException e) {
                log.error("{}", e);
            }
        });
    }

    @Scheduled(fixedDelay = 120000)
    public void release() {
        Map<String, IosDevice> releaseMap = deviceRepo.getToReleaseIosDeviceMap();

        releaseMap.forEach((serial, iosDevice) -> {
            Duration duration = Duration.between(iosDevice.getReleaseTime(), LocalDateTime.now());

            if (duration.toMinutes() >= 2L) {
                iosDeviceService.reset(serial);
            }
        });
    }
}
