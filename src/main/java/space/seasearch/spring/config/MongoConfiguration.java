package space.seasearch.spring.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableMongoRepositories(basePackages = "space.seasearch.spring.repository")
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Value("${mongo.host}")
    private String host;

    @Value("${mongo.port}")
    private String port;

    @Override
    protected String getDatabaseName() {
        return "seasearch";
    }

    @Override
    public MongoClient mongoClient() {
        final ConnectionString connectionString =
                new ConnectionString("mongodb://%s:%s/%s".formatted(host, port, getDatabaseName()));
        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    public Collection<String> getMappingBasePackages() {
        return Collections.singleton("space.seasearch.spring");
    }
}
