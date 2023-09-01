package com.example.InPushServerDemo.controller;

import com.example.InPushServerDemo.model.Constantes;
import com.example.InPushServerDemo.model.Device;
import com.example.InPushServerDemo.model.Topic;
import com.example.InPushServerDemo.service.DeviceService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/topics")
public class TopicsController {

    private final DeviceService deviceService;

    @Autowired
    public TopicsController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/get-topics")
    public List<String> getTopics(){
        return deviceService.getTopics();
    }

    @PostMapping("/get-topics-with-tokens")
    public Set<Map.Entry<String, Set<String>>> getTopicsWithTokens(){
        return deviceService.getTopicsWithTokens();
    }

    @PostMapping("/get-topic-with-tokens")
    public Map<String, Set<String>> getTopicWithTokens(@RequestBody Topic topic){
        return deviceService.getTopicWithTokens(topic.getName());
    }

    @PostMapping("/register-topic")
    public Map<String, Object> registerTopic(@RequestBody Topic topic) {
        Map<String, Object> result = new HashMap<>();

        String topicName = topic.getName();
        if (Strings.isNotBlank(topicName)) {
            result = deviceService.registerTopic(topic.getName());
        } else {
            result.put(Constantes.RESPONSE, Constantes.ERROR);
            result.put(Constantes.DETAILS, "Falta el nombre del tópico.");
        }

        return result;
    }
}
