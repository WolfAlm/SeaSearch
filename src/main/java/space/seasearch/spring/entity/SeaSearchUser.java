package space.seasearch.spring.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Data
@Document(collection = "users")
public class SeaSearchUser{
    @Id
    private String username;
    private LocalDateTime lastActivity;
    private Map<Long, ChatStatsRaw> stats;
    private String tokenPath;
}
