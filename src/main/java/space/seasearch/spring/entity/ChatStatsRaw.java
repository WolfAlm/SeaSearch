package space.seasearch.spring.entity;

import lombok.Data;
import space.seasearch.telegram.stats.info.InfoStats;
import space.seasearch.telegram.stats.info.MessagesPerDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ChatStatsRaw {
    private ChatObjects incoming;
    private ChatObjects outgoing;
    private Map<String, Integer> wordCount;
    private List<MessagesPerDay> incomingMessages;
    private List<MessagesPerDay> outgoingMessages;
    private String oldestMessageDate;

    public ChatStatsRaw() {
        incoming = new ChatObjects();
        outgoing = new ChatObjects();
        wordCount = new HashMap<>();
        incomingMessages = new ArrayList<>();
        outgoingMessages = new ArrayList<>();
    }

    public ChatStatsRaw(InfoStats stats) {
        incoming = new ChatObjects();
        incoming.setAudio(stats.getIncomingAudio());
        incoming.setDocument(stats.getIncomingDocument());
        incoming.setForward(stats.getIncomingForward());
        incoming.setPhoto(stats.getIncomingPhoto());
        incoming.setSticker(stats.getIncomingSticker());
        incoming.setSymbol(stats.getIncomingSymbol());
        incoming.setVideo(stats.getIncomingVideo());
        incoming.setWord(stats.getIncomingWord());

        outgoing = new ChatObjects();
        outgoing.setAudio(stats.getOutgoingAudio());
        outgoing.setDocument(stats.getOutgoingDocument());
        outgoing.setForward(stats.getOutgoingForward());
        outgoing.setPhoto(stats.getOutgoingPhoto());
        outgoing.setSticker(stats.getOutgoingSticker());
        outgoing.setSymbol(stats.getOutgoingSymbol());
        outgoing.setVideo(stats.getOutgoingVideo());
        outgoing.setWord(stats.getOutgoingWord());

        incomingMessages = stats.getMessagesIncomingOfDay();
        outgoingMessages = stats.getMessagesOutgoingOfDay();

        wordCount = stats.getDictionaryWords();
        oldestMessageDate = stats.getDateFirstMessage();
    }
}
