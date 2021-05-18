package space.seasearch.telegram.communication.message;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.GetChatHistory;
import it.tdlight.jni.TdApi.MessageAudio;
import it.tdlight.jni.TdApi.MessageDocument;
import it.tdlight.jni.TdApi.MessagePhoto;
import it.tdlight.jni.TdApi.MessageSticker;
import it.tdlight.jni.TdApi.MessageText;
import it.tdlight.jni.TdApi.MessageVideo;
import it.tdlight.jni.TdApi.MessageVoiceNote;
import it.tdlight.jni.TdApi.Messages;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import lombok.Getter;
import lombok.Setter;
import space.seasearch.telegram.stats.info.InfoStats;

public class Message {

  /**
   * Позиции сообщений в порядке возрастания.
   */
  private NavigableSet<PositionMessage> positionMessages;
  /**
   * Список исходящих сообщений.
   */
  @Setter
  @Getter
  private List<TdApi.Message> outgoingMessages = new ArrayList<>();

  /**
   * Список входящих сообщений.
   */
  @Setter
  @Getter
  private List<TdApi.Message> incomingMessages = new ArrayList<>();
  /**
   * Список смешанных сообщений.
   */
  @Setter
  @Getter
  private List<TdApi.Message> messages = new ArrayList<>();
  private final TelegramClient client;
  /**
   * ID счата, с которого были и получены сообщения.
   */
  private final Long idChat;
  /**
   * Состояние, которое обозначает то, есть ли все сообщения или нет.
   */
  @Getter
  private boolean haveFullMessages;
  /**
   * Состояние, которое проверяет, был ли уже запущен процесс парсинга, и если нет – то запускает и
   * меняется состояние.
   */
  @Setter
  private boolean startParse;
  /**
   * Сколько всего сообщений уже было получено.
   */
  @Getter
  private int countMessage;
  /**
   * Последнее сообщение, которое было получено.
   */
  @Getter
  private TdApi.Message lastMessage;

  public Message(TelegramClient client, Long idChat) {
    this.idChat = idChat;
    this.client = client;
    positionMessages = new TreeSet<>();
  }

  /**
   * Запускает процесс получения сообщений для диалога, если не был запущен.
   */
  public void startParseMessage() {
    if (!startParse) {
      startParse = true;
      if (positionMessages.size() == 0) {
        parseMessage();
      }
    }
  }

  /**
   * Добавляет сообщения по типам сообщений, определяет содержание, после чего позиционные сообщения
   * сбрасываются.
   *
   * @param infoStats Профильная информация про его сообщения.
   */
  public void makeMessage(InfoStats infoStats) {
    if (positionMessages == null) {
      return;
    }

    for (PositionMessage positionMessage : positionMessages) {
      TdApi.Message message = positionMessage.getMessage();
      messages.add(message);

      if (message.isOutgoing) {
        outgoingMessages.add(message);
      } else {
        incomingMessages.add(message);
      }

      checkTypeMessage(message, infoStats);
    }

    positionMessages = new TreeSet<>();
  }

