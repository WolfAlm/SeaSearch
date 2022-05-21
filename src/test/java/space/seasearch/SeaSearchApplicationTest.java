//package space.seasearch;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.MongoDBContainer;
//import space.seasearch.spring.repository.UserRepository;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public abstract class SeaSearchApplicationTest {
//    static final MongoDBContainer MONGO_DB_CONTAINER;
//
//    static {
//        MONGO_DB_CONTAINER = new MongoDBContainer("mongo:latest").withExposedPorts(27017);
//        MONGO_DB_CONTAINER.start();
//    }
//
////    @DynamicPropertySource
////    public void properties(DynamicPropertyRegistry registry) {
////        registry.add("spring.datasource.url", MONGO_DB_CONTAINER::getHost);
////        registry.add("spring.datasource.password", MONGO_DB_CONTAINER::getPort);
////        registry.add("spring.datasource.username", MONGO_DB_CONTAINER::getUsername);
////    }
//
//    @Autowired
//    public UserRepository userRepository;
//}
