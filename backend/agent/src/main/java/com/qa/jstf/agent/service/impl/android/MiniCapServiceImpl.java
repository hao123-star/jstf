package com.qa.jstf.agent.service.impl.android;

import com.qa.jstf.agent.entity.android.AndroidDevice;
import com.qa.jstf.agent.entity.android.Banner;
import com.qa.jstf.agent.entity.android.MiniCap;
import com.qa.jstf.agent.entity.android.RotationWatcher;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.MiniCapService;
import com.qa.jstf.agent.utils.BytesUtils;
import com.qa.jstf.agent.utils.NetWorkUtils;
import com.qa.jstf.agent.utils.WaitUtils;
import com.qa.jstf.agent.utils.lcmd.LocalCommandExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;

import com.qa.jstf.agent.utils.lcmd.ExecutedResult;
import javax.websocket.Session;

@Slf4j
@Service
public class MiniCapServiceImpl implements MiniCapService {
    @Autowired
    DeviceRepo deviceRepo;

    /*@Autowired
    WebSocketSessionRepo webSocketSessionRepo;*/

    @Autowired
    LocalCommandExecutor localCommandExecutor;

    @Autowired
    JadbConnection jadbConnection;

    @Override
    public boolean install(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        androidDevice.setMiniCap(MiniCap.builder().restarting((short)0).imgQueue(new LinkedBlockingDeque<>()).build());
        return true;
    }

    // 启动miniCap
    @Async
    @Override
    public void start(String serial) throws IOException, JadbException {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        RotationWatcher rotationWatcher = androidDevice.getRotationWatcher();

        for (int i=0;i<20;i++) {
            if (null != rotationWatcher && true == rotationWatcher.isStarted() && null != androidDevice.getRealWidth() && null != androidDevice.getRealHeight()) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (19 == i) {
                log.error("rotation service启动失败...");
                return;
            }
        }

        JadbDevice jadbDevice = androidDevice.getJadbDevice();
        log.info("启动miniCap, rotation: {}", androidDevice.getRotationWatcher().getRotation());
        InputStream inputStream = jadbDevice.executeShell("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap",
                "-P", androidDevice.getRealWidth() + "x" + androidDevice.getRealHeight() + "@" + androidDevice.getRealWidth() + "x" + androidDevice.getRealHeight() +"/" + androidDevice.getRotationWatcher().getRotation());
        MiniCap miniCap = androidDevice.getMiniCap();
        miniCap.setInputStream(inputStream);
        miniCap.setStarted(true);

        int c = 0;

        while (true == miniCap.isStarted() && (c = inputStream.read()) != -1) {
            System.out.print((char) c);
        }
    }

    @Override
    public boolean started(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        MiniCap miniCap = androidDevice.getMiniCap();
        Predicate<MiniCap> predicate = x -> true == x.isStarted();
        return WaitUtils.wait(predicate, miniCap, 30);

        /*for (int i=0;i<10;i++) {
            if( true == miniCap.isStarted()) {
                return true;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        log.error("mini cap启动失败");
        return false;
        */
    }

    @Override
    public void stop(String serial) throws IOException{
        log.info("停止mini cap");

        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if ( null != androidDevice.getMiniCap().getInputStream()) {
            androidDevice.getMiniCap().getInputStream().close();
        }
    }

    @Override
    public boolean removeForward(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        MiniCap miniCap = androidDevice.getMiniCap();

        if (null == miniCap || null == miniCap.getPort()) {
            return true;
        }

        ExecutedResult executeResult = localCommandExecutor.executeCommand("adb forward --remove tcp:" + miniCap.getPort(), 10000);

        if (executeResult.getExitCode() == 0) {
            log.info("移除本地端口映射到miniCap, 端口：{}", miniCap.getPort());
            return true;
        } else {
            log.info("移除本地端口映射到miniCap失败");
            return false;
        }
    }

    // 本地端口映射到miniCap
    @Override
    public boolean forwardSocket(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        int port = NetWorkUtils.allocatePort();
        ExecutedResult executeResult = localCommandExecutor.executeCommand("adb forward tcp:" + port + " localabstract:minicap", 10000);

        if (executeResult.getExitCode() == 0) {
            log.info("本地端口映射到miniCap, 端口：{}", port);
        } else {
            log.info("本地端口映射到miniCap失败");
            return false;
        }

        Socket socket = null;

        try {
            socket = NetWorkUtils.createLocalSocket(port);
            log.info("连接miniCap端口,{}", port);
        } catch (IOException e) {
            log.error("{}", e);
            return false;
        }

        MiniCap miniCap = androidDevice.getMiniCap();
        miniCap.setSocket(socket);
        miniCap.setPort(socket.getPort());
        return true;
    }

    @Async
    @Override
    public void receiveImg(String serial) throws IOException {
        log.info("当前线程接受mini cap图片：{}", Thread.currentThread().getName());

        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null == androidDevice) {
            return;
        }

        MiniCap miniCap = androidDevice.getMiniCap();

        BlockingDeque<byte []> blockingDeque = miniCap.getImgQueue();

        Socket socket = null;
        InputStream stream = null;

        int readBannerBytes = 0; //

        int bannerLength = 2; //

        int readFrameBytes = 0; //

        int frameBodyLength = 0; // 图片长度

        byte[] frameBody = new byte[0];

        byte [] buffer = new byte[4096];

        Banner banner = Banner.builder().build();

        socket = androidDevice.getMiniCap().getSocket();
        stream = socket.getInputStream();
        int readLen = stream.read(buffer);
        int len = buffer.length;

