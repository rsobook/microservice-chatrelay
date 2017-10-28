package si.fri.rsobook.coders;


import si.fri.rsobook.Config;
import si.fri.rsobook.model.Message;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

public class MesageDecoder implements Decoder.Text<Message> {

    @Override
    public Message decode(String s) throws DecodeException {
        if(Config.DEBUG){
            System.out.println(s);
        }
        try {
            Message cm = Config.MAPPER.readValue(s, Message.class);
            return cm;

        } catch (IOException e) {
            e.printStackTrace();
            throw new DecodeException(s, "Exception during serilization", e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }

}
