package space.seasearch.telegram.user;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Error;
import it.tdlight.tdlight.ClientManager;
import java.util.concurrent.CountDownLatch;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import space.seasearch.telegram.communication.chat.Dialog;

@Data
public class UserClient {

  @Getter
  private int currentStateConstructor;
  @Getter
  private String currentError = "";
  public CountDownLatch countDownLatch;
  private TelegramClient client;
  @Getter
  private final Dialog dialog;
  private String token;

  public UserClient(TelegramClient telegramClient, String token) {
    this.client = telegramClient;
    this.token = token;
    dialog = new Dialog(client);
  }

  public void start() {
    client.initialize(
        new UpdateHandler(),
        (object) -> System.out.println(object.getMessage()),
        (object) -> System.out.println(object.getMessage())
    );
  }

  /**
   * Отправляет запрос с мобильным телефоном, введенным пользователем.
   *
   * @param phoneNumber Мобильный телефон пользователя.
   */
  public void authPhoneNumber(String phoneNumber) {
    client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null),
        new AuthorizationRequestHandler());
  }

  /**
   * Отправляет запрос с кодом аутентификации, который ввел пользователь.
   *
   * @param code Аутентификационный код пользователя.
   */
  public void authCode(String code) {
    client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
  }

  /**
   * Отправляет запрос с паролем, который ввел пользователь.
   *
   * @param password Пароль пользователя.
   */
  public void authPassword(String password) {
    client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
  }

  public void exitUser() {
    client.send(new TdApi.LogOut(), new AuthorizationRequestHandler());
  }

  private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
    switch (authorizationState.getConstructor()) {
      case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
        TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
        parameters.databaseDirectory = "database/" + token;
        parameters.useMessageDatabase = true;
        parameters.useChatInfoDatabase = false;
        parameters.useFileDatabase = true;
        parameters.useSecretChats = false;
        parameters.apiId = 3993284;
        parameters.apiHash = "c4b3283315cbabc63dd8f9150f1ebf4d";
        parameters.systemLanguageCode = "ru";
        parameters.deviceModel = "Desktop";
        parameters.applicationVersion = "1.0";
        parameters.enableStorageOptimizer = true;

        client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
        break;
      case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
        client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
        break;
      case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
        currentStateConstructor = TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR;
        countDownLatch.countDown();
        break;
      }
      case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
        String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) authorizationState).link;
        System.out.println("Please confirm this login link on another device: " + link);
        break;
      }
      case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
        currentError = "";
        currentStateConstructor = TdApi.AuthorizationStateWaitCode.CONSTRUCTOR;
        countDownLatch.countDown();
        break;
      }
      case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
        currentError = "";
        currentStateConstructor = TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR;
        countDownLatch.countDown();
        break;
      }
      case TdApi.AuthorizationStateReady.CONSTRUCTOR:
        currentError = "";
        currentStateConstructor = TdApi.AuthorizationStateReady.CONSTRUCTOR;
        countDownLatch.countDown();
        break;
      case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
        client = ClientManager.create();
        start();
        break;
      default:
        System.err.println("Unsupported authorization state: " + authorizationState);
    }
  }

  private class UpdateHandler implements ResultHandler {
    // Тут получается бесконечный мониторинг запросов
    @Override
    public void onResult(TdApi.Object object) {
      // Узнаем, какие исходящие вообще запросы.
      switch (object.getConstructor()) {
        // Авторизация пользователя.
        case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
          onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
          break;
        // Добавляет новые чаты.
        case TdApi.UpdateNewChat.CONSTRUCTOR:
          if (dialog.getParserDialog() != null && dialog.getParserDialog().isProcessParse()) {
            TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
            TdApi.Chat chat = updateNewChat.chat;
            dialog.getParserDialog().addChat(chat);
          }
          break;
        // Получает позиции для этих чатов.
        case TdApi.UpdateChatPosition.CONSTRUCTOR:
          if (dialog.getParserDialog() != null && dialog.getParserDialog().isProcessParse()) {
            dialog.getParserDialog().addPosition((TdApi.UpdateChatPosition) object);
          }
          break;
      }
    }

  }

  private class AuthorizationRequestHandler implements ResultHandler {

    @Override
    public void onResult(TdApi.Object object) {
      if (object.getConstructor() == Error.CONSTRUCTOR) {
        currentError = ((Error) object).message;
        countDownLatch.countDown();
        System.err.println("AuthorizationRequestHandler:" + object);
      }
    }
  }
}