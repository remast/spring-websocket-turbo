package remast.websocket.turbo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // 1. Create identifier
        var identifierJson = objectMapper.createObjectNode();
        identifierJson.put("channel", "Turbo::StreamsChannel");
        identifierJson.put("signed_stream_name", "**mysignature**");

        // 2. Create Turbo Stream Action
        String turboAction = "<turbo-stream action='append' target='board'>" +
                "<template>" +
                "<p>$$MESSAGE$$</p>" +
                "</template>" +
                "</turbo-stream>";
        turboAction = turboAction.replace("$$MESSAGE$$", message.getPayload());

        // 3. Create WebSocket Message as JSON
        var turboMessageJson = objectMapper.createObjectNode();
        turboMessageJson.put("identifier", objectMapper.writeValueAsString(identifierJson));
        turboMessageJson.put("message", turboAction);

        // 4. Broadcast WebSocket Message to all connected sessions
        var turboStreamPayload = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(turboMessageJson);
        broadcast(new TextMessage(turboStreamPayload));
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

    private void broadcast(TextMessage message) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            webSocketSession.sendMessage(message);
        }
    }
}