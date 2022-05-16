package space.seasearch.spring.listener;

import it.tdlight.jni.TdApi;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import space.seasearch.spring.entity.SeaSearchUser;

public class NewMessageReceivedEvent extends ApplicationEvent {

    @Getter
    private String seaSearchUser;

    public NewMessageReceivedEvent(Object source, String userPhone) {
        super(source);
        seaSearchUser = userPhone;
    }
}
