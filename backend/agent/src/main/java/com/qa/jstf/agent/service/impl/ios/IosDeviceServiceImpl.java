package com.qa.jstf.agent.service.impl.ios;

import com.qa.jstf.agent.entity.android.AndroidDevice;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import com.qa.jstf.agent.entity.ios.IosDevice;
import com.qa.jstf.agent.entity.message.InitMsg;
import com.qa.jstf.agent.entity.message.IosInfoMsg;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.IosDeviceService;
import com.qa.jstf.agent.service.WebDriverAgentService;
import com.qa.jstf.agent.utils.lcmd.ExecutedResult;
import com.qa.jstf.agent.utils.lcmd.LocalCommandExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class IosDeviceServiceImpl implements IosDeviceService {

    @Autowired
    DeviceRepo deviceRepo;

    @Lazy
    @Autowired
    MjpegService mjpegService;

    @Autowired
    WebDriverAgentService webDriverAgentService;

    @Autowired
    LocalCommandExecutor localCommandExecutor;

    @Value("${wda.home}")
    String wdaHome;

    static String START_CMD = "xcodebuild -project %s/WebDriverAgent.xcodeproj -scheme WebDriverAgentRunner -destination id=%s test";

    static String LIST_PHONES_CMD = "idevice_id -l | head -n1";

    List<String> removedPhones = new ArrayList<>();

    List<String> addedPhones = new ArrayList<>();

    @Scheduled(fixedDelay = 30000)
    @Override
    public void sync() {
        Map<String, IosDevice> iosDeviceMap = deviceRepo.getIosDeviceMap();
        ExecutedResult executedResult = localCommandExecutor.executeCommand(LIST_PHONES_CMD, 10000);

        if (executedResult.getExitCode() == 0) {
            String result = executedResult.getExecuteOut();

            if (StringUtils.hasLength(result)) {
               String [] serials = result.split("\n");

                for (String serial : serials) {
                    if (!iosDeviceMap.containsKey(serial)) { // 新接入设备
                        IosDevice iosDevice = IosDevice.builder().build();
                        iosDevice.setSerial(serial);
                        iosDevice.setStatus(EnumPhoneStatus.Idle);
                        iosDeviceMap.put(serial, iosDevice);
                        addedPhones.add(serial);
                    }
                }

                Set<String> set = iosDeviceMap.keySet();

                if (null != set && set.size() > 0) {
                    for (String s : set) {
                        if (Arrays.binarySearch(serials, s) < 0) {
                            iosDeviceMap.remove(s);
                            removedPhones.add(s);
                        }
                    }
                }
            }

            // todo: notice server
        }
    }

    @Async
    @Override
    public void start(String serial, Session session) {
        IosDevice iosDevice = deviceRepo.getToReleaseIosDeviceMap().get(serial);

        if (null != iosDevice) {
            iosDevice.setWebSocketSession(session);
            deviceRepo.getIosDeviceMap().put(serial, iosDevice);
            mjpegService.sendImg(serial);
            deviceRepo.getToReleaseIosDeviceMap().remove(serial);
            IosInfoMsg iosInfoMsg = IosInfoMsg.builder().realX(iosDevice.getRealWidth())
                    .realY(iosDevice.getRealHeight())
                    .rotation(iosDevice.getRotation())
                    .type("iosInfoMsg")
                    .build();

            synchronized (session) {
                try {
                    log.info("session: {}", session);
                    session.getBasicRemote().sendObject(iosInfoMsg);
                } catch (IOException | EncodeException e) {
                    log.error("{}", e);
                }
            }

            return;
        }

        iosDevice = deviceRepo.getIosDeviceMap().get(serial);

        if (false == occupy(iosDevice)) {
            log.warn("设备不存在，{}", serial);

            InitMsg initMsg = InitMsg.builder().type("initMsg")
                    .ready(false)
                    .msg("设备不存在...")
                    .build();

            try {
                synchronized (session) {
                    session.getBasicRemote().sendObject(initMsg);
                }
            } catch (IOException | EncodeException e) {
                log.error("{}", e);
            }
        }

        iosDevice.setWebSocketSession(session);

        // start webDriverAgent
        String startCommand = String.format(START_CMD, wdaHome, serial);
        Process process = null;

        try {
            process = localCommandExecutor.executeCommand(startCommand);
            iosDevice.setProcess(process);
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);

                if (line.contains("8100")) {
                    InitMsg initMsg = InitMsg.builder().type("initMsg")
                            .ready(true)
                            .msg("启动成功")
                            .build();

                    log.info("启动成功");

                    try {
                        synchronized (session) {
                            session.getBasicRemote().sendObject(initMsg);
                        }
                    } catch (IOException | EncodeException e) {
                        log.error("{}", e);
                    }

                    webDriverAgentService.forwardSocket(serial);
                    mjpegService.forwardSocket(serial);
                    mjpegService.receiveImg(serial);
                    mjpegService.sendImg(serial);
                }
            }
        } catch (IOException e) {
            log.error("启动wda失败，serial：{}", serial);
            reset(serial);
        }
    }

    @Override
    public void toRelease(String serial) {
        IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);

        if (null == iosDevice) {
            return;
        }

        iosDevice.setReleaseTime(LocalDateTime.now());
        deviceRepo.getIosDeviceMap().remove(serial);
        deviceRepo.getToReleaseIosDeviceMap().put(serial, iosDevice);
    }

    @Override
    public void handleCommand(String serial, WebSocketMsg webSocketMsg) {
    }

    @Override
    public void reset(String serial) {
        mjpegService.reset(serial);
        webDriverAgentService.reset(serial);
        IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);

        if (null != iosDevice) {
            iosDevice.setMjpeg(null);
            iosDevice.setRotation(0);
            iosDevice.setStatus(EnumPhoneStatus.Idle);
            iosDevice.setWebDriverAgent(null);

            if (null != iosDevice.getProcess()) {
                iosDevice.getProcess().destroy();
                iosDevice.setProcess(null);
            }
        }
    }
}
