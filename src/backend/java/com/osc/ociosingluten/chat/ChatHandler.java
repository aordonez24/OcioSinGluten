package chat;//Se encarga de administrar las conexiones con el webSocket
//Que va a suceder cuando alguien se conecte al websocket

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<WebSocketSession>(); //Tipo de lista eficiente para entornos con concurrencia

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session); //Añadir la sesion cuando se conecta
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session); //Eliminarla
    }

    @Override //Una vez llegue el mensaje desde el navegador, se envia el mensaje a todas las secciones activas, es decir a todas las sesiones dentrod el sessions
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        for(WebSocketSession webSocketSession : sessions){
            webSocketSession.sendMessage(message);
        }
    }
}
