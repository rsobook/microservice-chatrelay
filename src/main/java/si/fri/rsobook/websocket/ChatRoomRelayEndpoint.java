package si.fri.rsobook.websocket;

import si.fri.rsobook.coders.MesageDecoder;
import si.fri.rsobook.coders.MesageEncoder;
import si.fri.rsobook.model.Message;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/room/{room}", decoders = MesageDecoder.class, encoders = MesageEncoder.class)
public class ChatRoomRelayEndpoint {

    private final Logger log = Logger.getLogger(getClass().getName());

    @OnOpen
    public void open(final Session session, @PathParam("room") final String room) {
        log.info("session openend and bound to room: " + room);
        session.getUserProperties().put("room", room);

        try {
            session.getBasicRemote().sendObject(new Message("Server", "You logged in chat room: " + room));
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void close(final Session session) {
        log.info("session closed");
    }

    @OnMessage
    public void onMessage(final Session session, final Message chatMessage) {
        String room = (String) session.getUserProperties().get("room");
        try {
            for (Session s : session.getOpenSessions()) {
                if (s.isOpen() && room.equals(s.getUserProperties().get("room"))) {
                    s.getBasicRemote().sendObject(chatMessage);
                }
            }
        } catch (IOException | EncodeException e) {
            log.log(Level.WARNING, "onMessage failed", e);
        }
    }
}
