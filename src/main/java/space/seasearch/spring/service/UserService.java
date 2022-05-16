package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import space.seasearch.spring.entity.ChatObjects;
import space.seasearch.spring.entity.ChatStatsRaw;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.repository.UserRepository;
import space.seasearch.telegram.communication.message.Message;
import space.seasearch.telegram.stats.info.InfoStats;
import space.seasearch.telegram.stats.info.MessagesPerDay;
import space.seasearch.telegram.stats.profile.ProfileStats;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void saveUser(String username, Long chatId, ProfileStats stats) {
        SeaSearchUser user = getUser(username);
//        Map<Long, ChatStatsRaw> chatStats = user.getStats();
        ChatStatsRaw chat = new ChatStatsRaw(stats.getInfoStats());
        chat.setNewestMessageDate(stats.getMessage().getNewestMessageDate());
//        chatStats.put(chatId, chat);
        userRepository.save(user);
    }

    public void updateGroupIds(String phoneNumber, Set<Long> ids) {
        SeaSearchUser user = getUser(phoneNumber);
        var chats = user.getChatIds();
        if (chats == null || chats.isEmpty()) {
            user.setChatIds(ids);
        } else {
            user.getChatIds().addAll(ids);
        }
        userRepository.save(user);
    }

    public void updateStats(InfoStats stats, String username, Long chatId) {
        SeaSearchUser user = getUser(username);

        if (user.getChatIdToInfoStats() == null) {
            user.setChatIdToInfoStats(new HashMap<>());
        }
        if (user.getChatIdToInfoStats().get(chatId) == null) {
            user.getChatIdToInfoStats().put(chatId, stats);
        } else {
            user.getChatIdToInfoStats().put(chatId, stats);
        }

        userRepository.save(user);
    }


    public InfoStats getInfoStats(String username, Long chatId) {
        SeaSearchUser user = getUser(username);

        if (user.getChatIdToInfoStats() == null) {
            user.setChatIdToInfoStats(new HashMap<>());
        }

        InfoStats stats = user.getChatIdToInfoStats().get(chatId);

        if (stats == null) {
            stats = new InfoStats();
        }

        return stats;

    }

    private SeaSearchUser getUser(String username) {
        Optional<SeaSearchUser> userOpt = userRepository.findById(username);
        SeaSearchUser user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new SeaSearchUser();
            user.setPhoneNumber(username);
            user.setChatIdToInfoStats(new HashMap<>());
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
        to.setIncomingSymbols(incoming.getSymbol());
        to.setIncomingVideo(incoming.getVideo());
        to.setIncomingWords(incoming.getWord());

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
