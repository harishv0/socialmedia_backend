package facebook.backend.Repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import facebook.backend.Credentials.User;

@Repository
public interface  UserRepository extends  MongoRepository<User, ObjectId> {
    User findByMail(String mail);
}
