package facebook.backend.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import facebook.backend.Credentials.Comment;
import facebook.backend.Credentials.Notifications;
import facebook.backend.Credentials.Post;
import facebook.backend.Credentials.User;
import facebook.backend.Response.ApiResponse;
import facebook.backend.Service.CloudinaryService;
import facebook.backend.Service.PostService;
import facebook.backend.Service.UserService;


@RestController
@RequestMapping("api/post")
@CrossOrigin(origins = "https://harishmedia.netlify.app")//https://harishmedia.netlify.app
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private ApiResponse response;

    
    @PostMapping("/newpost")
    public ResponseEntity<ApiResponse<Post>> newpost(@RequestParam("file") MultipartFile file, @RequestParam("mail") String mail, @RequestParam("name") String name,
                                                    @RequestParam("userid") String userId,@RequestParam("posttype") String postType, @RequestParam("description") String description) throws IOException {
        Post post = new Post();
        String folderName = "";
        System.out.println("Entered");
        try {
            System.out.println("Entered try");
            if(description != null || file != null){
                if(mail != null){
                    String[] userMailParts = mail.split("@");
                    folderName += "Post/"+userMailParts[0];
                }
                String postUrl = cloudinaryService.uploadNewPost(file, folderName, UUID.randomUUID().toString());
        
                LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));
                String dataTime = currentDate.toString()+ " " + currentTime.toString();
        
                post.setMail(mail);
                post.setUserId(userId);
                post.setPostId(postService.generatePostId());
                post.setName(name);
                post.setPostType(postType);
                post.setDateTime(dataTime);
                post.setPostUrl(postUrl);
                post.setDescription(description);
                post.setComment(new ArrayList<>());
                post.setLikes(new ArrayList<>());
        
                postService.save(post);
                return ResponseEntity.ok(new ApiResponse<>(true, "Post Uploaded Successfully", post));
            }else{
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Description or Post data is empty", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Description or Post data is empty", null));
        }
    }

    @GetMapping("/allpost/{userid}")
    public ResponseEntity<ApiResponse<List<Post>>> allPost(@PathVariable String userid) {
        List<Post> returnpost = new ArrayList<>();
        
        try {
            List<Post> allpost = postService.getAllPosts();
            Optional<User> user = userService.getUserById(userid);
            
            for (String followerId : user.get().getFollowing()) {
                List<Post> followerPosts = postService.getPostById(followerId);
                
                if (!followerPosts.isEmpty()) {
                    returnpost.addAll(followerPosts);
                }
            }
            for (Post post : allpost) {
                if (post.getPostType().equals("Public") && !returnpost.contains(post)) {
                    returnpost.add(post);
                }
                if(post.getUserId().equals(userid) && !returnpost.contains(post)){
                    returnpost.add(post);
                }
            }
            ApiResponse<List<Post>> response = new ApiResponse<>(true, "Fetch all post success", returnpost);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<Post>> response = new ApiResponse<>(false, "Server Error", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/postlike/{userId}/{postUserId}")
    public ResponseEntity<ApiResponse<Post>> userLike(@PathVariable String userId, @PathVariable String postUserId) {
        Post userPost = postService.getPostByPostId(postUserId);
        
        if(!userPost.getLikes().contains(userId)){
            userPost.getLikes().add(userId);
            postService.save(userPost);

            

            if(!userId.equals(userPost.getUserId())){
                Optional<User> postOwner = userService.getUserById(userPost.getUserId());
                Optional<User> liker = userService.getUserById(userId);

                if(postOwner.get().getNotifications() == null){
                    postOwner.get().setNotifications(new ArrayList<>());
                }

                Notifications notifications = new Notifications();
                notifications.setNotificationId(UUID.randomUUID().toString());
                notifications.setUserId(userPost.getUserId());
                notifications.setPostId(postUserId);
                notifications.setNotificationMessage(liker.get().getName() + " is liked your post");
                notifications.setSeen(false);
                notifications.setDateTime(LocalDateTime.now().toString());

                postOwner.get().getNotifications().add(notifications);
                userService.save(postOwner.get());
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Liked SuccessFully", userPost));
        }else{
            userPost.getLikes().remove(userId);
            postService.save(userPost);
            return ResponseEntity.ok(new ApiResponse<>(false, "Dislike SuccessFully", userPost));
        }
        
    }
    @PostMapping("/notifications/{userId}/{notificationId}")
    public ResponseEntity<ApiResponse<User>> markNotificationAsSeen(@PathVariable String userId, @PathVariable String notificationId) {
        Optional<User> user = userService.getUserById(userId);
        if (user != null ) {
            for (Notifications notifi : user.get().getNotifications()) {
                if(notifi.getNotificationId().equals(notificationId)){
                    notifi.setSeen(true);
                }
            }
            userService.save(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as seen.", user.get()));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "user not found", null));
        }
    }
    @GetMapping("/getpostbyid/{postId}")
    public ResponseEntity<Post> getPostByPostId(@PathVariable String postId) {
        Post post = postService.getPostByPostId(postId);
        if (post == null) {
            return ResponseEntity.notFound().build(); 
        }
        return ResponseEntity.ok(post);
    }
    
    
    @PostMapping("/comment")
    public ResponseEntity<ApiResponse<Post>> addComment(@RequestParam("postId") String postId, @RequestParam("userId") String userId,@RequestParam("comment") String comment) throws InterruptedException{
        System.out.println("postId: " + postId + ", userId: " + userId + ", comment: " + comment);
        Post currentPost = postService.getPostByPostId(postId);
        if (currentPost == null) {
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(new ApiResponse<>(false, "Post not found", null));
        }else{
            Optional<User> user = userService.getUserById(userId);
            System.out.println(user.get().getName());
            Comment com = new Comment();
            com.setPostId(postId);
            com.setUserId(userId);
            com.setUser(user.get());
            com.setComment(comment);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            com.setDateTime(LocalDateTime.now(ZoneOffset.UTC).format(formatter));
            currentPost.getComment().add(com);
            postService.save(currentPost);
            simpMessagingTemplate.convertAndSend("/topic/comments", com);


            //notification
            Optional<User> postUser = userService.getUserById(currentPost.getUserId());
            if(postUser.get().getNotifications() == null){
                postUser.get().setNotifications(new ArrayList<>());
            }
            Notifications notifications = new Notifications();
            notifications.setNotificationId(UUID.randomUUID().toString());
            notifications.setDateTime(LocalDateTime.now(ZoneOffset.UTC).format(formatter));
            notifications.setUserId(user.get().getId().toString());
            notifications.setPostId(postId);
            notifications.setNotificationMessage(user.get().getName() + " Commented on your post");
            notifications.setSeen(false);
            postUser.get().getNotifications().add(notifications);
            userService.save(postUser);
            return ResponseEntity.ok(new ApiResponse<>(true, "Commented", currentPost));
        }
        
    }
    
    @GetMapping("getuserpost/{id}")
    public ResponseEntity<ApiResponse<List<Post>>> getUserPost(@PathVariable String id) {
        List<Post> getUserPost = postService.getPostById(id);
        if(getUserPost.size() > 0){
            return ResponseEntity.ok(new ApiResponse<>(true, "User Posts", getUserPost));
        }else{
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "User Posts not available", null));
        }
    }
    
    @GetMapping("/allvideos")
    public ResponseEntity<ApiResponse<List<Post>>> getAllVideos() {
        List<Post> returnPosts = new ArrayList<>();
        List<Post> allPosts = postService.getAllPosts();

        for (Post post : allPosts) {
            if(post.getPostUrl().endsWith("mp4") || post.getPostUrl().endsWith("mkv")){
                returnPosts.add(post);
            }
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "All videos", returnPosts));

    }
    
}
