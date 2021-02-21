package com.qa.jstf.agent.service.impl.android;

import com.google.protobuf.ByteString;
import com.qa.jstf.agent.entity.android.*;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import com.qa.jstf.agent.entity.message.android.BatteryMsg;
import com.qa.jstf.agent.entity.message.android.DisplayMsg;
import com.qa.jstf.agent.entity.message.android.NetWorkMsg;
import com.qa.jstf.agent.entity.message.android.SIMMsg;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.STFService;
import com.qa.jstf.agent.utils.NetWorkUtils;
import com.qa.jstf.agent.utils.WaitUtils;
import com.qa.jstf.agent.utils.lcmd.ExecutedResult;
import com.qa.jstf.agent.utils.lcmd.LocalCommandExecutor;
import jp.co.cyberagent.stf.proto.Wire;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
public class STFServiceImpl implements STFService {
    @Autowired
    DeviceRepo deviceRepo;

    /*@Autowired
    WebSocketSessionRepo webSocketSessionRepo;*/

    @Autowired
    LocalCommandExecutor localCommandExecutor;

    @Override
    public boolean install(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        androidDevice.setStf(STF.builder().build());
        return true;
    }

    @Override
    public boolean start(String serial) throws IOException, JadbException {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        STF stf = androidDevice.getStf();

//        if (null == androidDevice || !androidDevice.getStatus().equals(EnumPhoneStatus.Idle)) {
        if (null == androidDevice || !androidDevice.getStatus().equals(EnumPhoneStatus.Occupied)) {
            return false;
        }

        String cmd = "am startservice --user 0 -a jp.co.cyberagent.stf.ACTION_START -n jp.co.cyberagent.stf/.Service";

        JadbDevice jadbDevice = androidDevice.getJadbDevice();

        if (!jadbDevice.getState().equals(JadbDevice.State.Device)) {
            return false;
        }

        log.info("启动stf service");

        InputStream inputStream = jadbDevice.executeShell(cmd);

        int c = 0;

        while ((c = inputStream.read()) != -1) {
            System.out.print((char) c);
        }

        stf.setStarted(true);
        return true;
    }

    @Override
    public boolean forwardSocket(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        int port = NetWorkUtils.allocatePort();
        ExecutedResult executedResult = localCommandExecutor.executeCommand("adb forward tcp:" + port + " localabstract:stfservice", 10000);

        if (executedResult.getExitCode() == 0) {
            log.info("本地端口映射到stf service, 端口：{}", port);
        } else {
            log.info("本地端口映射到stf service失败");
            return false;
        }

        Socket socket = null;

        try {
            socket = NetWorkUtils.createLocalSocket(port);
            log.info("连接stf service端口,{}", port);
        } catch (IOException e) {
            log.error("{}", e);
            return false;
        }

        STF stf = androidDevice.getStf();
        stf.setSocket(socket);
        stf.setPort(port);
        return true;
    }

    @Override
    public void receiveCmd(String serial) {
    }

    @Override
    public void sendInfo(String serial) throws IOException {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return;
        }

        STF stf = androidDevice.getStf();

        if (null == stf) {
            return;
        }

        Socket socket = stf.getSocket();

        if (null == socket) {
            return;
        }

        /////////////////////////////////////////////////// SdStatus
        Wire.GetSdStatusRequest.Builder getSdStatusRequestBuilder = Wire.GetSdStatusRequest.newBuilder();
        Wire.Envelope envelope1 = Wire.Envelope.newBuilder().setType(Wire.MessageType.GET_SD_STATUS)
                .setMessage(getSdStatusRequestBuilder.build().toByteString())
                .setId(0)
                .build();
        envelope1.writeDelimitedTo(socket.getOutputStream());

        /////////////////////////////////////////////////// wifi
        Wire.GetWifiStatusRequest.Builder wifiStatusRequestBuilder = Wire.GetWifiStatusRequest.newBuilder();
        Wire.Envelope envelope3 = Wire.Envelope.newBuilder().setType(Wire.MessageType.GET_WIFI_STATUS)
                .setMessage(wifiStatusRequestBuilder.build().toByteString())
                .setId(0)
                .build();
        envelope3.writeDelimitedTo(socket.getOutputStream());

        /////////////////////////////////////////////////// display
        Wire.GetDisplayRequest.Builder builder = Wire.GetDisplayRequest.newBuilder();

        Wire.Envelope envelope4 = Wire.Envelope.newBuilder().setType(Wire.MessageType.GET_DISPLAY)
                .setMessage(builder.setId(0).build().toByteString())
                .setId(0)
                .build();

        envelope4.writeDelimitedTo(socket.getOutputStream());

        /////////////////////////////////////////////////// property
        Wire.GetPropertiesRequest.Builder propertiesRequestBuilder = Wire.GetPropertiesRequest.newBuilder();
        propertiesRequestBuilder.addAllProperties(Arrays.asList("imei", "network", "imsi", "phoneNumber", "iccid", "operator"));
        Wire.Envelope envelope5 = Wire.Envelope.newBuilder().setType(Wire.MessageType.GET_PROPERTIES)
                .setMessage(propertiesRequestBuilder.build().toByteString())
                .setId(1)
                .build();

