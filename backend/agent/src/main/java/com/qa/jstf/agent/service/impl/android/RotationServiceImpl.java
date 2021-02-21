package com.qa.jstf.agent.service.impl.android;

import com.qa.jstf.agent.entity.android.AndroidDevice;
import com.qa.jstf.agent.entity.message.RotationMsg;
import com.qa.jstf.agent.entity.android.RotationWatcher;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.AndroidDeviceService;
import com.qa.jstf.agent.service.MiniCapService;
import com.qa.jstf.agent.service.RotationService;
import com.qa.jstf.agent.utils.WaitUtils;
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
import java.util.function.Predicate;

@Slf4j
@Service
public class RotationServiceImpl implements RotationService {

    @Autowired
    DeviceRepo deviceRepo;

    @Autowired
    MiniCapService miniCapService;

    @Autowired
    AndroidDeviceService androidDeviceService;

    @Override
    public String install(String serial) throws IOException, JadbException {

        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice || !androidDevice.getStatus().equals(EnumPhoneStatus.Occupied)) {
            return null;
        }

        String cmd = "pm path jp.co.cyberagent.stf.rotationwatcher \\| tr -d '\\r' \\| cut -d: -f 2";

        JadbDevice jadbDevice = androidDevice.getJadbDevice();

        if (!jadbDevice.getState().equals(JadbDevice.State.Device)) {
            return null;
        }

        InputStream inputStream = jadbDevice.executeShell(cmd);

        StringBuilder stringBuilder = new StringBuilder();

        int c = 0;

        while ((c = inputStream.read()) != -1) {
            stringBuilder.append((char) c);
        }

        String path = stringBuilder.toString();

        if (null != path && path.length() > 0) {
            log.info("rotationWatcher app路径: {}", path);
            return path;
        } else {
            log.warn("rotation没有安装");
            return null;
        }
    }

    @Async
    @Override
    public void start(String serial, Session session) throws IOException, JadbException {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null != androidDevice) {
            if (null == androidDevice.getRotationWatcher()) {
                log.info("创建rotation");
                RotationWatcher.RotationWatcherBuilder builder = RotationWatcher.builder().started(false);
                androidDevice.setRotationWatcher(builder.build());
            }
        } else {
            log.error("设备不存在, {}", serial);
            return;
        }

        String path = install(serial);

        if (null == path) {
            return;
        }

        JadbDevice jadbDevice = deviceRepo.getAndroidDeviceMap().get(serial).getJadbDevice();

        String cmd = "app_process -Djava.class.path=/data/app/jp.co.cyberagent.stf.rotationwatcher-1.apk /system/bin jp.co.cyberagent.stf.rotationwatcher.RotationWatcher";

        log.info("rotation启动命令: {}", cmd);

        InputStream inputStream = jadbDevice.executeShell(cmd);

        androidDevice.getRotationWatcher().setInputStream(inputStream);

        StringBuilder stringBuilder = new StringBuilder();

        int c = 0;

        while ((c = inputStream.read()) != -1) {
            if (!System.getProperty("line.separator").equals(String.valueOf((char) c))) {
                stringBuilder.append((char) c);
            } else {
                Integer rotation = Integer.valueOf(stringBuilder.toString());
                Integer preRotation = androidDevice.getRotationWatcher().getRotation();
                androidDevice.getRotationWatcher().setRotation(rotation);
                androidDevice.getRotationWatcher().setStarted(true);

                log.info("rotation: {}", rotation);

                // 重启mini cap
                if (null != preRotation && preRotation != rotation) {
                    androidDevice.getMiniCap().setRestarting((short)1);
                    androidDeviceService.restartMiniCap(serial);
                }

                RotationMsg rotationMsg = RotationMsg.builder().type("rotationMsg").rotation(rotation).build();

                try {
                    session.getBasicRemote().sendObject(rotationMsg);
                } catch (EncodeException e) {
                    log.error("{}", e);
                }

                /*RotationInfo rotationInfo = RotationInfo.builder().type(EnumAndroidInfo.Rotation).rotation(rotation).build();
                ObjectMapper mapper = new ObjectMapper();
                String str = mapper.writeValueAsString(rotationInfo);
                session.getBasicRemote().sendObject(str);*/
                stringBuilder = new StringBuilder();
            }
        }
    }

    @Override
    public boolean started(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        Predicate<AndroidDevice> predicate = x -> {
            return null != x
                    && null != x.getRotationWatcher()
                    && null != x.getRotationWatcher().getInputStream();
        };

        return WaitUtils.wait(predicate, androidDevice, 30);
    }

    @Override
    public void reset(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return;
        }

        RotationWatcher rotationWatcher = androidDevice.getRotationWatcher();

        if (null != rotationWatcher && null != rotationWatcher.getInputStream()) {
            try {
                rotationWatcher.getInputStream().close();
            } catch (IOException e) {
                log.error("{}", e);
            }
        }
    }
}
