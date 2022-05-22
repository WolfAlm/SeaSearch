package space.seasearch.spring.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import space.seasearch.spring.entity.SeaSearchUser;

public interface UserRepository extends MongoRepository<SeaSearchUser, String> {
}
