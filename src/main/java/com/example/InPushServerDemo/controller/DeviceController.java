package com.example.InPushServerDemo.controller;

import com.example.InPushServerDemo.model.Constantes;
import com.example.InPushServerDemo.model.Device;
import com.example.InPushServerDemo.model.DeviceIdentifier;
import com.example.InPushServerDemo.model.Topic;
import com.example.InPushServerDemo.service.DeviceService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/get-all-device-tokens-with-topics")
    public Set<Map.Entry<String, Set<String>>> getAllDeviceTokensWithTopics() {
        return deviceService.getAllDeviceTokensWithTopics();
    }

    @PostMapping("/get-device-topics-by-token")
    public Map<String, Set<String>> getDeviceTopicsByToken(@RequestBody DeviceIdentifier deviceIdentifier) {
        return deviceService.getDeviceTopicsByToken(deviceIdentifier);
    }

    @PostMapping("/register-device")
    public Map<String, Object> registerDevice(@RequestBody Device device) {
        Map<String, Object> result = new HashMap<>();

        String token = device.getToken();
        if (Strings.isNotBlank(token)) {
            result = deviceService.registerDevice(token, new HashSet<>(device.getTopics()));
        } else {
            result.put(Constantes.RESPONSE, Constantes.ERROR);
            result.put(Constantes.DETAILS, "Falta el token de registro.");
        }
        return result;
    }

    @PostMapping("/register-device-to-topics")
    public Map<String, Object> registerDeviceToTopics(@RequestBody Device device) {
        Map<String, Object> result = new HashMap<>();

        String token = device.getToken();
        Set<Topic> topics = new HashSet<>(device.getTopics());

        if (Strings.isNotBlank(token) && !topics.isEmpty()) {
            result = deviceService.addTopicsToDevice(token, topics);
        } else {
            result.put(Constantes.RESPONSE, Constantes.ERROR);
            result.put(Constantes.DETAILS, "Falta el token de registro o los tópicos a registrar.");
        }
        return result;
    }

    @PostMapping("/delete-device-from-topics")
    public Map<String, Object> deleteDeviceFromTopics(@RequestBody Device device) {
        Map<String, Object> result = new HashMap<>();

        String token = device.getToken();
        Set<Topic> topics = new HashSet<>(device.getTopics());

        if (Strings.isNotBlank(token) && !topics.isEmpty()) {
            result = deviceService.removeTopicsFromDevice(token, topics);
        } else {
            result.put(Constantes.RESPONSE, Constantes.ERROR);
            result.put(Constantes.DETAILS, "Falta el token de registro o los tópicos a eliminar.");
        }
        return result;
    }
}
