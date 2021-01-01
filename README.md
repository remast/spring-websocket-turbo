# Spring Boot Example for TurboStreams over WebSockets

Simple example for using [Turbo](https://turbo.hotwire.dev/)s [Stream](https://turbo.hotwire.dev/reference/streams)s in Go with the [Gorilla WebSocket](https://github.com/gorilla/websocket) toolkit.

Run the sample using the following command:

    $ ./mvnw spring-boot:run

To use the chat example, open http://localhost:8080/ in your browser.

## Frontend
The frontend connects to the Turbo Stream using plain JavaScript like:

```html
<script src="https://unpkg.com/@hotwired/turbo@7.0.0-beta.1/dist/turbo.es5-umd.js" ></script>
<script>
Turbo.connectStreamSource(new WebSocket("ws://" + document.location.host + "/chat"));
</script>
```

After that the frontend is connected to the Turbo Stream and get's all messages. Every chat message is appended to the dom element with id `board`.

This _should_ work with html markup too but I have not gotten it working yet.

## Server

The server receives the new chat message via plain web socket. Then it wraps the message as Turbo Stream action with action `append` and broadcasts it to all subscribers. That way all subscribed users see the new message on the board.

The [SocketHandler](src/main/java/remast/websocket/turbo/SocketHandler.java) takes the incoming chat message, wraps it in a Turbo Stream Action
and broadcasts it to all connected clients:
```java
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // 1. Create identifier
        var identifierJson = objectMapper.createObjectNode();
        identifierJson.put("channel", "Turbo::StreamsChannel");
        identifierJson.put("signed_stream_name", "**mysignature**");

        // 2. Create Turbo Stream Action
        var turboAction = "<turbo-stream action='append' target='board'>" +
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
```

The raw text message sent over the web socket is:
```json
{ 
  "identifier": 
     "{\"channel\":\"Turbo::StreamsChannel\",  \"signed_stream_name\":\"**mysignature**\"}",
  "message":
    "<turbo-stream action='append' target='board'>
        <template>
            <p>My new Message</p>
        </template>
    </turbo-stream>"
}
```

Turbo Streams use raw JSON messages over WebSockets without STOMP.

## Credits
A lot of insights by https://github.com/mbucc/hotwire-demo-chat-in-springboot.