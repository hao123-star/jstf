package com.qa.jstf.agent.service.impl.ios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.jstf.agent.entity.enumaration.EnumPhoneStatus;
import com.qa.jstf.agent.entity.ios.IosDevice;
import com.qa.jstf.agent.entity.ios.Mjpeg;
import com.qa.jstf.agent.entity.message.ImgMsg;
import com.qa.jstf.agent.entity.message.WebSocketMsg;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.ImgService;
import com.qa.jstf.agent.service.IosDeviceService;
import com.qa.jstf.agent.utils.SocketUtils;
import com.qa.jstf.agent.utils.WaitUtils;
import com.qa.jstf.agent.utils.lcmd.LocalCommandExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.websocket.EncodeException;
import java.io.*;
import java.net.URL;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;

@Slf4j
@Service
public class MjpegService implements ImgService {

    @Autowired
    DeviceRepo deviceRepo;

    @Autowired
    LocalCommandExecutor localCommandExecutor;

    @Autowired
    IosDeviceService iosDeviceService;

    @Async
    @Override
    public void forwardSocket(String serial) {
        try {
            IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);
            Integer mjpegPort = SocketUtils.allocatePort();
            Process process = localCommandExecutor.executeCommand("iproxy " + mjpegPort + " 9100" );
            URL url = new URL("http://localhost:" + mjpegPort + "/?action=stream");

            Mjpeg mjpeg = Mjpeg.builder().port(mjpegPort)
                    .url(url)
                    .imgQueue(new LinkedBlockingDeque<>())
                    .running(true)
                    .process(process)
                    .build();

            iosDevice.setMjpeg(mjpeg);

            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            log.error("Mjpeg转发端口异常，{}", e);
            iosDeviceService.reset(serial);
        }
    }

    @Override
    public boolean removeSocket(String serial) {
        IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);

        if (null == iosDevice) {
            return true;
        }

        Mjpeg mjpeg = iosDevice.getMjpeg();

        if (null == mjpeg) {
            return true;
        }

        if (null != mjpeg.getProcess()) {
            mjpeg.getProcess().destroy();
        }

        return true;
    }

    @Async
    @Override
    public void receiveImg(String serial) {
        try {
            IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);
            Predicate<IosDevice> predicate = x -> x.getMjpeg() != null;
            boolean result = WaitUtils.wait(predicate, iosDevice,30);

            if (false == result) {
                log.error("mjpeg启动失败, serial: {}", serial);
                iosDeviceService.reset(serial);
                return;
            }

            Mjpeg mjpeg = iosDevice.getMjpeg();
            BlockingDeque<byte[]> imgQueue = mjpeg.getImgQueue();
            URL url = mjpeg.getUrl();
            InputStream urlStream = url.openStream();

            String CONTENT_LENGTH = "Content-Length: ";

            while (true == mjpeg.isRunning()) {
                int currByte = -1;

                String header = null;
                // build headers
                // the DCS-930L stops it's headers

                boolean captureContentLength = false;
                StringWriter contentLengthStringWriter = new StringWriter(128);
                StringWriter headerWriter = new StringWriter(128);

                int contentLength = 0;

                while ((currByte = urlStream.read()) > -1) {
                    if (captureContentLength) {
                        if (currByte == 10 || currByte == 13) {
                            contentLength = Integer.parseInt(contentLengthStringWriter.toString());
                            break;
                        }
                        contentLengthStringWriter.write(currByte);

                    } else {
                        headerWriter.write(currByte);
                        String tempString = headerWriter.toString();
                        int indexOf = tempString.indexOf(CONTENT_LENGTH);
                        if (indexOf > 0) {
                            captureContentLength = true;
                        }
                    }
                }

                // 255 indicates the start of the jpeg image
                while ((urlStream.read()) != 255) {
                    // just skip extras
                }

                // rest is the buffer
                byte[] imageBytes = new byte[contentLength + 1];
                // since we ate the original 255 , shove it back in
                imageBytes[0] = (byte) 255;
                int offset = 1;
                int numRead = 0;
                while (offset < imageBytes.length
                        && (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
                    offset += numRead;
                }

                imgQueue.add(imageBytes);
            }
        } catch (IOException e) {
            log.error("接受图片发生异常，{}", e);
//            deviceService.reset(serial);
        }
    }

    @Async
    @Override
    public void sendImg(String serial) {
        try {
            IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);

            if (null == iosDevice) {
                log.error("发送图片，设备不存在，serial: {}", serial);
                return;
            }

            Predicate<IosDevice> predicate = x -> x.getMjpeg() != null;
            boolean result = WaitUtils.wait(predicate, iosDevice,30);

            if (false == result) {
                log.error("mjpeg启动失败, serial: {}", serial);
                iosDeviceService.reset(serial);
                return;
            }

            Mjpeg mjpeg = iosDevice.getMjpeg();
            BlockingDeque<byte []> imgQueue = mjpeg.getImgQueue();

            while (null != iosDevice && EnumPhoneStatus.Occupied.equals(iosDevice.getStatus())) {
                synchronized (iosDevice.getWebSocketSession()) {
                    iosDevice.getWebSocketSession().getBasicRemote().sendObject(imgQueue.take());
                }
            }
        } catch (InterruptedException | IOException | EncodeException e) {
            log.warn("发送图片发生异常，{}", e);
//            deviceService.reset(serial);
        }
    }

    @Override
    public void reset(String serial) {
        IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);

        if (null == iosDevice) {
            return;
        }

        Mjpeg mjpeg = iosDevice.getMjpeg();

        if (null != mjpeg) {
            Process process = mjpeg.getProcess();

            if (null != process) {
                process.destroy();
            }

            mjpeg.setProcess(null);
            mjpeg.setPort(null);
            mjpeg.setRunning(false);
            BlockingDeque<byte []> imgQueue = mjpeg.getImgQueue();

            if (null != imgQueue) {
                imgQueue.clear();
                mjpeg.setImgQueue(null);
            }
        }
    }

    public static void main(String[] args) {
        WebSocketMsg webSocketMsg = ImgMsg.builder()
                .type("img")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(webSocketMsg));

            ImgMsg i = mapper.readValue("{\"type\":\"imgMsg\",\"msgType\":\"img\",\"rotation\":0,\"bytes\":null}\n", ImgMsg.class);
            System.out.println();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
