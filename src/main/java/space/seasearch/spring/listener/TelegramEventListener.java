package space.seasearch.spring.listener;

import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.service.UserService;

@Component
@RequiredArgsConstructor
public class TelegramEventListener {

    private final UserService userService;

    @EventListener
    @Transactional
    public void processNewMessageEvent(NewMessageReceivedEvent event) {
        TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) event.getSource();
        var message = updateNewMessage.message;
        String phoneNumber = event.getSeaSearchUser();
        var stats = userService.getInfoStats(phoneNumber, message.chatId);

        stats.updateOldestMessagedate(message);
        stats.messageDailyStatUpdate(message);
        stats.countWrodsAndSymbols(message);

        userService.updateStats(stats, phoneNumber, message.chatId);
    }
}
