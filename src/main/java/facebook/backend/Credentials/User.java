package facebook.backend.Credentials;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "user")
public class User {
    private ObjectId id;
    private String name;
    private String mail;
    private String password;
    private String dob;
    private String gender;
    private String profile;
    private boolean isOnline;
    private String lastseen;
    private List<Notifications> notifications;
    private boolean notificationView;
    private List<Post> savepost;
    private String emailverifyotp;
    private List<String> followers;
    private List<String> following;

    public String getId(){
        return id.toHexString();
    }
}
