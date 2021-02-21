package com.qa.jstf.agent.mq;

import com.qa.jstf.agent.entity.android.AndroidDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableBinding(PhoneSource.class)
public class PhoneProducer {

    @Autowired
    @Output(PhoneSource.OUTPUT)
    private MessageChannel channel;

    public void send(String ip, String phoneType, List<AndroidDevice> androidDevices) {
        log.info("更新手机状态: {}");

        List<Map<String, Object>> list = new ArrayList<>();

        androidDevices.forEach(androidDevice -> {
            Map<String, Object> deviceMap = new HashMap<>();
            deviceMap.put("ip", ip);
            deviceMap.put("serial", androidDevice.getSerial());
            deviceMap.put("status", androidDevice.getStatus().toString());
            deviceMap.put("width", androidDevice.getRealWidth());
            deviceMap.put("height", androidDevice.getRealWidth());
            deviceMap.put("type", phoneType);
            list.add(deviceMap);
        });

        channel.send(MessageBuilder.withPayload(list).build());
    }

}
