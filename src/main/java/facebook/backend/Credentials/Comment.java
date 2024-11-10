package facebook.backend.Credentials;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Comment {
    private String userId;
    private String postId;
    private String comment;
    private User user;
    private String dateTime;
}