        envelope5.writeDelimitedTo(socket.getOutputStream());

        Wire.GetRootStatusRequest.Builder rootStatusRequestBuilder = Wire.GetRootStatusRequest.newBuilder();
        Wire.Envelope envelope6 = Wire.Envelope.newBuilder().setType(Wire.MessageType.GET_ROOT_STATUS)
                .setMessage(rootStatusRequestBuilder.build().toByteString())
                .build();

        envelope6.writeDelimitedTo(socket.getOutputStream());
    }

    @Override
    public void readSTFInfo(String serial) throws IOException {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return;
        }

        STF stf = androidDevice.getStf();

        if (null == stf) {
            return;
        }

        Socket socket = stf.getSocket();

        if (null == socket) {
            return;
        }

        Session session = androidDevice.getWebSocketSession();

        while (true) {
            Wire.Envelope envelope = Wire.Envelope.parseDelimitedFrom(socket.getInputStream());

            if (null == envelope) {
                return;
            }

            Wire.MessageType messageType = envelope.getType();
            ByteString message = envelope.getMessage();

            switch (messageType) {
                case EVENT_BATTERY: // 电池
                    Wire.BatteryEvent batteryEvent = Wire.BatteryEvent.parseFrom(message);
                    BatteryMsg batteryMsg = BatteryMsg.builder()
                            .type("batteryMsg")
                            .health(batteryEvent.getHealth())
                            .level(batteryEvent.getLevel())
                            .scale(batteryEvent.getScale())
                            .source(batteryEvent.getSource())
                            .status(batteryEvent.getStatus())
                            .temp(batteryEvent.getTemp())
                            .voltage(batteryEvent.getVoltage()).build();

                    log.info("发送电池信息");

                    try {
                        session.getBasicRemote().sendObject(batteryMsg);
                    } catch (EncodeException e) {
                        log.error("{}", e);
                    }
                    break;
                case EVENT_CONNECTIVITY: // 网络
                    Wire.ConnectivityEvent connectivityEvent = Wire.ConnectivityEvent.parseFrom(message);
                    NetWorkMsg netWorkMsg = NetWorkMsg.builder()
                            .type("netWorkMsg")
                            .connected(connectivityEvent.getConnected())
                            .failover(connectivityEvent.getFailover())
                            .roaming(connectivityEvent.getRoaming())
                            .subNetworkType(connectivityEvent.getSubtype())
                            .subNetworkType(connectivityEvent.getSubtype())
                            .build();

                    log.info("发送网络信息");

                    try {
                        session.getBasicRemote().sendObject(netWorkMsg);
                    } catch (EncodeException e) {
                        log.error("{}", e);
                    }
                    break;
                case EVENT_ROTATION:
                    Wire.RotationEvent rotationEvent = Wire.RotationEvent.parseFrom(message);
                    System.out.println(rotationEvent.getRotation());
                    break;
                case EVENT_AIRPLANE_MODE: // 飞行模式
                    Wire.AirplaneModeEvent airplaneModeEvent = Wire.AirplaneModeEvent.parseFrom(message);
                    NetWorkMsg netWorkMsg1 = NetWorkMsg.builder()
                            .type("netWorkMsg")
                            .airPlaneMode(airplaneModeEvent.getEnabled())
                            .build();

                    log.info("发送网络信息");

                    try {
                        session.getBasicRemote().sendObject(netWorkMsg1);
                    } catch (EncodeException e) {
                        e.printStackTrace();
                    }
                    break;
                case EVENT_BROWSER_PACKAGE:
                    Wire.BrowserPackageEvent browserPackageEvent = Wire.BrowserPackageEvent.parseFrom(message);
                    System.out.println(browserPackageEvent.getAppsCount());
                    System.out.println(browserPackageEvent.getSelected());
                    break;
                case EVENT_PHONE_STATE:
                    Wire.PhoneStateEvent phoneStateEvent = Wire.PhoneStateEvent.parseFrom(message);
                    System.out.println(phoneStateEvent.getManual());
                    System.out.println(phoneStateEvent.getOperator());
                    System.out.println(phoneStateEvent.getState());
                    break;
                case DO_TYPE:
                case DO_IDENTIFY:
                case DO_WAKE:
                case DO_KEYEVENT:
                case DO_REMOVE_ACCOUNT:
                case DO_ADD_ACCOUNT_MENU:
                case GET_PROPERTIES:
                    Wire.GetPropertiesResponse propertiesResponse = Wire.GetPropertiesResponse.parseFrom(message);
                    List<Wire.Property> properties = propertiesResponse.getPropertiesList();
                    SIMMsg.SIMMsgBuilder simMsgBuilder = SIMMsg.builder().type("simMsg");

                    if (null != properties) {
                        for (Wire.Property property : properties) {
                            switch (property.getName()) {
                                case "imei":
                                    simMsgBuilder.imei(property.getValue());
                                    break;
                                case "network":
                                    simMsgBuilder.network(property.getValue());
                                    break;
                                case "iccid":
                                    simMsgBuilder.iccid(property.getValue());
                                    break;
                                case "imsi":
                                    simMsgBuilder.imsi(property.getValue());
                                    break;
                                case "operator":
                                    simMsgBuilder.operator(property.getValue());
                                    break;
                                case "phoneNumber":
                                    simMsgBuilder.phoneNumber(property.getValue());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    log.info("发送sim信息");

                    try {
                        session.getBasicRemote().sendObject(simMsgBuilder.build());
                    } catch (EncodeException e) {
                        e.printStackTrace();
                    }
                    break;
                case GET_DISPLAY: // 播放
                    Wire.GetDisplayResponse displayResponse = Wire.GetDisplayResponse.parseFrom(message);
                    DisplayMsg displayMsg = DisplayMsg.builder()
                            .type("displayMsg")
                            .density(displayResponse.getDensity())
                            .fps(displayResponse.getFps())
                            .height(displayResponse.getHeight())
                            .rotation(displayResponse.getRotation())
                            .secure(displayResponse.getSecure())
                            .success(displayResponse.getSuccess())
                            .width(displayResponse.getWidth())
                            .xdpi(displayResponse.getXdpi())
                            .ydpi(displayResponse.getYdpi())
                            .build();

                    // 设置设备高度、宽度
                    androidDevice.setRealHeight(displayResponse.getHeight());
                    androidDevice.setRealWidth(displayResponse.getWidth());

                    log.info("发送display信息");

                    try {
                        session.getBasicRemote().sendObject(displayMsg);
                    } catch (EncodeException e) {
                        log.error("{}", e);
                    }
                    break;
                case GET_VERSION:
                    Wire.GetVersionResponse versionResponse = Wire.GetVersionResponse.parseFrom(message);
                    System.out.println(versionResponse.getSuccess());
                    System.out.println(versionResponse.getVersion());
                    break;
                case GET_ACCOUNTS:
                case GET_BROWSERS:
                case GET_BLUETOOTH_STATUS:
                case GET_RINGER_MODE:
                case GET_ROOT_STATUS:
                    Wire.GetRootStatusResponse rootStatusResponse = Wire.GetRootStatusResponse.parseFrom(message);
                    System.out.println(rootStatusResponse.getStatus());
                    System.out.println(rootStatusResponse.getSuccess());
                    break;
                case GET_CLIPBOARD:
                case GET_SD_STATUS:
                    Wire.GetSdStatusResponse sdStatusResponse = Wire.GetSdStatusResponse.parseFrom(message);
                    System.out.println(sdStatusResponse.getMounted());
                    System.out.println(sdStatusResponse.getSuccess());
                    break;
                case GET_WIFI_STATUS:
                    Wire.GetWifiStatusResponse wifiStatusResponse = Wire.GetWifiStatusResponse.parseFrom(message);
                    System.out.println(wifiStatusResponse.getStatus());
                    System.out.println(wifiStatusResponse.getSuccess());
                    break;
                case SET_BLUETOOTH_ENABLED:
                case SET_CLIPBOARD:
                case SET_KEYGUARD_STATE:
                case SET_MASTER_MUTE:
                case SET_RINGER_MODE:
                case SET_ROTATION:
                case SET_WAKE_LOCK:
                case SET_WIFI_ENABLED:
                default:
                    break;

            }
        }
    }

    @Async
    @Override
    public void launch(String serial) throws IOException, JadbException {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return;
        }

        STF stf = androidDevice.getStf();

        if (null != stf && true == stf.isStarted()) {
            return;
        }

        boolean result = install(serial);

        if (false == result) {
            return;
        }

        result = start(serial);

        if (false == result) {
            return;
        }

        result = forwardSocket(serial);

        if (false == result) {
            return;
        }

        sendInfo(serial);

        readSTFInfo(serial);
    }

    @Override
    public void reset(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return;
        }

        STF stf = androidDevice.getStf();

        if (null == stf) {
            return;
        }

        Socket socket = stf.getSocket();

        if (null != socket) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("{}", e);
            }
        }

        InputStream inputStream = stf.getInputStream();

        if (null != inputStream) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("{}", e);
            }
        }

        Session session = androidDevice.getWebSocketSession();

        if (null != session) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("{}", e);
            }
        }

        androidDevice.setWebSocketSession(null);
    }

    @Override
    public boolean started(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        Predicate<AndroidDevice> predicate = x -> {
            return  null != androidDevice
                    && null != androidDevice.getStf()
                    && androidDevice.getStf().isStarted()
                    && null != androidDevice.getStf().getSocket();
        };

        return WaitUtils.wait(predicate, androidDevice, 30);
    }

}