  /**
   * Проверяет тип сообщений и в зависимости от типа обновляется конкретная информация у статистики,
   * если сообщение является репостом, то просто засчитываем количество репостов и возвращаемся.
   *
   * @param message Сообщение, у которого нужно проверить.
   * @param stats   Профильная информация, куда нужно заносить данные.
   */
  private void checkTypeMessage(TdApi.Message message, InfoStats stats) {
    // Проверка на репост.
    if (message.forwardInfo != null) {
      if (message.isOutgoing) {
        stats.setOutgoingForward(stats.getOutgoingForward() + 1);
      } else {
        stats.setIncomingForward(stats.getIncomingForward() + 1);
      }

      return;
    }
    // Проверка на тип сообщения и в зависимости от типа сообщения - входящего или исходящего
    // прибавляем в количестве на единичку.
    switch (message.content.getConstructor()) {
      case MessageVoiceNote.CONSTRUCTOR:
        if (message.isOutgoing) {
          stats.setOutgoingAudio(stats.getOutgoingAudio() + 1);
        } else {
          stats.setIncomingAudio(stats.getIncomingAudio() + 1);
        }
        break;
      case MessageDocument.CONSTRUCTOR:
        if (message.isOutgoing) {
          stats.setOutgoingDocument(stats.getOutgoingDocument() + 1);
        } else {
          stats.setIncomingDocument(stats.getIncomingDocument() + 1);
        }
        break;
      case MessagePhoto.CONSTRUCTOR:
        if (message.isOutgoing) {
          stats.setOutgoingPhoto(stats.getOutgoingPhoto() + 1);
        } else {
          stats.setIncomingPhoto(stats.getIncomingPhoto() + 1);
        }
        break;
      case MessageSticker.CONSTRUCTOR:
        if (message.isOutgoing) {
          stats.setOutgoingSticker(stats.getOutgoingSticker() + 1);
        } else {
          stats.setIncomingSticker(stats.getIncomingSticker() + 1);
        }
        break;
      case MessageText.CONSTRUCTOR:
        // Считаем количество символов.
        long symbol = ((MessageText) message.content).text.text.length();
        // Получаем список слов по разделителям.
        String[] words = ((MessageText) message.content).text.text.split("[,;:\\[\\]()+.\\\\!?\\s]+");
        // Заносим слова в словарь.
        Map<String, Integer> wordsDictionary = stats.getDictionaryWords();
        // Считаем количество слов.
        long countWords = 0;

        for (String word : words) {
          // Проверяем, что слово содержит только буквы.
          if (!UtilMessage.deleteNotLetters(word).equals("")) {
            countWords += word.length();

            wordsDictionary.merge(word.toLowerCase(), 1, Integer::sum);
          }
        }

        if (message.isOutgoing) {
          stats.setOutgoingSymbol(stats.getOutgoingSymbol() + symbol);
          stats.setOutgoingWord(stats.getOutgoingWord() + countWords);
        } else {
          stats.setIncomingSymbol(stats.getIncomingSymbol() + symbol);
          stats.setIncomingWord(stats.getIncomingWord() + countWords);
        }
        break;
      case MessageVideo.CONSTRUCTOR:
        if (message.isOutgoing) {
          stats.setOutgoingVideo(stats.getOutgoingVideo() + 1);
        } else {
          stats.setIncomingVideo(stats.getIncomingVideo() + 1);
        }
        break;
    }
  }

  /**
   * Отправляет запрос на получение новых сообщений, если были получены не все сообщения.
   */
  private void parseMessage() {
    synchronized (positionMessages) {
      if (!haveFullMessages) {
        // Получаем идентификатор последнего сообщения, от которого нужно получать
        // более старые сообщения.
        long fromMessageId = 0;

        if (!positionMessages.isEmpty()) {
          fromMessageId = positionMessages.last().getMessageID();
        }

        client.send(new GetChatHistory(idChat, fromMessageId, 0, 99, false),
            new MessageHandler());
      }
    }
  }

  /**
   * Получает новые сообщения от сервера Telegram, после чего добавляет всех их в список сообщений,
   * проверяет, есть ли новые сообщения среди добавленных, и если нет – прекращает посылать новые
   * запросы.
   */
  private class MessageHandler implements ResultHandler {

    @Override
    public void onResult(TdApi.Object object) {
      if (object.getConstructor() == Messages.CONSTRUCTOR) {
        TdApi.Message[] messagesId = ((Messages) object).messages;

        synchronized (positionMessages) {
          int size = positionMessages.size();

          for (var a : messagesId) {
            positionMessages.add(new PositionMessage(a.id, a));
          }

          countMessage = positionMessages.size();
          lastMessage = positionMessages.last().getMessage();

          if (size == countMessage) {
            haveFullMessages = true;
          }
        }

        parseMessage();
      } else {
        System.out.println("Message Handler... :" + object.getConstructor() + object);
      }
    }
  }
}
