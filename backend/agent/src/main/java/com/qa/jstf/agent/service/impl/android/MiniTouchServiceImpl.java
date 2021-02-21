package com.qa.jstf.agent.service.impl.android;

import com.qa.jstf.agent.entity.android.AndroidDevice;
import com.qa.jstf.agent.entity.android.MiniTouch;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.MiniTouchService;
import com.qa.jstf.agent.utils.NetWorkUtils;
import com.qa.jstf.agent.utils.lcmd.ExecutedResult;
import com.qa.jstf.agent.utils.lcmd.LocalCommandExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

@Slf4j
@Service
public class MiniTouchServiceImpl implements MiniTouchService {

    @Autowired
    DeviceRepo deviceRepo;

    @Autowired
    LocalCommandExecutor localCommandExecutor;

    @Override
    public boolean install(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        MiniTouch.MiniTouchBuilder builder = MiniTouch.builder();
        androidDevice.setMiniTouch(builder.build());
        return true;
    }

    @Async
    @Override
    public void start(String serial) throws IOException, JadbException {
        log.info("启动miniTouch");
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        JadbDevice jadbDevice = androidDevice.getJadbDevice();
        InputStream inputStream = jadbDevice.executeShell("/data/local/tmp/minitouch");

        MiniTouch miniTouch = androidDevice.getMiniTouch();
        miniTouch.setStarted(true);
        miniTouch.setInputStream(inputStream);

        int c = 0;

        while ((c = inputStream.read()) != -1) {
            System.out.print((char) c);
        }
    }

    @Override
    public void stop(String serial) throws IOException {
        log.info("停止mini touch");
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        MiniTouch miniTouch = androidDevice.getMiniTouch();

        if (null != miniTouch.getInputStream()) {
            miniTouch.getInputStream().close();
        }
    }

    @Override
    public boolean started(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        MiniTouch miniTouch = androidDevice.getMiniTouch();

        for (int i=0;i<10;i++) {
            if( true == miniTouch.isStarted()) {
                return true;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        log.error("启动mini touch失败，{}", serial);
        return false;
    }

    @Override
    public boolean forwardSocket(String serial) {
        int port = NetWorkUtils.allocatePort();
        ExecutedResult executedResult = localCommandExecutor.executeCommand("adb forward tcp:" + port + " localabstract:minitouch", 10000);

        if (executedResult.getExitCode() == 0) {
            log.info("本地端口映射到miniTouch, 端口：{}", port);
        } else {
            log.error("本地端口映射到miniTouch失败");
            return false;
        }

        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        MiniTouch miniTouch = androidDevice.getMiniTouch();
        miniTouch.setPort(port);
        return true;
    }

    @Async
    @Override
    public void connectSocket(String serial) throws IOException{
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        MiniTouch miniTouch = androidDevice.getMiniTouch();

        log.info("连接miniTouch端口,{}", miniTouch.getPort());

        Socket socket = NetWorkUtils.createLocalSocket(miniTouch.getPort());
        miniTouch.setSocket(socket);
        miniTouch.setSocketConnected(true);
        miniTouch.setBufferedWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        InputStream inputStream = socket.getInputStream();

        int c = 0;

        while ((c = inputStream.read()) != -1) {
            System.out.print((char) c);
        }
    }

    @Override
    public void forwardCommand(String serial, String command) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null != androidDevice && EnumPhoneStatus.Occupied.equals(androidDevice.getStatus())) {
            MiniTouch miniTouch = androidDevice.getMiniTouch();
            BufferedWriter bufferedWriter = miniTouch.getBufferedWriter();

            try {
                bufferedWriter.write(command);
                bufferedWriter.flush();
            } catch (IOException e) {
                log.error("{}", e);
            }
        }
    }

    @Override
    public boolean reset(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return true;
        }

        MiniTouch miniTouch = androidDevice.getMiniTouch();

        if (null == miniTouch) {
            return true;
        }

        miniTouch.setPort(null);

        Socket socket = miniTouch.getSocket();

        if (null != socket) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("{}", e);
            }

            miniTouch.setSocket(null);
        }

        InputStream inputStream = miniTouch.getInputStream();

        if (null != inputStream) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("{}", e);
            }
        }

        BufferedWriter bufferedWriter = miniTouch.getBufferedWriter();

        if (null != bufferedWriter) {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                log.error("{}", e);
            }
        }

        return true;
    }

}
