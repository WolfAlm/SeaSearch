package space.seasearch.telegram.communication.message;

import it.tdlight.jni.TdApi;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PositionMessage implements Comparable<PositionMessage> {

  /**
   * Идентификатор сообщения. Чем он больше, тем он старее.
   */
  @Getter
  private final long messageID;
  /**
   * Сообщение.
   */
  @Getter
  private final TdApi.Message message;

  @Override
  public int compareTo(PositionMessage o) {
    return Long.compare(o.messageID, this.messageID);
  }
}
