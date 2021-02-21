package com.qa.jstf.agent.service.impl.ios;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.jstf.agent.entity.ios.BatteryInfo;
import com.qa.jstf.agent.entity.ios.IosDevice;
import com.qa.jstf.agent.entity.ios.WebDriverAgent;
import com.qa.jstf.agent.entity.message.IosInfoMsg;
import com.qa.jstf.agent.entity.message.TouchMsg;
import com.qa.jstf.agent.repo.DeviceRepo;
import com.qa.jstf.agent.service.DeviceService;
import com.qa.jstf.agent.service.WebDriverAgentService;
import com.qa.jstf.agent.utils.LineUtils;
import com.qa.jstf.agent.utils.OKHttpClientUtils;
import com.qa.jstf.agent.utils.SocketUtils;
import com.qa.jstf.agent.utils.lcmd.LocalCommandExecutor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.websocket.EncodeException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class WebDriverAgentServiceImpl implements WebDriverAgentService {

    @Autowired
    DeviceRepo deviceRepo;

    @Autowired
    LocalCommandExecutor localCommandExecutor;

    @Autowired
    OKHttpClientUtils okHttpClientUtils;

    @Lazy
    @Resource(name="iosDeviceService")
    DeviceService deviceService;

    @Async
    @Override
    public void forwardSocket(String serial) {
        try {
            IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);
            Integer wdaPort = SocketUtils.allocatePort();
            Process process = localCommandExecutor.executeCommand("iproxy " + wdaPort + " 8100");
            URL url = new URL("http://localhost:" + wdaPort + "/");
            WebDriverAgent.WebDriverAgentBuilder builder = WebDriverAgent.builder();

            builder.port(wdaPort)
                    .url(url)
                    .process(process)
                    .touchMsgQueue(new LinkedBlockingQueue<>())
                    .running(true);
            iosDevice.setWebDriverAgent(builder.build());

            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;

            //
            IosInfoMsg.IosInfoMsgBuilder iosInfoMsgBuilder = IosInfoMsg.builder();
            String sessionId = getSession(url.toURI().toString());
            iosDevice.setSessionId(sessionId);

            Map<String, Integer> map = getScreenInfo(url.toURI().toString(), sessionId);
            iosDevice.setRealWidth(map.get("width"));
            iosDevice.setRealHeight(map.get("height"));

            iosInfoMsgBuilder.type("iosInfoMsg")
                    .realX(map.get("width"))
                    .realY(map.get("height"))
                    ;

            Integer rotation = getRotation(url.toURI().toString(), sessionId);
            iosInfoMsgBuilder.rotation(rotation);

            try {
                iosDevice.getWebSocketSession().getBasicRemote().sendObject(iosInfoMsgBuilder.build());
            } catch (IOException | EncodeException e) {
                log.error("{}", e);
            }

//            handleMouseMove2(serial);

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            log.error("WebDriverAgent转发端口异常，{}", e);
            deviceService.reset(serial);
        }
    }

    @Override
    public void click(String url, String session, Integer x, Integer y) {
        url = url + "session/" + session + "/wda/tap/0";
        Map<String, Object> formData = new HashMap<>();
        formData.put("x", x);
        formData.put("y", y);

        Response response = null;

        try {
            response = okHttpClientUtils.postJson(url, formData);
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }
    }

    @Override
    public void drag(String url, String session, Integer fromX, Integer fromY, Integer toX, Integer toY, Double duration) {
        url = url + "session/" + session + "/wda/dragfromtoforduration";

        DecimalFormat df  = new DecimalFormat("######0.00");

        Map<String, Object> formData = new HashMap<>();
        formData.put("fromX", fromX);
        formData.put("fromY", fromY);
        formData.put("toX", toX);
        formData.put("toY", toY);

        if (duration > 60d) {
            formData.put("duration", 60);
        } else if (duration < 0.5d) {
            formData.put("duration", 0.5);
        } else {
            formData.put("duration", Double.valueOf(df.format(duration)));
        }

        log.info("move from {}, {} to {},{}, {}", fromX, fromY, toX, toY, Double.valueOf(df.format(duration)));

        Response response = null;

        try {
            response = okHttpClientUtils.postJson(url, formData);
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }
    }

    @Override
    public String getSession(String url) {
        url = url + "session";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("capabilities", new HashMap<String, Object>());
        Response response = null;

        try {
            response = okHttpClientUtils.postJson(url, jsonData);
            ResponseBody responseBody = response.body();
            String body = responseBody.string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            return jsonNode.path("sessionId").asText();
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }

        return null;
    }

    @Override
    public BatteryInfo getBatterInfo(String url, String session) {
        url = url + "session/" + session + "/wda/batteryInfo";
        Response response = null;

        try {
            response = okHttpClientUtils.get(url);
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }

        return null;
    }

    @Override
    public Map<String, Integer> getScreenInfo(String url, String session) {
        url = url + "session/" + session + "/window/size";
        Map<String, Integer> map = new HashMap<>();

        Response response = null;

        try {
            response = okHttpClientUtils.get(url);
            ResponseBody responseBody = response.body();
            String body = responseBody.string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            jsonNode = jsonNode.path("value");
            map.put("width", jsonNode.path("width").asInt());
            map.put("height", jsonNode.path("height").asInt());
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }

        return map;
    }

    @Override
    public void changeOrientation(String url, String session, String orientation) {
        url = url + "session/" + session + "/orientation";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("orientation", orientation);

        Response response = null;

        try {
            response = okHttpClientUtils.postJson(url, jsonData);
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }
    }

    @Override
    public void simulateHomeScreen(String url) {
        url = url + "wda/homescreen";
        Map<String, Object> jsonData = new HashMap<>();
        Response response = null;

        try {
            response = okHttpClientUtils.postJson(url, jsonData);
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }
    }

    @Override
    public void setScreenShotQuality(String url, String session, String quality) {
        url = url + "session/" + session + "/appium/settings";
        Map<String, Object> settingMap = new HashMap<>();
        Map<String, Integer> qualityMap = new HashMap<>();
        settingMap.put("settings", qualityMap);
        int qua = 50;

        switch (quality) {
            case "low":
                qua = 40;
                break;
            case "medium":
                qua = 50;
                break;
            case "high":
                qua = 60;
                break;
            default:
                break;
        }

        qualityMap.put("FBMjpegServerScreenshotQuality", qua);

        Response response = null;

        try {
            response = okHttpClientUtils.postJson(url, settingMap);
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }
    }

    @Override
    public Integer getRotation(String url, String session) {
        url = url + "session/" + session + "/rotation";
        Response response = null;

        try {
             response = okHttpClientUtils.get(url);
            ResponseBody responseBody = response.body();
            String body = responseBody.string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            JsonNode value = jsonNode.path("value");
            int rotation = value.path("z").asInt();
            return rotation;
        } catch (IOException e) {
            log.error("{}", e);
        } finally {
            if (null != response) {
                response.close();
            }
        }

        return null;
    }

    @Override
    public void reset(String serial) {
        IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);

        if (null == iosDevice) {
            return;
        }

        WebDriverAgent webDriverAgent = iosDevice.getWebDriverAgent();

        if (null == webDriverAgent) {
            return;
        }

        Process process = webDriverAgent.getProcess();

        if (null != process) {
            process.destroy();
            webDriverAgent.setProcess(null);
        }

        webDriverAgent.setPort(null);
        webDriverAgent.setUrl(null);
        webDriverAgent.setRunning(false);
    }

    @Override
    public void handleCmd(String serial, TouchMsg touchMsg) throws URISyntaxException {
        IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);

        switch (touchMsg.getAction()) {
            case "mouseClick":
                click(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                        iosDevice.getSessionId(),
                        touchMsg.getX(),
                        touchMsg.getY());
                break;
            case "mouseMove":
                drag(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                        iosDevice.getSessionId(),
                        touchMsg.getFromY(), touchMsg.getFromY(),
                        touchMsg.getToX(), touchMsg.getToY(), touchMsg.getDuration());
            case "mouseUp":
                iosDevice.getWebDriverAgent().getTouchMsgQueue().add(touchMsg);
                break;
            case "homeScreen":
                simulateHomeScreen(iosDevice.getWebDriverAgent().getUrl().toURI().toString());
                break;
            case "changeQuality":
                setScreenShotQuality(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                        iosDevice.getSessionId(),
                        touchMsg.getQuality());
                break;
            default:
                break;
        }
    }

    @Async
    @Override
    public void handleMouseMove(String serial) throws URISyntaxException{
        IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);
        BlockingQueue<TouchMsg> touchMsgQueue = iosDevice.getWebDriverAgent().getTouchMsgQueue();

        while (true) {
            TouchMsg point1 = null;
            TouchMsg point2 = null;
            TouchMsg point3 = null;

            try {
                point1 = touchMsgQueue.take();
                log.info("point1 {}, {}, {}", point1.getAction(), point1.getX(), point1.getY());

                if (point1.getAction().equals("mouseUp")) {
                    continue;
                }

                point2 = touchMsgQueue.take();
                log.info("point2 {}, {}, {}", point2.getAction(), point2.getX(), point2.getY());

                if (point2.getAction().equals("mouseUp")) {
                    if (point1.getX().equals(point2.getX()) && point2.getY().equals(point2.getY())) {
                        continue;
                    }

                    if (point2.getX() > iosDevice.getRealWidth() || point2.getY() > iosDevice.getRealHeight()) {
                        continue;
                    }

                    log.info("mouse move from point1 {} {} to point2 {} {}", point1.getX(), point1.getY(), point2.getX(), point2.getY());

                    drag(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                            iosDevice.getSessionId(),
                            point1.getX(), point1.getY(),
                            point2.getX(), point2.getY(),
                            point2.getTime()-point1.getTime());
                    continue;
                } else { // mouse move
                    while ((point1.getX().equals(point2.getX()) || point1.getY().equals(point2.getY())) &&
                            point2.getX() < iosDevice.getRealWidth() && point2.getY() > iosDevice.getRealHeight()) {
                        log.info("skip point2 {}, {}", point2.getX(), point2.getY());
                        point2 = touchMsgQueue.take();
                    }

                    if (point2.getX() == iosDevice.getRealWidth() || point2.getY() == iosDevice.getRealHeight()) {
                        log.info("mouse move from point1 {} {} to point2 {} {}", point1.getX(), point1.getY(), point2.getX(), point2.getY());

                        drag(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                                iosDevice.getSessionId(),
                                point1.getX(), point1.getY(),
                                point2.getX(), point2.getY(),
                                point2.getTime()-point1.getTime());
                        continue;
                    }
                }

                point3 = touchMsgQueue.take();

                while (true == LineUtils.isLine(point1.getX(), point1.getY(),
                        point2.getX(), point2.getY(),
                        point3.getX(), point3.getY()) && !point3.getAction().equals("mouseUp")) {
                    log.info("skip point3 {}, {}", point3.getX(), point3.getY());
                    point3 = touchMsgQueue.take();
                }

                if (true == LineUtils.isLine(point1.getX(), point1.getY(),
                                            point2.getX(), point2.getY(),
                                            point3.getX(), point3.getY())) { // move from point1 to point3
                    log.info("mouse move from point1 {} {} to point3 {} {}", point1.getX(), point1.getY(), point3.getX(), point3.getY());

                    drag(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                            iosDevice.getSessionId(),
                            point1.getX(), point1.getY(),
                            point3.getX(), point3.getY(),
                            point3.getTime()-point1.getTime());
                } else { // move from point1 to point2
                    log.info("mouse move from point1 {} {} to point2 {} {}", point1.getX(), point1.getY(), point2.getX(), point2.getY());

                    drag(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                            iosDevice.getSessionId(),
                            point1.getX(), point1.getY(),
                            point2.getX(), point2.getY(),
                            point2.getTime()-point1.getTime());

                    log.info("mouse move from point2 {} {} to point3 {} {}", point2.getX(), point2.getY(), point3.getX(), point3.getY());

                    drag(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                            iosDevice.getSessionId(),
                            point2.getX(), point2.getY(),
                            point3.getX(), point3.getY(),
                            point3.getTime()-point2.getTime());
                }
            } catch (InterruptedException e) {
                log.error("鼠标移动异常...");
            }
        }
    }

    @Override
    public void handleMouseMove2(String serial) throws InterruptedException, URISyntaxException {
        IosDevice iosDevice = deviceRepo.getIosDeviceMap().get(serial);
        BlockingQueue<TouchMsg> touchMsgQueue = iosDevice.getWebDriverAgent().getTouchMsgQueue();

        while (true) {
            TouchMsg startPoint = null;
            TouchMsg fromPoint = null;
            TouchMsg toPoint = null;

            fromPoint = touchMsgQueue.take();
            startPoint = fromPoint;
            log.info("point1: {}, {}, {}, {}", fromPoint.getAction(), fromPoint.getX(), fromPoint.getY(), fromPoint.getTime());

            if (fromPoint.getAction().equals("mouseUp")) {
                continue;
            }

            toPoint = touchMsgQueue.take();
            log.info("point2: {}, {}, {}, {}", toPoint.getAction(), toPoint.getX(), toPoint.getY(), toPoint.getTime());

            if (toPoint.getAction().equals("mouseUp")) {
                if (fromPoint.getX().equals(toPoint.getX()) && toPoint.getY().equals(toPoint.getY())) {
                    log.info("skip point2: {}, {}, {}, {}", toPoint.getAction(), toPoint.getX(), toPoint.getY(), toPoint.getTime());
                    continue;
                }

                if (toPoint.getX() > iosDevice.getRealWidth() || toPoint.getY() > iosDevice.getRealHeight()) {
                    log.info("point2 out of bound, skip : {}, {}, {}, {}", toPoint.getAction(), toPoint.getX(), toPoint.getY(), toPoint.getTime());
                    continue;
                }

                log.info("mouse move from point1 {} {} to point2 {} {}", fromPoint.getX(), fromPoint.getY(), toPoint.getX(), toPoint.getY());

                drag(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                        iosDevice.getSessionId(),
                        fromPoint.getX(), fromPoint.getY(),
                        toPoint.getX(), toPoint.getY(),
                        toPoint.getTime() - fromPoint.getTime());
                continue;
            } else { // mouse move
                while ((toPoint.getTime() - fromPoint.getTime() < 0.8) && toPoint.getX() < iosDevice.getRealWidth() && toPoint.getY() < iosDevice.getRealHeight()) {
                    log.info("skip point2: {}, {}, {}, {}", toPoint.getX(), toPoint.getY(), toPoint.getTime(), toPoint.getTime());
                    fromPoint = toPoint;
                    toPoint = touchMsgQueue.take();
                }

                log.info("mouse move from point1 {} {} to point2 {} {}", fromPoint.getX(), fromPoint.getY(), toPoint.getX(), toPoint.getY());

                drag(iosDevice.getWebDriverAgent().getUrl().toURI().toString(),
                        iosDevice.getSessionId(),
                        startPoint.getX(), startPoint.getY(),
                        toPoint.getX(), toPoint.getY(),
                        toPoint.getTime() - fromPoint.getTime());
                continue;
            }
        }
    }
}
