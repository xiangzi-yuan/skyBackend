package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务端
 * 用于实现商家端消息推送（新订单提醒、催单提醒）
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    /**
     * 存放所有连接的Session，key为客户端标识sid
     */
    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     *
     * @param session 会话对象
     * @param sid     客户端标识
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("WebSocket连接建立，sid={}", sid);
        SESSION_MAP.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送的消息
     * @param sid     客户端标识
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到来自客户端{}的消息: {}", sid, message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid 客户端标识
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        log.info("WebSocket连接关闭，sid={}", sid);
        SESSION_MAP.remove(sid);
    }

    /**
     * 发生错误时调用的方法
     *
     * @param session 会话对象
     * @param error   错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误: {}", error.getMessage());
    }

    /**
     * 向所有客户端发送消息
     *
     * @param message 要发送的消息
     */
    public void sendToAllClients(String message) {
        Collection<Session> sessions = SESSION_MAP.values();
        for (Session session : sessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                log.error("向客户端发送消息失败: {}", e.getMessage());
            }
        }
    }
}
