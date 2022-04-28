package space.seasearch.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import space.seasearch.spring.entity.ChatObjects;
import space.seasearch.spring.entity.ChatStatsRaw;
import space.seasearch.spring.entity.UserInfo;
import space.seasearch.spring.repository.UserRepository;
import space.seasearch.telegram.communication.message.Message;
import space.seasearch.telegram.stats.info.InfoStats;
import space.seasearch.telegram.stats.info.MessagesPerDay;
import space.seasearch.telegram.stats.profile.ProfileStats;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository users;

    public void saveUser(String username, Long chatId, ProfileStats stats) {
        UserInfo user = getUser(username);
        Map<Long, ChatStatsRaw> chatStats = user.getStats();
        ChatStatsRaw chat = new ChatStatsRaw(stats.getInfoStats());
        chat.setNewestMessageDate(stats.getMessage().getNewestMessageDate());
        chatStats.put(chatId, chat);
        users.save(user);
    }

    public int updateStats(ProfileStats stats, String username, Long chatId) {
        UserInfo user = getUser(username);
        if (!user.getStats().containsKey(chatId)) {
            return 0;
        }

        ChatStatsRaw chat = user.getStats().get(chatId);
        fillStats(chat, stats.getInfoStats());
        fillMessages(chat, stats.getMessage());

        return chat.getNewestMessageDate();
    }

    private UserInfo getUser(String username) {
        Optional<UserInfo> userOpt = users.findById(username);
        UserInfo user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new UserInfo();
            user.setUsername(username);
            user.setStats(new HashMap<>());
        }
        user.setLastActivity(LocalDateTime.now());

        return user;
    }

    private void fillStats(ChatStatsRaw from, InfoStats to) {
        ChatObjects incoming = from.getIncoming();
        to.setIncomingAudio(incoming.getAudio());
        to.setIncomingDocument(incoming.getDocument());
        to.setIncomingForward(incoming.getForward());
        to.setIncomingPhoto(incoming.getPhoto());
        to.setIncomingSticker(incoming.getSticker());
        to.setIncomingSymbol(incoming.getSymbol());
        to.setIncomingVideo(incoming.getVideo());
        to.setIncomingWord(incoming.getWord());

        ChatObjects outgoing = from.getOutgoing();
        to.setOutgoingAudio(outgoing.getAudio());
        to.setOutgoingDocument(outgoing.getDocument());
        to.setOutgoingForward(outgoing.getForward());
        to.setOutgoingPhoto(outgoing.getPhoto());
        to.setOutgoingSticker(outgoing.getSticker());
        to.setOutgoingSymbol(outgoing.getSymbol());
        to.setOutgoingVideo(outgoing.getVideo());
        to.setOutgoingWord(outgoing.getWord());

        to.setIncomingMessage(from.getIncomingMessages().stream()
                .reduce(0, (a, b) -> a + b.getValue(), Integer::sum));
        to.setOutgoingMessage(from.getOutgoingMessages().stream()
                .reduce(0, (a, b) -> a + b.getValue(), Integer::sum));

        to.setDictionaryWords(from.getWordCount());
        to.setDateFirstMessage(from.getOldestMessageDate());
    }

    private void fillMessages(ChatStatsRaw from, Message to) {
        to.setMessagesIncomingOfDay(listToMap(from.getIncomingMessages()));
        to.setMessagesOutgoingOfDay(listToMap(from.getOutgoingMessages()));
        var allMessages = listToMap(from.getIncomingMessages());
        for (var message : from.getOutgoingMessages()) {
            allMessages.merge(message.getDate(), message.getValue(), Integer::sum);
        }
        to.setMessagesOfDay(allMessages);
    }

    private Map<LocalDate, Integer> listToMap(List<MessagesPerDay> list) {
        Map<LocalDate, Integer> map = new HashMap<>();
        for (var message : list) {
            map.put(message.getDate(), message.getValue());
        }
        return map;
    }
}
