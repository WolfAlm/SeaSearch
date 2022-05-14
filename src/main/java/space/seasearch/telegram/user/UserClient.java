package space.seasearch.telegram.user;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Error;
import it.tdlight.tdlight.ClientManager;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import space.seasearch.spring.exception.TelegramException;
import space.seasearch.telegram.communication.chat.Dialog;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Data
public class UserClient {

    @Getter
    private int currentStateConstructor;
    private String currentError = "";
    public CountDownLatch countDownLatch;
    private TelegramClient client;
    @Getter
    private final Dialog dialog;
    @Getter
    private String token;
    private int status;


    public UserClient(TelegramClient telegramClient) {
        this.client = telegramClient;
        dialog = new Dialog(client);
    }

    public String getCurrentError() {
        var error = this.currentError;
        this.currentError = "";
        return error;
    }

    public void start() {
        client.initialize(
                new UpdateHandler(),
                (object) -> System.out.println(object.getMessage()),
                (object) -> System.out.println(object.getMessage())
        );
    }

    public CountDownLatch startRequest() {
        this.countDownLatch = new CountDownLatch(1);
        return this.countDownLatch;
    }

    public void authPhoneNumber(String phoneNumber) {
        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null),
                new AuthorizationRequestHandler());
    }

    public void authCode(String code) {
        client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
    }


    public void authPassword(String password) {
        client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
    }

    public void exitUser() {
        client.send(new TdApi.LogOut(), new AuthorizationRequestHandler());
    }

    private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        switch (authorizationState.getConstructor()) {
            //нужно отправить парамтер бибилотеки
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                TdApi.TdlibParameters parameters = getTdlibParameters();
                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
            }
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
            //ожидаем тлф
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                currentStateConstructor = TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR;
                countDownLatch.countDown();
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                currentError = "";
                currentStateConstructor = TdApi.AuthorizationStateWaitCode.CONSTRUCTOR;
                countDownLatch.countDown();
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                currentError = "";
                currentStateConstructor = TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR;
                countDownLatch.countDown();
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                currentError = "";
                currentStateConstructor = TdApi.AuthorizationStateReady.CONSTRUCTOR;
                countDownLatch.countDown();
            }
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                client = ClientManager.create();
                start();
            }
            default -> System.err.println("Unsupported authorization state: " + authorizationState);
        }
    }

    private TdApi.TdlibParameters getTdlibParameters() {
        this.token = UUID.randomUUID().toString();
        TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
        parameters.databaseDirectory = "database/" + token;
        parameters.useMessageDatabase = true;
        parameters.useChatInfoDatabase = false;
        parameters.useFileDatabase = true;
        parameters.useTestDc = false;
        parameters.useSecretChats = false;
        parameters.apiId = 3993284;
        parameters.apiHash = "c4b3283315cbabc63dd8f9150f1ebf4d";
        parameters.systemLanguageCode = "ru";
        parameters.deviceModel = "Desktop";
        parameters.applicationVersion = "1.0";
        parameters.enableStorageOptimizer = true;
        return parameters;
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
                    if (dialog.getParserDialog() != null) {
                        TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                        TdApi.Chat chat = updateNewChat.chat;
                        dialog.getParserDialog().addChat(chat);
                    }
                    break;
                // Получает позиции для этих чатов.
                case TdApi.UpdateChatPosition.CONSTRUCTOR:
                    if (dialog.getParserDialog() != null) {
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
                status = ((Error) object).code;
                countDownLatch.countDown();
                System.err.println("AuthorizationRequestHandler:" + object);
            }
        }
    }

    public boolean isWaitingForPhoneNumber() {
        return this.currentStateConstructor == TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR;
    }

    public boolean hasError() {
        return !currentError.isEmpty();
    }

    public boolean isWaitingCode() {
        return this.currentStateConstructor == TdApi.AuthorizationStateWaitCode.CONSTRUCTOR;
    }

    public int getStatus() {
        var tempStatus = status;
        this.status = 0;
        return tempStatus;
    }


    public boolean isWaitingPassword() {
        return this.currentStateConstructor == TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR;
    }

    public boolean is2FA() {
        return this.currentStateConstructor == TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR;
    }

    public TelegramException getException() {
        return new TelegramException(this.getCurrentError(), this.getStatus());
    }
}