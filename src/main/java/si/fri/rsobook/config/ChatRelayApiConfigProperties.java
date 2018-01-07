package si.fri.rsobook.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;
import com.kumuluz.ee.discovery.annotations.DiscoverService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;

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
