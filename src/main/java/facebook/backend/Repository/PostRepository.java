package facebook.backend.Repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import facebook.backend.Credentials.Post;

public interface PostRepository extends MongoRepository<Post, ObjectId> {
    List<Post> findByUserId(String userid);
    Post findByPostId(String postId);
}
