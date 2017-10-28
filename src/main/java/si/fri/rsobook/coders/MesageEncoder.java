package si.fri.rsobook.coders;


import si.fri.rsobook.Config;
import si.fri.rsobook.model.Message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

public class MesageEncoder implements Encoder.Text<Message> {

    @Override
    public String encode(Message object) throws EncodeException {
        try {
            String message = Config.MAPPER.writeValueAsString(object);
            return message;
        } catch (IOException e) {
            e.printStackTrace();
            throw new EncodeException(object, "Exception during deserilization", e);
        }
    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }
}
