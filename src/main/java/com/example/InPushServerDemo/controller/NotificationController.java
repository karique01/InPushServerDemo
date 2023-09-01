package com.example.InPushServerDemo.controller;

import com.example.InPushServerDemo.model.Constantes;
import com.example.InPushServerDemo.model.TokenMessage;
import com.example.InPushServerDemo.model.TopicMessage;
import com.example.InPushServerDemo.service.DeviceService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final DeviceService deviceService;

    @Autowired
    public NotificationController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/send-notification-to-topic")
    public Map<String, Object> sendNotificationToTopic(@RequestBody TopicMessage topicMessage) {
        Map<String, Object> result = new HashMap<>();

        if (Strings.isNotBlank(topicMessage.topic)) {
            result = deviceService.sendNotificationToTopic(topicMessage.topic, topicMessage.data);
        } else {
            result.put(Constantes.RESPONSE, Constantes.ERROR);
            result.put(Constantes.DETAILS, "Falta el topico.");
        }

        return result;
    }

    @PostMapping("/send-notification-to-device")
    public Map<String, Object> sendNotificationToDevice(@RequestBody TokenMessage tokenMessage) {
        Map<String, Object> result = new HashMap<>();

        if (Strings.isNotBlank(tokenMessage.token)) {
            result = deviceService.sendNotificationToDevice(tokenMessage.token, tokenMessage.data);
        } else {
            result.put(Constantes.RESPONSE, Constantes.ERROR);
            result.put(Constantes.DETAILS, "Falta el token del dispositivo.");
        }

        return result;
    }
}
