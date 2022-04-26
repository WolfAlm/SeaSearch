package space.seasearch.telegram.communication.chat;

import it.tdlight.jni.TdApi;
import java.util.Objects;
import lombok.Getter;

public class PositionDialog implements Comparable<PositionDialog> {

  /**
   * ID чата.
   */
  @Getter
  private final long chatId;
  /**
   * Позиция чата. Чем больше число, тем выше в списке.
   */
  private final TdApi.ChatPosition position;

  PositionDialog(long chatId, TdApi.ChatPosition position) {
    this.chatId = chatId;
    this.position = position;
  }

  /**
   * @return Получает позицию чата в списке диалогов.
   */
  public long getOrder() {
    return position.order;
  }

  /**
   * позиции чатов между собой, при их равенстве, сравнивает по идентификатору чатов.
   *
   * @param o Другой чат, с че мнужно сравнивать.
   * @return Результат сравнения.
   */
  @Override
  public int compareTo(PositionDialog o) {
    if (this.position.order != o.position.order) {
      return o.position.order < position.order ? -1 : 1;
    }

    if (this.chatId != o.chatId) {
      return o.chatId < chatId ? -1 : 1;
    }

    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PositionDialog that = (PositionDialog) o;
    return chatId == that.chatId && position.order == that.position.order;
  }

  @Override
  public int hashCode() {
    return Objects.hash(chatId, position.order);
  }
}