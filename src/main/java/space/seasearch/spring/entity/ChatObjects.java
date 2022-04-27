package space.seasearch.spring.entity;

import lombok.Data;

@Data
public class ChatObjects {
    private long word;
    private long symbol;
    private int audio;
    private int document;
    private int photo;
    private int sticker;
    private int video;
    private int forward;
}
