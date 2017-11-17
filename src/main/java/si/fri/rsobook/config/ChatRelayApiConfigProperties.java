package si.fri.rsobook.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("properties")
public class ChatRelayApiConfigProperties {

    @ConfigValue(watch = true)
    private Integer maxChatUsers;

    public Integer getMaxChatUsers() {
        return maxChatUsers;
    }

    public void setMaxChatUsers(Integer maxChatUsers) {
        this.maxChatUsers = maxChatUsers;
    }
}
