package space.seasearch.spring.mapper;

import it.tdlight.jni.TdApi;
import org.springframework.stereotype.Component;
import space.seasearch.spring.dto.ChatDto;
import space.seasearch.telegram.photo.PhotoPath;

import java.util.regex.Pattern;

@Component
public class ChatMapper {

    public ChatDto map(TdApi.Chat chat) {
        return ChatDto.builder()
                .title(chat.title)
                .base64Image(getPhoto(chat.photo))
                .isPrivate(chatIsPrivate(chat))
                .build();
    }

    private String getPhoto(TdApi.ChatPhotoInfo photoInfo) {
        if (photoInfo != null) {
            String[] folders = photoInfo.small.local.path.split(Pattern.quote(java.io.File.separator));

            return PhotoPath.PATH_TO_DATABASE + folders[folders.length - 3]
                    + PhotoPath.PATH_TO_DOWNLOAD_PHOTO +
                    folders[folders.length - 1];
        } else {
            return null;
        }
    }

    private boolean chatIsPrivate(TdApi.Chat chat) {
        return chat.getConstructor() == TdApi.ChatTypePrivate.CONSTRUCTOR;
    }
}
