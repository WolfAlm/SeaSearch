package space.seasearch.telegram.communication.message;

import java.util.List;
import java.util.stream.Collectors;

public class UtilMessage {

  /**
   * Список ненужных слов для учета в облаке слов, например: предлоги, служебные части речи.
   */
  public final static List<String> EXTRA_WORDS = List
      .of("ты", "я", "он", "она", "оно", "где", "его", "ее", "ли", "как", "что", "в", "без", "до",
          "для", "за", "через", "над", "по", "из", "у", "около", "под", "о", "про", "на", "к",
          "перед", "при", "с", "между", "а", "от", "после", "сквозь", "об", "ага", "это", "вот",
          "так", "мне", "меня", "там", "еще", "нас", "ему", "им", "все", "себе", "ей", "тебе", "и",
          "не", "да", "то", "её", "но", "ну", "если", "вы", "мы", "бы", "же", "уже", "http",
          "https", "нет", "тебя", "вам", "они", "их", "вас");

  /**
   * Удаляет все символы, которые не являются буквами, в переданной строке.
   *
   * @param str Строка, из которой нужно удалить символы.
   * @return Полученная строка после удаления.
   */
  public static String deleteNotLetters(String str) {
    return str.chars()
        .filter(Character::isLetter)
        .mapToObj(i -> String.valueOf((char) i))
        .collect(Collectors.joining());
  }
}
