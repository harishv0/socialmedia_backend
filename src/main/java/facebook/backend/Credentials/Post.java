package facebook.backend.Credentials;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "post")
public class Post {
    private ObjectId id;
    private String name;
    private String mail;
    private String postId;
    private String userId;
    private String postType;
    private String dateTime;
    private String postUrl;
    private String description;
    private List<String> likes;
    private List<Comment> comment;

}
