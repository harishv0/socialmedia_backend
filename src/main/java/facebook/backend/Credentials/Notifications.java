package facebook.backend.Credentials;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notifications {
    private String notificationId;
    private String userId;
    private String postId;
    private String notificationMessage;
    private boolean isSeen;
    private String dateTime;
}
