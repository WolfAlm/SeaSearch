package space.seasearch.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class GraphDto {

    private List<DailyMessagesDto> outgoingDailyMessages;

    private List<DailyMessagesDto> incomingDailyMessages;
}
