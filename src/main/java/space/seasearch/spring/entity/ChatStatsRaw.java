package space.seasearch.spring.entity;

import lombok.Data;
import space.seasearch.telegram.stats.info.MessagesPerDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ChatStatsRaw {
    private ChatObjects incoming;
    private ChatObjects outgoing;
    private Map<String, Integer> wordCount;
    private List<MessagesPerDay> incomingMessages;
    private List<MessagesPerDay> outgoingMessages;
    private LocalDate oldestMessageDate;
}
