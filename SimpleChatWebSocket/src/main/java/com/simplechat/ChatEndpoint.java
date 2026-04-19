package com.simplechat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private static final Map<Session, String> userNames = new ConcurrentHashMap<>();
    
    private static final Gson gson = new Gson();
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("\n" + getTime() + " [ChatEndpoint.onOpen] ✓ New connection from: " + session.getId());
        sessions.add(session);
        System.out.println("Total clients connected: " + sessions.size());
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
                String channelId = json.has("channelId") ? json.get("channelId").getAsString() : "global";
                String msgType = json.has("msgType") ? json.get("msgType").getAsString() : "text";
                
                System.out.println("[" + getTime() + "] " + username + " [ch:" + channelId + "]: " + content);
                broadcastChannelMessage(username, content, channelId, msgType);
            } else if ("dm".equals(type)) {
                String sender = userNames.getOrDefault(session, "Guest");
                String content = json.get("content").getAsString();
                String recipient = json.get("recipient").getAsString();
                String msgType = json.has("msgType") ? json.get("msgType").getAsString() : "text";

                JsonObject out = new JsonObject();
                out.addProperty("type", "dm");
                out.addProperty("username", sender);
                out.addProperty("content", content);
                out.addProperty("msgType", msgType);
                out.addProperty("time", getTime());
                
                sendToUser(recipient, out.toString());
            } else if ("notification".equals(type)) {
                String recipient = json.get("recipient").getAsString();
                sendToUser(recipient, message);
            } else if ("online".equals(type)) {
                updateUserList();
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
        if (session != null) {
            onClose(session);
        }
    }

    // Gửi tin nhắn từ user theo channel
    private void broadcastChannelMessage(String username, String content, String channelId, String msgType) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "message");
        json.addProperty("username", username);
        json.addProperty("content", content);
        json.addProperty("channelId", channelId);
        json.addProperty("msgType", msgType);
        json.addProperty("time", getTime());
        String message = json.toString();
        for (Session session : snapshotSessions()) {
            sendIfOpen(session, message, "channel message");
        }
    }

    // Gửi tin nhắn từ user
    private void broadcastUserMessage(String username, String content) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "message");
        json.addProperty("username", username);
        json.addProperty("content", content);
        json.addProperty("time", getTime());
        
        String message = json.toString();
        
        for (Session session : snapshotSessions()) {
            sendIfOpen(session, message, "message");
        }
    }

    // Gửi tin nhắn system
    private void broadcastSystemMessage(String content) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "system");
        json.addProperty("content", content);
        json.addProperty("time", getTime());
        
        String message = json.toString();
        
        for (Session session : snapshotSessions()) {
            sendIfOpen(session, message, "system message");
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
        
        for (Session session : snapshotSessions()) {
            sendIfOpen(session, message, "user list");
        }
    }

    private void sendToUser(String targetUsername, String message) {
        for (Map.Entry<Session, String> entry : userNames.entrySet()) {
            if (entry.getValue().equals(targetUsername)) {
                sendIfOpen(entry.getKey(), message, "private message");
            }
        }
    }

    private static List<Session> snapshotSessions() {
        return new ArrayList<>(sessions);
    }

    private void sendIfOpen(Session session, String message, String kind) {
        if (!session.isOpen()) {
            sessions.remove(session);
            userNames.remove(session);
            return;
        }

        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            sessions.remove(session);
            userNames.remove(session);
            System.err.println("Error sending " + kind + ": " + e.getMessage());
        }
    }

    private static String getTime() {
        return LocalDateTime.now().format(timeFormat);
    }
}
