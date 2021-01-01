package com.example.messagingstompwebsocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {

        for (WebSocketSession webSocketSession : sessions) {
            String payload = message.getPayload();

            String turboMessage = "{ \n" +
                    "\t\t\t\t\"identifier\": \n" +
                    "\t\t\t\t   \"{\\\"channel\\\":\\\"Turbo::StreamsChannel\\\",\\\"signed_stream_name\\\":\\\"**mysignature**\\\"}\",\n" +
                    "\t\t\t\t\"message\":\n" +
                    "\t\t\t\t  \"<turbo-stream action=\"append\" target=\"board\">\n" +
                    "\t\t\t\t\t<template>\n" +
                    "\t\t\t\t\t\t<p>$$MESSAGE$$</p>\n" +
                    "\t\t\t\t\t</template>\n" +
                    "\t\t\t\t   </turbo-stream>\"\n" +
                    "\t\t\t  }";

            webSocketSession.sendMessage(new TextMessage(turboMessage.replace("$$MESSAGE$$", payload)));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //the messages will be broadcasted to all users.
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}