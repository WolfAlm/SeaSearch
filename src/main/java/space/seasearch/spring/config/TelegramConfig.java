package space.seasearch.spring.config;

import it.tdlight.jni.TdApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class TelegramConfig {

    @Bean
    public TdApi.TdlibParameters tdlibParams() {
        TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
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
        return parameters;
    }
}
