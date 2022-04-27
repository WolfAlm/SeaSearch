package space.seasearch.spring.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "users")
public class UserInfo {
    @Id
    private String username;
    private LocalDateTime lastActivity;
    private Map<Long, ChatStatsRaw> stats;
}
