package com.qa.jstf.agent.service.impl.android;

import com.qa.jstf.agent.entity.android.AndroidDevice;
import com.qa.jstf.agent.entity.android.MiniCap;
import com.qa.jstf.agent.entity.enumaration.EnumAndroidState;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import com.qa.jstf.agent.entity.ios.IosDevice;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import com.qa.jstf.agent.mq.PhoneProducer;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.*;
import com.qa.jstf.agent.utils.WaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AndroidDeviceServiceImpl implements AndroidDeviceService {

    @Autowired
    JadbConnection jadbConnection;

    @Autowired
    DeviceRepo deviceRepo;

    @Autowired
    MiniCapService miniCapService;

    @Autowired
    MiniTouchService miniTouchService;

    @Autowired
    RotationService rotationService;

    @Autowired
    STFService stfService;

    /*@Autowired
    CmdService cmdService;*/

    /*@Autowired
    WebSocketSessionRepo webSocketSessionRepo;*/

    @Autowired
    PhoneProducer phoneProducer;

    @Scheduled(fixedDelay = 30000)
    @Override
    public void sync() {
        Map<String, AndroidDevice> currentDeviceMap = deviceRepo.getAndroidDeviceMap();

        Map<String, AndroidDevice> connectedDeviceMap = new HashMap<>(currentDeviceMap);

        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();

            if (null != jadbDevices && jadbDevices.size() > 0) {
                for (JadbDevice jadbDevice : jadbDevices) {
                    String serial = jadbDevice.getSerial();
                    AndroidDevice.AndroidDeviceBuilder builder = AndroidDevice.builder();
                    builder.serial(serial)
                            .androidState(EnumAndroidState.Unknown)
                            .jadbDevice(jadbDevice)
                    ;

                    try {
                        JadbDevice.State state = jadbDevice.getState();
                        builder.androidState(EnumAndroidState.valueOf(state.name()));
                    } catch (IOException | JadbException e) {
                        log.error("{}", e);
                    }

                    connectedDeviceMap.put(serial, builder.build());
                }
            }
        } catch (IOException | JadbException e) {
            log.error("{}", e);
        }

        connectedDeviceMap.forEach((k, v) -> {
            AndroidDevice androidDevice = currentDeviceMap.get(k);

            if (null != androidDevice) {
                if (!androidDevice.getAndroidState().equals(v.getAndroidState())) { // 状态改变的设备
                    log.info("设备{}状态改变{} -> {}", k, v.getAndroidState().name(), androidDevice.getAndroidState().name());
                    androidDevice.setUpdateTime(LocalDateTime.now());
                    androidDevice.setAndroidState(v.getAndroidState());
                }
            } else if (null == androidDevice) { // 新接入的设备
                log.info("发现新设备{}", k);
                v.setCreateTime(LocalDateTime.now());
                v.setStatus(EnumPhoneStatus.Idle);
                currentDeviceMap.put(k, v);
            }
        });

        currentDeviceMap.forEach((k, v) -> {
            if (!connectedDeviceMap.containsKey(k)) { // 移除的设备
                log.info("设备{}已经移除", k);

                if (EnumPhoneStatus.Occupied.equals(v.getStatus())) { // 被占用的移除了
                    reset(k);
                }

                v.setUpdateTime(LocalDateTime.now());
                v.setStatus(EnumPhoneStatus.Offline);
            } else if (!EnumPhoneStatus.Occupied.equals(v.getStatus())){ // 仍连接着的空闲的设备
                if (EnumAndroidState.Device.equals(v.getAndroidState())) {
                    v.setStatus(EnumPhoneStatus.Idle);
                } else {
                    v.setStatus(EnumPhoneStatus.Down);
                }
            } else if (EnumPhoneStatus.Occupied.equals(v.getStatus()) && !EnumAndroidState.Device.equals(v.getAndroidState())) { // 被占用手机发生异常
                v.setStatus(EnumPhoneStatus.Down);
                reset(k);
            }
        });

        /*if (null != deviceRepo.getAndroidDeviceMap().values()) {
            try {
                InetAddress address = InetAddress.getLocalHost();
                phoneProducer.send(address.getHostAddress(), "android", deviceRepo.getAndroidDeviceMap().values().stream().collect(Collectors.toList()));
            } catch (UnknownHostException e) {
                log.error("获取本地ip失败");
            }
        }*/
    }

    @Override
    public void handleCommand(String serial, WebSocketMsg webSocketMsg) {
    }

    @Override
    public void noticeServer() {
    }

    @Override
    public void start(String serial, Session session) {
        AndroidDevice androidDevice = deviceRepo.getToReleaseAndroidDeviceMap().get(serial);

        if (null != androidDevice) {
            androidDevice.setWebSocketSession(session);
            deviceRepo.getAndroidDeviceMap().put(serial, androidDevice);
            deviceRepo.getToReleaseIosDeviceMap().remove(serial);
            miniCapService.sendImg(serial);

            try {
                stfService.sendInfo(serial);
                stfService.readSTFInfo(serial);
            } catch (IOException e) {
                log.error("{}", e);
            }

            return;
        }


        androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        boolean result = occupy(androidDevice);

        if (false == result) {
            log.error("手机<{}>已被占用", serial);
            return;
        }

        androidDevice.setWebSocketSession(session);

        // sft
        try {
            stfService.launch(serial);
        } catch (IOException | JadbException e) {
            log.error("{}", e);
        }

        result = stfService.started(serial);

        if (false == result) {
            log.error("启动stf失败:{}", serial);
            return;
        }

        // rotation
        try {
            rotationService.start(serial, session);
        } catch (IOException | JadbException e) {
            log.error("{}", e);
        }

        result = rotationService.started(serial);

        if (false == result) {
            log.error("启动rotation失败:{}", serial);
            return;
        }

        // mini cap
        result = miniCapService.install(serial);

        if (false == result) {
            log.error("安装mini cap失败:{}", serial);
            return;
        }

        try {
            miniCapService.start(serial);
        } catch (IOException | JadbException e) {
            log.error("{}", e);
        }

        result = miniCapService.started(serial);

        if (false == result) {
            log.error("启动mini cap失败:{}", serial);
            return;
        }

        result = miniCapService.forwardSocket(serial);

        if (false == result) {
            log.error("转发mini cap端口失败:{}", serial);
            return;
        }

        try {
            miniCapService.receiveImg(serial);
        } catch (IOException e) {
            log.error("{}", e);
        }

        miniCapService.sendImg(serial);

        // mini touch
        result = miniTouchService.install(serial);

        if (false == result) {
            log.error("安装mini touch失败:{}", serial);
            return;
        }

        try {
            miniTouchService.start(serial);
        } catch (IOException | JadbException e) {
            log.error("{}", e);
        }

        result = miniTouchService.started(serial);

        if (false == result) {
            log.error("启动mini touch失败:{}", serial);
            return;
        }

        result = miniTouchService.forwardSocket(serial);

        if (false == result) {
            log.error("转发mini touch端口失败:{}", serial);
            return;
        }

        try {
            miniTouchService.connectSocket(serial);
        } catch (IOException e) {
            log.error("{}", e);
            return;
        }
    }

    @Override
    public void toRelease(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return;
        }

        androidDevice.setReleaseTime(LocalDateTime.now());
        deviceRepo.getAndroidDeviceMap().remove(serial);
        deviceRepo.getToReleaseAndroidDeviceMap().put(serial, androidDevice);
    }

    @Override
    public void restartMiniCap(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return;
        }

        MiniCap miniCap = androidDevice.getMiniCap();

        if (null == miniCap) {
            return;
        }

        for (int i=0;i<10;i++) {
            if (miniCap.getRestarting() == (short)2) {
                break;
            }

            if (i == 9) {
                log.error("重启mini cap失败");
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("{}", e);

            }
        }

        Socket socket = miniCap.getSocket();

        if (null != socket) {
            try {
                // 关闭和mini cap的连接
                socket.close();
            } catch (IOException e) {
                log.error("{}", e);
            }

            miniCap.setSocket(null);
        }

        InputStream inputStream = miniCap.getInputStream();

        if (null != inputStream) {
            try {
                // 停止mini cap
                inputStream.close();
                miniCap.setStarted(false);
            } catch (IOException e) {
                log.error("{}", e);
            }

            miniCap.setInputStream(null);
        }

        boolean result = miniCapService.removeForward(serial);

        if (false == result) {
            return;
        }

        try {
            miniCapService.start(serial);
        } catch (IOException | JadbException e) {
            log.error("{}", e);
        }

        result = miniCapService.started(serial);

        if (false == result) {
            return;
        }

        result = miniCapService.forwardSocket(serial);

        if (false == result) {
            return;
        }

        miniCap.setRestarting((short)0);

        try {
            miniCapService.receiveImg(serial);
        } catch (IOException e) {
            log.error("{}", e);
        }
    }

    @Override
    public void reset(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null != androidDevice && null != androidDevice.getWebSocketSession()) {
            try {
                androidDevice.getWebSocketSession().close();
            } catch (IOException e) {
                log.error("{}", e);
            }

            androidDevice.setStatus(EnumPhoneStatus.Idle);
        }

        miniCapService.reset(serial);
        miniTouchService.reset(serial);
        rotationService.reset(serial);
    }
}
