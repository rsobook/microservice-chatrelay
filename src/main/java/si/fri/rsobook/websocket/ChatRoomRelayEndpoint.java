package si.fri.rsobook.websocket;

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
import java.util.logging.Level;
import java.util.logging.Logger;


@ServerEndpoint(value = "/ws/v1/ChatRelay/{room}", decoders = MesageDecoder.class, encoders = MesageEncoder.class)
public class ChatRoomRelayEndpoint {

    @Inject
    private ChatRelayApiConfigProperties chatRelayApiConfigProperties;

    private final Logger log = Logger.getLogger(getClass().getName());

    private final HashMap<String, RoomStats> roomUserHashSet = new HashMap<>();

    @OnOpen
    public void open(final Session session, @PathParam("room") final String room) {
        log.info("session openend and bound to room: " + room);

        RoomStats roomStats = roomUserHashSet.get(room);
        if(roomStats == null){
            roomStats = new RoomStats(room);
            roomUserHashSet.put(room, roomStats);
        }

        if(roomStats.userCount >= chatRelayApiConfigProperties.getMaxChatUsers()){
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
            } catch (EncodeException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
            log.log(Level.WARNING, "onMessage failed", e);
        }
    }
}
