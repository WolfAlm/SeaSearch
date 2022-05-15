package space.seasearch.spring.dto;

import it.tdlight.jni.TdApi;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatDto {

    private long id;
    private String title;
    private String base64Image;
    private boolean isPrivate;
    private Instant updatedInstant;
}
