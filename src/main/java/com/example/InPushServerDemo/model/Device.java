package com.example.InPushServerDemo.model;

import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Device {
    private String token;
    private List<Topic> topics;
    private String estado;
    public WebSocketSession webSocketSession;

    public void removeTopics(List<Topic> topicsRem) {
        for (int i = 0; i < topicsRem.size(); i++) {
            removeTopic(topicsRem.get(i));
        }
    }

    public void removeTopic(Topic topicRem) {
        for (int i = 0; i < topics.size(); ) {
            if (topics.get(i).getName().equals(topicRem.getName())){
                topics.remove(i);
            }
            else i++;
        }
    }

    public void addTopics(List<Topic> topics) {
        this.topics.addAll(topics);
    }

    public void clearTopics() {
        this.topics.clear();
    }

    public Device() {
        topics = new LinkedList<>();
        webSocketSession = new WebSocketSession() {
            @Override
            public String getId() {
                return null;
            }

            @Override
            public URI getUri() {
                return null;
            }

            @Override
            public HttpHeaders getHandshakeHeaders() {
                return null;
            }

            @Override
            public Map<String, Object> getAttributes() {
                return null;
            }

            @Override
            public Principal getPrincipal() {
                return null;
            }

            @Override
            public InetSocketAddress getLocalAddress() {
                return null;
            }

            @Override
            public InetSocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public String getAcceptedProtocol() {
                return null;
            }

            @Override
            public void setTextMessageSizeLimit(int messageSizeLimit) {

            }

            @Override
            public int getTextMessageSizeLimit() {
                return 0;
            }

            @Override
            public void setBinaryMessageSizeLimit(int messageSizeLimit) {

            }

            @Override
            public int getBinaryMessageSizeLimit() {
                return 0;
            }

            @Override
            public List<WebSocketExtension> getExtensions() {
                return null;
            }

            @Override
            public void sendMessage(WebSocketMessage<?> message) throws IOException {

            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public void close() throws IOException {

            }

            @Override
            public void close(CloseStatus status) throws IOException {

            }
        };
    }

    public Device(String token, List<Topic> topics, String estado) {
        this.token = token;
        this.topics = topics;
        this.estado = estado;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
