package space.seasearch.spring.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import space.seasearch.spring.entity.UserInfo;

public interface UserRepository extends MongoRepository<UserInfo, String> {
}