        if (len != readLen) {
            buffer = BytesUtils.subByteArray(buffer, 0, readLen);
        }

        len = buffer.length;

        log.info("接受miniCap图片, {}", serial);

        while (null != androidDevice && androidDevice.getStatus().equals(EnumPhoneStatus.Occupied)) {
            for (int cursor=0;cursor<len;) {
                int byte10 = buffer[cursor] & 0xff;

                if (readBannerBytes < bannerLength) { // 处理前24个字节
                    switch (readBannerBytes) {
                        case 0:
                            // version
                            banner.setVersion(byte10);
                            break;
                        case 1:
                            // length
                            bannerLength = byte10;
                            banner.setLength(byte10);
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            // pid
                            int pid = banner.getPid();
                            pid += (byte10 << ((readBannerBytes - 2) * 8)) >>> 0;
                            banner.setPid(pid);
                            break;
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                            // real width
                            int realWidth = banner.getReadWidth();
                            realWidth += (byte10 << ((readBannerBytes - 6) * 8)) >>> 0;
                            banner.setReadWidth(realWidth);
                            break;
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                            // real height
                            int realHeight = banner.getReadHeight();
                            realHeight += (byte10 << ((readBannerBytes - 10) * 8)) >>> 0;
                            banner.setReadHeight(realHeight);
                            break;
                        case 14:
                        case 15:
                        case 16:
                        case 17:
                            // virtual width
                            int virtualWidth = banner.getVirtualWidth();
                            virtualWidth += (byte10 << ((readBannerBytes - 14) * 8)) >>> 0;
                            banner.setVirtualWidth(virtualWidth);
                            break;
                        case 18:
                        case 19:
                        case 20:
                        case 21:
                            // virtual height
                            int virtualHeight = banner.getVirtualHeight();
                            virtualHeight += (byte10 << ((readBannerBytes - 18) * 8)) >>> 0;
                            banner.setVirtualHeight(virtualHeight);
                            break;
                        case 22:
                            // orientation
                            banner.setOrientation(byte10 * 90);
                            break;
                        case 23:
                            // quirks
                            banner.setQuirks(byte10);
                            break;
                    }

                    cursor += 1;
                    readBannerBytes += 1;
                } else if (readFrameBytes < 4){ // 处理图片长度
                    frameBodyLength += (byte10 << (readFrameBytes * 8)) >>> 0;
                    cursor += 1;
                    readFrameBytes += 1;
                } else { // 处理图片
                    if (len - cursor >= frameBodyLength) {
                        byte[] subByte = BytesUtils.subByteArray(buffer, cursor, cursor + frameBodyLength);

                        frameBody = BytesUtils.byteMerger(frameBody, subByte);

                        if ((frameBody[0] != -1) || frameBody[1] != -40) {
                            return;
                        }

                        if (miniCap.getRestarting() == (short)0) {
                            blockingDeque.add(frameBody);
                        } else { // 重启中
                            miniCap.setRestarting((short)2);
                            return;
                        }

                        cursor += frameBodyLength;
                        frameBodyLength = 0;
                        readFrameBytes = 0;
                        frameBody = new byte[0];
                    } else {
                        byte[] subByte = BytesUtils.subByteArray(buffer, cursor, len);
                        frameBody = BytesUtils.byteMerger(frameBody, subByte);
                        frameBodyLength -= (len - cursor);
                        readFrameBytes += (len - cursor);
                        cursor = len;
                    }
                }
            }

            buffer = new byte[4096];
            readLen = stream.read(buffer);
            len = buffer.length;

            if (len != readLen) {
                buffer = BytesUtils.subByteArray(buffer, 0, readLen);
            }

            len = buffer.length;
        }
    }

    @Async
    @Override
    public void sendImg(String serial) {
        log.info("当前线程发送mini cap图片：{}", Thread.currentThread().getName());

        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);
        Session session = androidDevice.getWebSocketSession();

        if (null == androidDevice) { // 设备连接已断开
            log.error("手机连接已断开");
            return;
        }

        MiniCap miniCap = androidDevice.getMiniCap();
        BlockingDeque<byte []> imgQueue = miniCap.getImgQueue();

        byte[] frameBody = new byte[0];

        log.info("开始发送mini cap图片:{}", serial);

        while (null != androidDevice && androidDevice.getStatus().equals(EnumPhoneStatus.Occupied)) {

            try {
                frameBody = imgQueue.take();
            } catch (InterruptedException e) {
                log.error("从队列中取图片失败, {}", serial);
            }

            try {
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(frameBody));
            } catch (IOException e) {
                log.error("websocket发送异常, {}", serial);
            }
        }
    }

    @Override
    public boolean reset(String serial) {
        AndroidDevice androidDevice = deviceRepo.getAndroidDeviceMap().get(serial);

        if (null != androidDevice) {
            MiniCap miniCap = androidDevice.getMiniCap();

            if (null != miniCap) {
                miniCap.setPort(0);
                Socket socket = miniCap.getSocket();

                if (null != socket) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        log.error("关闭socket，{}, {}", serial, e);
                    }

                    miniCap.setSocket(null);
                }

                BlockingDeque<byte []> imgQueue = miniCap.getImgQueue();

                if (null != imgQueue) {
                    imgQueue.clear();
                    miniCap.setImgQueue(null);
                }

                InputStream inputStream = miniCap.getInputStream();

                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("关闭inputStream，{}, {}", serial, e);
                    }

                    miniCap.setInputStream(null);
                }
            }
        }

        return true;
    }
}
