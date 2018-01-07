package si.fri.rsobook.websocket;

import com.kumuluz.ee.discovery.enums.AccessType;
import com.kumuluz.ee.discovery.utils.DiscoveryUtil;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import si.fri.rsobook.coders.MesageDecoder;
import si.fri.rsobook.coders.MesageEncoder;
import si.fri.rsobook.config.ChatRelayApiConfigProperties;
import si.fri.rsobook.model.Message;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@ServerEndpoint(value = "/api/v1/ChatRelay/{room}", decoders = MesageDecoder.class, encoders = MesageEncoder.class)
public class ChatRoomRelayEndpoint {

    private final Logger log = LogManager.getLogger(ChatRoomRelayEndpoint.class.getName());

    private Optional<URL> chatroomUrl = Optional.empty();

    @Inject
    private DiscoveryUtil discoveryUtil;

    @Inject
    @Metric(name = "users_logedin")
    private Counter usersLoggedIn;

    private Client httpClient;

    @Inject
    private ChatRelayApiConfigProperties chatRelayApiConfigProperties;

    @OnOpen
    public void open(final Session session, @PathParam("room") final String room) {
        if (usersLoggedIn.getCount() < chatRelayApiConfigProperties.getMaxChatUsers()) {
            log.info(String.format("User with ip %s logged int room: %s ", session.getUserProperties().get("javax.websocket.endpoint.remoteAddress"), room));

            if (!chatroomUrl.isPresent()) {
                chatroomUrl = discoverChatrooms();
            }
            if (httpClient == null) {
                httpClient = ClientBuilder.newClient();
            }

            // send info to ms-chatroom
            log.info("chatroomUrl:" + chatroomUrl);
            Response resp = null;
            if (chatroomUrl.isPresent()) {
                URL url = chatroomUrl.get();
                try {
                    resp = httpClient.target(String.format("%s/api/v1/ChatRoom/%s/joined", url.toString(), room))
                            .request().post(null);

                    if (resp.getStatus() < 400) {
                        session.getUserProperties().put("room", room);
                        try {
                            session.getBasicRemote().sendObject(new Message("Server", "You logged in chat room: " + room));
                            usersLoggedIn.inc();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                } catch (Exception e) {
                    log.error(e);
                }
                log.info(String.valueOf(resp));
            }
        } else {
            log.info("Too many concurrent users: " + usersLoggedIn.getCount());
        }

        try {
            session.getBasicRemote().sendObject(new Message("Server", "Unable to connect to chatroom"));
            session.close();
        } catch (EncodeException | IOException e) {
            e.printStackTrace();
        }

    }

    @OnClose
    public void close(final Session session) {
        usersLoggedIn.dec();
        String room = (String) session.getUserProperties().get("room");

        if (!chatroomUrl.isPresent()) {
            chatroomUrl = discoverChatrooms();
        }
        if (httpClient == null) {
            httpClient = ClientBuilder.newClient();
        }

        // send info to ms-chatroom
        log.info("chatroomUrl:" + chatroomUrl);
        chatroomUrl.ifPresent(url -> {
            Response resp = null;
            try {
                resp = httpClient.target(String.format("%s/api/v1/ChatRoom/%s/disconnected", url.toString(), room))
                        .request().post(null);
            } catch (Exception e) {
                log.error(e);
            }
            log.info(String.valueOf(resp));
        });


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

    private Optional<URL> discoverChatrooms() {
        if (discoveryUtil != null) {
            return discoveryUtil.getServiceInstance("ms-chatroom", "2.0.*", "dev", AccessType.DIRECT);
        }
        log.error("ms-chatroom could not be discovered");
        return Optional.empty();
    }
}
