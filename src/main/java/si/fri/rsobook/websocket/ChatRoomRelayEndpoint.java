package si.fri.rsobook.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import si.fri.rsobook.coders.MesageDecoder;
import si.fri.rsobook.coders.MesageEncoder;
import si.fri.rsobook.config.ChatRelayApiConfigProperties;
import si.fri.rsobook.model.Message;
import si.fri.rsobook.model.RoomStats;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;


@ServerEndpoint(value = "/api/v1/ChatRelay/{room}", decoders = MesageDecoder.class, encoders = MesageEncoder.class)
public class ChatRoomRelayEndpoint {

    private final Logger log = LogManager.getLogger(ChatRoomRelayEndpoint.class.getName());

    @Inject
    @Metric(name = "users_logedin")
    private Counter usersLoggedIn;

    @Inject
    private ChatRelayApiConfigProperties chatRelayApiConfigProperties;

    private final HashMap<String, RoomStats> roomUserHashSet = new HashMap<>();

    @OnOpen
    public void open(final Session session, @PathParam("room") final String room) {
        log.info(String.format("User with ip %s logged int room: %s ", session.getUserProperties().get("javax.websocket.endpoint.remoteAddress"), room ) );

        usersLoggedIn.inc();

        RoomStats roomStats = roomUserHashSet.get(room);
        if(roomStats == null){
            roomStats = new RoomStats(room);
            roomUserHashSet.put(room, roomStats);
        }


        if(chatRelayApiConfigProperties.getMaxChatUsers() >= roomStats.userCount) {
            roomStats.userCount++;

            session.getUserProperties().put("room", room);
            try {
                session.getBasicRemote().sendObject(new Message("Server", "You logged in chat room: " + room));
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }
        } else {
            try {
                session.getBasicRemote().sendObject(new Message("Server", "The room is full."));
                session.close();
            } catch (EncodeException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void close(final Session session) {
        String room = (String) session.getUserProperties().get("room");

        RoomStats roomStats = roomUserHashSet.get(room);
        if(roomStats != null){
            roomStats.userCount--;
            if(roomStats.userCount == 0){
                roomUserHashSet.remove(roomStats);
            }
        }

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
            log.error("onMessage failed", e);
        }
    }
}
