package space.seasearch.telegram.stats.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessagesPerDay {

  /**
   * В какой день хранится информация о количестве сообщения.
   */
  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonFormat(pattern="yyyy-MM-dd")
  private LocalDate date;
  /**
   * Количество сообщений.
   */
  private Long value;
}
