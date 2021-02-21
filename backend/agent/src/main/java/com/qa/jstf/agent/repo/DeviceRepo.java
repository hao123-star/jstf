package com.qa.jstf.agent.repo;

import com.qa.jstf.agent.entity.android.AndroidDevice;
import com.qa.jstf.agent.entity.ios.IosDevice;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Component
public class DeviceRepo {
     Map<String, IosDevice> iosDeviceMap = new ConcurrentHashMap<>();
     Map<String, IosDevice> toReleaseIosDeviceMap = new ConcurrentHashMap<>();

     Map<String, AndroidDevice> androidDeviceMap = new ConcurrentHashMap<>();
     Map<String, AndroidDevice> toReleaseAndroidDeviceMap = new ConcurrentHashMap<>();
}
