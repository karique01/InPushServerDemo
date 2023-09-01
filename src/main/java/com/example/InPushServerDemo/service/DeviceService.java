package com.example.InPushServerDemo.service;

import com.example.InPushServerDemo.model.Constantes;
import com.example.InPushServerDemo.model.Device;
import com.example.InPushServerDemo.model.DeviceIdentifier;
import com.example.InPushServerDemo.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    //reemplazar con base de datos
    private static final Map<String, Device> registeredDevices = new HashMap<>();
    private static final Map<String, Set<String>> topicSubscriptions = new HashMap<>();

    public void setWebSocketSessionToToken(String token, WebSocketSession session) {
        registeredDevices.get(token).webSocketSession = session;
        registeredDevices.get(token).setEstado("conectado");
        sendUpdatedTokenData();
    }

    public void setWebSocketSessionToClosed(String token) {
        registeredDevices.get(token).setEstado("desconectado");
        sendUpdatedTokenData();
    }

    public Set<Map.Entry<String, Set<String>>> getAllDeviceTokensWithTopics() {
        Map<String, Set<String>> result = new HashMap<>();
        for (String token : registeredDevices.keySet()) {
            Device device = registeredDevices.get(token);
            Set<String> topics = new HashSet<>();
            for (Topic topic : device.getTopics()) {
                topics.add(topic.getName());
            }
            result.put(token, topics);
        }
        return result.entrySet();
    }

    public Map<String, Set<String>> getDeviceTopicsByToken(DeviceIdentifier deviceIdentifier) {
        Map<String, Set<String>> result = new HashMap<>();
        Device device = registeredDevices.get(deviceIdentifier.getToken());
        if (device != null) {
            Set<String> topics = new HashSet<>();
            for (Topic topic : device.getTopics()) {
                topics.add(topic.getName());
            }
            result.put(deviceIdentifier.getToken(), topics);
        }
        return result;
    }

    public Map<String, Object> registerDevice(String token, Set<Topic> topics) {
        Map<String, Object> result = new HashMap<>();
        String details = "";

        if (!registeredDevices.containsKey(token)) {
            Device device = new Device();
            device.setToken(token);
            device.addTopics(topics.stream().toList());
            registeredDevices.put(token, device);

            for (Topic topic : topics) {
                String topicName = topic.getName();
                topicSubscriptions.computeIfAbsent(topicName, k -> new HashSet<>()).add(token);
            }

            details = "Dispositivo exitosamente registrado con el token: " + token;
            System.out.println(details);
            result.put(Constantes.RESPONSE, Constantes.OK);
            result.put(Constantes.DETAILS, details);
            return result;
        }

        details = "El dispositivo: " + token + " ya estaba registrado previamente";
        System.out.println(details);
        result.put(Constantes.RESPONSE, Constantes.OK);
        result.put(Constantes.DETAILS, details);
        return result;
    }

    public Map<String, Object> addTopicsToDevice(String token, Set<Topic> topics) {
        Map<String, Object> result = new HashMap<>();
        String details = "";

        if (registeredDevices.containsKey(token)) {
            Device device = registeredDevices.get(token);
            Set<Topic> currentTopics = new HashSet<>(device.getTopics());
            currentTopics.addAll(topics);
            device.clearTopics();
            device.addTopics(currentTopics.stream().toList());

            for (Topic topic : topics) {
                String topicName = topic.getName();
                topicSubscriptions.computeIfAbsent(topicName, k -> new HashSet<>()).add(token);
            }

            sendUpdatedTokenData();
            details = "Tópicos agregados al dispositivo con token: " + token;
            System.out.println(details);
            result.put(Constantes.RESPONSE, Constantes.OK);
            result.put(Constantes.DETAILS, details);
            result.put("device", device);
            return result;
        }

        details = "El dispositivo: " + token + " no existe";
        System.out.println(details);
        result.put(Constantes.RESPONSE, Constantes.ERROR);
        result.put(Constantes.DETAILS, details);
        return result;
    }

    public Map<String, Object> removeTopicsFromDevice(String token, Set<Topic> topics) {
        Map<String, Object> result = new HashMap<>();
        String details = "";

        if (registeredDevices.containsKey(token)) {
            Device device = registeredDevices.get(token);
            device.removeTopics(topics.stream().toList());

            for (Topic topic : topics) {
                String topicName = topic.getName();
                Set<String> deviceTokens = topicSubscriptions.get(topicName);
                if (deviceTokens != null) {
                    deviceTokens.remove(token);
                }
            }
            sendUpdatedTokenData();

            details = "Tópicos eliminados del dispositivo con token: " + token;
            System.out.println(details);
            result.put(Constantes.RESPONSE, Constantes.OK);
            result.put(Constantes.DETAILS, details);
            result.put("device", device);
            return result;
        }

        details = "El dispositivo: " + token + " no existe";
        System.out.println(details);
        result.put(Constantes.RESPONSE, Constantes.ERROR);
        result.put(Constantes.DETAILS, details);
        return result;
    }

    public Map<String, Object> sendNotificationToTopic(String topicName, Map<String, Object> data) {
        List<String> sents = new LinkedList<>();
        List<String> notSents = new LinkedList<>();
        String sent = "";
        String notSent = "";

        Map<String, Object> result = new HashMap<>();
        String details = "";

        Set<String> deviceTokens = topicSubscriptions.get(topicName);
        if (deviceTokens != null && !deviceTokens.isEmpty()) {
            for (String token : deviceTokens) {
                Device device = registeredDevices.get(token);
                if (device != null) {
                    //enviar notificacion a token del topico
                    WebSocketSession session = device.webSocketSession;
                    if (session != null && session.isOpen()) {
                        try {
                            String messageToSend = convertDataToJson(data); // Convierte el mapa de datos en formato JSON o cualquier otro formato deseado
                            TextMessage message = new TextMessage(messageToSend);
                            session.sendMessage(message);

                            sent = "Notificación enviada a dispositivo con token: " + token;
                            sents.add(sent);
                            System.out.println(sent);
                        } catch (IOException e) {
                            notSent = "Error al enviar la notificación al dispositivo con token: " + token;
                            notSents.add(notSent);
                            System.out.println(notSent);
                        }
                    } else {
                        notSent = "El dispositivo con token: " + token + " no tiene una sesión WebSocket activa.";
                        notSents.add(notSent);
                        System.out.println(notSent);
                    }
                }
            }
            details = "Notificaciones enviadas a topico: " + topicName + "\ndata: " + data;
            System.out.println(details);
            result.put(Constantes.RESPONSE, Constantes.OK);
            result.put(Constantes.DETAILS, details);
            result.put("sent", sents);
            result.put("notSent", notSents);
        }
        else {
            details = "No hay tokens disponibles en el topico ingresado";
            System.out.println(details);
            result.put(Constantes.RESPONSE, Constantes.ERROR);
            result.put(Constantes.DETAILS, details);
        }
        return result;
    }

    public Map<String, Object> sendNotificationToDevice(String token, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        String details = "";

        Device device = registeredDevices.get(token);
        if (device != null) {
            //enviar notificacion a token
            WebSocketSession session = device.webSocketSession;
            if (session != null && session.isOpen()) {
                try {
                    String messageToSend = convertDataToJson(data); // Convierte el mapa de datos en formato JSON o cualquier otro formato deseado
                    TextMessage message = new TextMessage(messageToSend);
                    session.sendMessage(message);

                    details = "Notificación enviada a dispositivo con token: " + token + "\ndata: " + data;
                    System.out.println(details);
                    result.put(Constantes.RESPONSE, Constantes.OK);
                    result.put(Constantes.DETAILS, details);
                    return result;
                } catch (IOException e) {
                    details = "Error al enviar la notificación al dispositivo con token: " + token;
                    System.out.println(details);
                    result.put(Constantes.RESPONSE, Constantes.ERROR);
                    result.put(Constantes.DETAILS, details);
                    return result;
                }
            } else {
                details = "El dispositivo con token: " + token + " no tiene una sesión WebSocket activa.";
                System.out.println(details);
                result.put(Constantes.RESPONSE, Constantes.ERROR);
                result.put(Constantes.DETAILS, details);
                return result;
            }
        }

        details = "No hay dispositivos disponibles para el token ingresado";
        System.out.println(details);
        result.put(Constantes.RESPONSE, Constantes.ERROR);
        result.put(Constantes.DETAILS, details);
        return result;
    }

    public Map<String, Object> registerTopic(String topicName) {
        Map<String, Object> result = new HashMap<>();
        String details = "";

        if (Strings.isNotBlank(topicName)) {
            if (!topicSubscriptions.containsKey(topicName)) {
                topicSubscriptions.put(topicName, new HashSet<>());

                details ="Nuevo tópico registrado: " + topicName;
                System.out.println(details);
                result.put(Constantes.RESPONSE, Constantes.OK);
                result.put(Constantes.DETAILS, details);
                result.put("registeredTopic", topicName);
                return result;
            }

            details = "El topico: " + topicName + " ya existía previamente";
            System.out.println(details);
            result.put(Constantes.RESPONSE, Constantes.ERROR);
            result.put(Constantes.DETAILS, details);
            return result;
        }

        details = "Falta el nombre del tópico.";
        System.out.println(details);
        result.put(Constantes.RESPONSE, Constantes.ERROR);
        result.put(Constantes.DETAILS, details);
        return result;
    }

    public List<String> getTopics() {
       return new ArrayList<>(topicSubscriptions.keySet());
    }

    public Set<Map.Entry<String, Set<String>>> getTopicsWithTokens() {
        return topicSubscriptions.entrySet();
    }

    public Map<String, Set<String>> getTopicWithTokens(String topicName) {
        System.out.println(topicName);

        Map<String, Set<String>> result = new HashMap<>();
        Set<String> tokens = topicSubscriptions.get(topicName);
        if (tokens == null) {
            tokens = new HashSet<>(); // Inicializar como una lista vacía si es null
        }
        result.put(topicName, tokens);
        return result;
    }

    public List<Map<String, Object>> getTokensData() {
        List<Map<String, Object>> tokensData = new ArrayList<>();
        for (String token : registeredDevices.keySet()) {
            Device device = registeredDevices.get(token);
            List<String> topics = device.getTopics().stream().map(Topic::getName).collect(Collectors.toList());
            boolean isConnected = device.webSocketSession != null && device.webSocketSession.isOpen();
            String estado = device.getEstado() + " - ws:" + (isConnected ? " open" : "closed");
            //System.out.println("token: " + token + " - topics: " + topics + " - estado: " + estado);
            tokensData.add(Map.of("token", token, "topics", topics, "estado", estado));
        }
        return tokensData;
    }

    public void sendUpdatedTokenData() {
        List<Map<String, Object>> tokensData = getTokensData();
        String jsonData = convertDataToJson(tokensData);

        Device device = registeredDevices.get("paneldecontrol");

        if (device != null) {
            WebSocketSession controlPanelSession = device.webSocketSession;
            if (controlPanelSession != null && controlPanelSession.isOpen()) {
                try {
                    controlPanelSession.sendMessage(new TextMessage(jsonData));
                } catch (IOException e) {
                    e.printStackTrace();
                    // Manejar el error si es necesario
                }
            }
        }
    }

    // Método para convertir el mapa de datos a formato JSON
    private String convertDataToJson(Map<String, Object> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            // Manejo de error en caso de que haya un problema al convertir el mapa de datos a JSON
            e.printStackTrace();
            return ""; // O devuelve un mensaje de error en lugar de una cadena vacía si es necesario
        }
    }

    private String convertDataToJson(Object data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ""; // O manejar el error de acuerdo a tus necesidades
        }
    }
}
