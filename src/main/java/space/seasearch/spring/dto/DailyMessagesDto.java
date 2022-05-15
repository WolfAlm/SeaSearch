package space.seasearch.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class DailyMessagesDto {

    private int count;
    private LocalDate date;
}
