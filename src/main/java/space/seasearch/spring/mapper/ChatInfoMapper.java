package space.seasearch.spring.mapper;

import org.springframework.stereotype.Component;
import space.seasearch.spring.dto.ChatInfoDto;
import space.seasearch.spring.dto.DailyMessagesDto;
import space.seasearch.spring.dto.DictWordDto;
import space.seasearch.spring.dto.GraphDto;
import space.seasearch.telegram.stats.info.InfoStats;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class ChatInfoMapper {

    public ChatInfoDto map(InfoStats info) {
        return ChatInfoDto.builder()
                .countAverageMessage(getTotalMessages(info) / getTotalActivDays(info))
                .countDaysActive(getTotalActivDays(info))
                .countDaysMessage(info.getCountDaysMessage())
                .countMaxMessage(getMaxMessages(info))
                .dateFirstMessage((info.getDateFirstMessage()))
                .incomingAudio(info.getIncomingAudio())
                .incomingDocument(info.getIncomingDocument())
                .incomingForward(info.getIncomingForward())
                .incomingMessage(info.getIncomingMessage())
                .incomingPhoto(info.getIncomingPhoto())
                .incomingSticker(info.getIncomingSticker())
                .incomingSymbols(info.getIncomingSymbols())
                .incomingVideo(info.getIncomingVideo())
                .incomingWords(info.getIncomingWords())
                .outgoingAudio(info.getOutgoingAudio())
                .outgoingDocument(info.getOutgoingDocument())
                .outgoingForward(info.getOutgoingForward())
                .outgoingMessage(info.getOutgoingMessage())
                .outgoingPhoto(info.getOutgoingPhoto())
                .outgoingSticker(info.getOutgoingSticker())
                .outgoingSymbols(info.getOutgoingSymbol())
                .outgoingVideo(info.getOutgoingVideo())
                .outgoingWord(info.getOutgoingWord())
                .totalIncoming(getTotalIncoming(info))
                .totalOutgoing(getTotalOutgoing(info))
                .totalMessages(getTotalMessages(info))
                .build();
    }

    public GraphDto mapToGraphDto(InfoStats stats) {
        return GraphDto.builder()
                .incomingDailyMessages(map(stats.getDateToMessageCountIncoming()))
                .outgoingDailyMessages(map(stats.getDateToMessageCountOutgoing()))
                .build();
    }

    public List<DictWordDto> mapToDictionary(InfoStats stats) {
        return stats.getDictionaryWords().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(100)
                .map(entry -> DictWordDto.builder().word(entry.getKey()).count(entry.getValue()).build())
                .toList();
    }

    private List<DailyMessagesDto> map(Map<LocalDate, Integer> localDateToCount) {
        return localDateToCount.entrySet()
                .stream()
                .map(entry -> DailyMessagesDto.builder()
                        .date(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
    }

    private int getTotalMessages(InfoStats stats) {
        return stats.getDateToMessageCountIncoming().values().stream().reduce(0, Integer::sum)
                + stats.getDateToMessageCountOutgoing().values().stream().reduce(0, Integer::sum);
    }

    private int getTotalActivDays(InfoStats stats) {
        return stats.getDateToDailyMessageCount().size();
    }

    private int getMaxMessages(InfoStats stats) {
        return stats.getDateToDailyMessageCount().values().stream().max(Integer::compareTo).orElse(0);
    }

    private int getTotalIncoming(InfoStats stats) {
        return stats.getDateToMessageCountIncoming().values().stream().reduce(0, Integer::sum);
    }

    private int getTotalOutgoing(InfoStats stats) {
        return stats.getDateToMessageCountOutgoing().values().stream().reduce(0, Integer::sum);
    }
}
