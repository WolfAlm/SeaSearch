package space.seasearch.spring.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Document(collection = "users")
public class SeaSearchUser{
    @Id
    private String phoneNumber;
    private LocalDateTime lastActivity;
    private Map<Long, ChatStatsRaw> stats;
    private String tokenPath;
    private Set<Long> chatIds;
}
