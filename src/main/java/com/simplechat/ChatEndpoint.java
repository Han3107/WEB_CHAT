package com.simplechat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * WebSocket Endpoint cho Chat Server
 * URL: ws://localhost:8080/SimpleChatWebSocket/chat
 */
@ServerEndpoint("/chat")
public class ChatEndpoint {
    
    // Lưu trữ tất cả client kết nối
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static Map<Session, String> userNames = Collections.synchronizedMap(new HashMap<>());
    
    private static final Gson gson = new Gson();
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("\n" + getTime() + " [ChatEndpoint.onOpen] ✓ New connection from: " + session.getId());
        System.out.println("Total clients connected: " + (sessions.size() + 1));
        sessions.add(session);
        updateUserList();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            if ("login".equals(type)) {
                String username = json.get("username").getAsString().trim();
                if (username.isEmpty()) {
                    username = "Guest";
                }
                userNames.put(session, username);
                
                System.out.println("[" + getTime() + "] " + username + " joined");
                
                // Broadcast thông báo
                broadcastSystemMessage(username + " đã tham gia chat");
                updateUserList();
                
            } else if ("message".equals(type)) {
                String username = userNames.getOrDefault(session, "Guest");
                String content = json.get("content").getAsString();
                
                System.out.println("[" + getTime() + "] " + username + ": " + content);
                
                // Broadcast tin nhắn
                broadcastUserMessage(username, content);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        String username = userNames.remove(session);
        if (username == null) {
            username = "Unknown";
        }
        
        sessions.remove(session);
        System.out.println("[" + getTime() + "] " + username + " left. Online: " + sessions.size());
        
        // Broadcast thông báo
        broadcastSystemMessage(username + " đã rời chat");
        updateUserList();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    // Gửi tin nhắn từ user
    private void broadcastUserMessage(String username, String content) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "message");
        json.addProperty("username", username);
        json.addProperty("content", content);
        json.addProperty("time", getTime());
        
        String message = json.toString();
        
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                }
            }
        }
    }

    // Gửi tin nhắn system
    private void broadcastSystemMessage(String content) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "system");
        json.addProperty("content", content);
        json.addProperty("time", getTime());
        
        String message = json.toString();
        
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Error sending system message: " + e.getMessage());
                }
            }
        }
    }

    // Cập nhật danh sách người online
    private void updateUserList() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "userlist");
        
        List<String> users = new ArrayList<>(userNames.values());
        json.addProperty("users", String.join(", ", users));
        json.addProperty("count", users.size());
        
        String message = json.toString();
        
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Error sending user list: " + e.getMessage());
                }
            }
        }
    }

    private static String getTime() {
        return LocalDateTime.now().format(timeFormat);
    }
}
