package space.seasearch.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChatInfoDto {

    private int outgoingMessage;
    private int incomingMessage;

    private long outgoingWord;
    private long incomingWords;

    private long outgoingSymbols;
    private long incomingSymbols;

    private int outgoingAudio;
    private int incomingAudio;

    private int outgoingDocument;
    private int incomingDocument;

    private int outgoingPhoto;
    private int incomingPhoto;

    private int outgoingSticker;
    private int incomingSticker;

    private int outgoingVideo;
    private int incomingVideo;

    private int outgoingForward;
    private int incomingForward;

    private int countDaysMessage;

    private int totalMessages;

    private int totalIncoming;

    private int totalOutgoing;

    private int countMaxMessage;

    private String dateFirstMessage;

    private int countAverageMessage;

    private int countDaysActive;

}
