package herramientas;

import chat.ChatHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import static chat.ChatHandler.*;

@Configuration
@EnableWebSocket
@ComponentScan(basePackages = {"chat"})
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatHandler chatHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/chat");
    }
    //Definir servidor socket
}
