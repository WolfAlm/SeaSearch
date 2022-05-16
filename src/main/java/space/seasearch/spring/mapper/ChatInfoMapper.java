package space.seasearch.spring.mapper;

import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Component;
import space.seasearch.spring.dto.ChatInfoDto;
import space.seasearch.spring.dto.DailyMessagesDto;
import space.seasearch.spring.dto.DictWordDto;
import space.seasearch.spring.dto.GraphDto;
import space.seasearch.telegram.communication.message.UtilMessage;
import space.seasearch.telegram.stats.info.InfoStats;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
        LocalDate minDate = getDateByComparator(stats.getDateToMessageCountOutgoing().keySet(),
                stats.getDateToMessageCountIncoming().keySet(),
                LocalDate::compareTo);
        LocalDate maxDate = getDateByComparator(stats.getDateToMessageCountOutgoing().keySet(),
                stats.getDateToMessageCountIncoming().keySet(),
                reverseOrder(LocalDate::compareTo));
        return GraphDto.builder()
                .incomingDailyMessages(map(stats.getDateToMessageCountIncoming(), minDate, maxDate))
                .outgoingDailyMessages(map(stats.getDateToMessageCountOutgoing(), minDate, maxDate))
                .build();
    }

    private LocalDate getDateByComparator(Set<LocalDate> s1, Set<LocalDate> s2, Comparator<LocalDate> comparator) {
        return Stream.concat(s1.stream(), s2.stream()).min(comparator).orElseGet(LocalDate::now);
    }

    public List<DictWordDto> mapToDictionary(InfoStats stats) {
        return stats.getDictionaryWords().entrySet()
                .stream()
                .filter(entry -> !UtilMessage.EXTRA_WORDS.contains(entry.getKey()))
                .sorted(reverseOrder(Map.Entry.comparingByValue()))
                .limit(100)
                .map(entry -> DictWordDto.builder().word(entry.getKey()).count(entry.getValue()).build())
                .toList();
    }

    private List<DailyMessagesDto> map(Map<LocalDate, Integer> localDateToCount, LocalDate minDate, LocalDate maxDate) {
        // SPLICE IN THE DATES
        for (LocalDate travDate = minDate; travDate.isBefore(maxDate); travDate = travDate.plus(1, ChronoUnit.DAYS)) {
            localDateToCount.putIfAbsent(travDate, 0);
        }

        return localDateToCount.entrySet()
                .stream()
                .map(entry -> DailyMessagesDto.builder()
                        .date(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(DailyMessagesDto::getDate))
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
