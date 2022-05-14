package space.seasearch.telegram.client;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Error;
import it.tdlight.tdlight.ClientManager;
import space.seasearch.spring.exception.TelegramException;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class UserClient extends space.seasearch.telegram.client.TelegramClient {



    public UserClient(TelegramClient telegramClient) {
       super(telegramClient);
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
        parameters.apiId = 12419938;
        parameters.apiHash = "22033f3a61b16e795125da5d8beb8b92";
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