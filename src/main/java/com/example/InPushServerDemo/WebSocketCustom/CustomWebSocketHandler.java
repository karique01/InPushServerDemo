package com.example.InPushServerDemo.WebSocketCustom;

import com.example.InPushServerDemo.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomWebSocketHandler implements WebSocketHandler {
    String token = "";
    private final DeviceService deviceService;

    @Autowired
    public CustomWebSocketHandler(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Obtener la cadena de la URL de la sesión WebSocket
        String url = session.getUri().toString();

        // Extraer el valor del parámetro de consulta "token" de la cadena de URL
        String token = UriComponentsBuilder.fromUriString(url).build().getQueryParams().getFirst("token");

        System.out.println("Nuevo cliente conectado: " + token);
        deviceService.setWebSocketSessionToToken(token, session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("Mensaje recibido desde el cliente");

        // Tu lógica para manejar el mensaje recibido del cliente
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            System.out.println("Mensaje: " + payload);
        }
        else {
            System.out.println("Mensaje no es de instancia TextMessage");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // Obtener la cadena de la URL de la sesión WebSocket
        String url = session.getUri().toString();

        // Extraer el valor del parámetro de consulta "token" de la cadena de URL
        String token = UriComponentsBuilder.fromUriString(url).build().getQueryParams().getFirst("token");

        System.out.println("Cliente desconectado: " + token);
        deviceService.setWebSocketSessionToClosed(token);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // Manejo de errores de transporte, si es necesario
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
