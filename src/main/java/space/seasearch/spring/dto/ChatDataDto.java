package space.seasearch.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class ChatDataDto {

    private int totalMessages;

    private int incomingMessages;

    private int outgoingMessages;

    private Instant oldestMessageInstant;

    private double averageMessages;

    private int maxDailyAmount;

    private int daysActive;

    private long words;

    private long symbols;

    private int audioCount;

    private long stickers;

    private long photos;

    private long videos;

    private long documents;
}
