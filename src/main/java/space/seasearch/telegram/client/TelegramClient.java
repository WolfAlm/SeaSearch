package space.seasearch.telegram.client;

import lombok.Getter;
import lombok.Setter;
import space.seasearch.telegram.communication.chat.Dialog;

import java.util.concurrent.CountDownLatch;

public abstract class TelegramClient {

    @Getter
    protected int currentStateConstructor;
    protected String currentError = "";
    public CountDownLatch countDownLatch;
    @Getter
    protected it.tdlight.common.TelegramClient client;
    @Getter
    @Setter
    protected String token =null;
    protected int status;

    protected TelegramClient(it.tdlight.common.TelegramClient client){
        this.client = client;
    }

    public CountDownLatch startRequest() {
        this.countDownLatch = new CountDownLatch(1);
        return this.countDownLatch;
    }

    public int getStatus() {
        var tempStatus = status;
        this.status = 0;
        return tempStatus;
    }

    public String getCurrentError() {
        var error = this.currentError;
        this.currentError = "";
        return error;
    }
}
