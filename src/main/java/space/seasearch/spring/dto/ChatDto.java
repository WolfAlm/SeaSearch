package space.seasearch.spring.dto;

import it.tdlight.jni.TdApi;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatDto {

    private String title;
    private String base64Image;
    private boolean isPrivate;
}
