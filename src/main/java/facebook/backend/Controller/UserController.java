package facebook.backend.Controller;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.catalina.mbeans.UserMBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import facebook.backend.Service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudinary.http44.api.Response;

import facebook.backend.Credentials.Post;
import facebook.backend.Credentials.User;
import facebook.backend.Response.ApiResponse;
import facebook.backend.Service.CloudinaryService;
import facebook.backend.Service.EmailService;
import facebook.backend.Service.PostService;


@RestController
@RequestMapping("api/user")
@CrossOrigin(origins = "https://harishmedia.netlify.app") //http://localhost:3000
public class UserController {

    @Autowired
    private UserService userService;

    private ApiResponse response;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PostService postService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    //otp genarator
    public String otpGenarator(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(999999);
        return String.valueOf(otp);
    }
    //sendotp
    @GetMapping("/otpsend")
    public ResponseEntity<ApiResponse<User>> sendOTP(@RequestParam("mail") String mail) {
        String currentOtp = this.otpGenarator();
        User user = userService.getUserByMail(mail);
        try {
            if(user != null){
                emailService.sendOtp(mail, currentOtp);
                user.setEmailverifyotp(currentOtp);
                userService.save(user);
                response = new ApiResponse<User>(true, "Otp sended successfully", user);
                return ResponseEntity.ok(response);
            }else{
                response = new ApiResponse<User>(false, "Failed to send", null);
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (MailException e) {
            e.printStackTrace(); // Log the error
            response = new ApiResponse<User>(false, "Failed to send", null);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    public String passwordEncryption(String password) {
        String encrypted = Base64.getEncoder().encodeToString(password.getBytes());
        return encrypted;
    }
    
    public String passwordDecryption(String password){
        byte[] bytes = Base64.getDecoder().decode(password);
        String decrypted = new String(bytes);
        return decrypted;
    }
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User body){
        User user = userService.getUserByMail(body.getMail());
        if(user == null){
            try {
                System.out.println(body.getName());
                body.setFollowers(new ArrayList<>());
                body.setFollowing(new ArrayList<>());
                body.setOnline(false);
                body.setLastseen(new String());
                body.setSavepost(new ArrayList<>());
                body.setPassword(this.passwordEncryption(body.getPassword()));
                userService.save(body);
                return ResponseEntity.ok("Signup succesfully");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body("Signup unsuccessfuly");
            }
        }else{
            return ResponseEntity.badRequest().body("This mail is alrady taken");
        }
        
    }

    @GetMapping("/signin")
    public ResponseEntity<ApiResponse<String>> signin(@RequestParam("mail") String mail, @RequestParam("password") String password) {
        User user = userService.getUserByMail(mail);
        if(user != null){
            if(this.passwordDecryption(user.getPassword()).equals(password)){
                String userid = user.getId().toString();
                user.setOnline(true);
                userService.save(user);
                simpMessagingTemplate.convertAndSend("/topic/userstatus", user);
                response = new ApiResponse<>(true, "Login Successfully", userid);
                return ResponseEntity.ok(response);
            }else{
                response = new ApiResponse<>(false, "Incorrect Password", null);
                return ResponseEntity.badRequest().body(response);
            }
        } else{
            response = new ApiResponse<>(false, "Invalid Email and Password", null);
            return ResponseEntity.badRequest().body(response);
        } 
    }

    @GetMapping("/useridbymail/{mail}")
    public String userIdByMail(@PathVariable String mail){
        User user = userService.getUserByMail(mail);
        if(user != null){
            return user.getId().toString();
        }
        return null;
    }
    @GetMapping("/getuserbymail")
    public ResponseEntity<ApiResponse<User>> forgotPassword(@RequestParam("mail") String mail){
        User user = userService.getUserByMail(mail);
        if(user != null){
            response = new ApiResponse<User>(true, "User Exist", user);
            return ResponseEntity.ok(response);
        }else{
            response = new ApiResponse<User>(false, "User Not Exist", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/changepassword")
    public ResponseEntity<ApiResponse<User>> changepassword(@RequestParam("mail") String mail, @RequestParam("password") String password) {
        User users = userService.getUserByMail(mail);
        // System.out.println("User " + users.getName());
        try {
            if(users != null){
                users.setPassword(password);
                userService.save(users);
                response = new ApiResponse<User>(true, "Password Changed Successfully", users);
                return ResponseEntity.ok(response);
            }
            else{
                response = new ApiResponse<User>(false, " Password cannot change ", null);
                return ResponseEntity.badRequest().body(response);
            } 
        } catch (Exception e) {
            response = new ApiResponse<User>(false, " Password cannot change ", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<User>> getModelById(@PathVariable String id) {

        Optional<User> user = userService.getUserById(id);
        if(user != null){
            response = new ApiResponse<>(true, "User Exist", user);
            return ResponseEntity.ok(response);
        }else{
            response = new ApiResponse<>(false, "User not Exist", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("uploadprofile")
    public ResponseEntity<ApiResponse<User>> postMethodName(@RequestParam("id") String id,@RequestParam("image") MultipartFile image) throws IOException {
        Optional<User> user = userService.getUserById(id);
        try {
            if(user.get() != null){
                int indexOf = user.get().getMail().indexOf('@');
                String folderName = user.get().getMail().substring(0,indexOf);
                String profileUrl = cloudinaryService.uploadProfile(image, folderName, UUID.randomUUID().toString());
                user.get().setProfile(profileUrl);
                userService.save(user);
                response = new ApiResponse<>(true, "Upload Successfully", user);
                return ResponseEntity.ok(response);
            }else{
                response = new ApiResponse<>(false, "Not Upload Successfully", null);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response = new ApiResponse<>(false, "Nots Upload Successfully", null);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/getalluser/{id}")
    public ResponseEntity<ApiResponse<List<User>>> getAllUser(@PathVariable String id){
        Optional<User> user = userService.getUserById(id);
        List<User> allUsers = userService.getAllUsers();
        allUsers.removeIf(users -> users.equals(user.get()));
        return ResponseEntity.ok(new ApiResponse<>( true, "All users ", allUsers));
    }

    @GetMapping("/getallusers/{id}")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(@PathVariable String id){
        Optional<User> userid = userService.getUserById(id);
        List<User> allUsers = userService.getAllUsers();

        allUsers.removeIf(user -> user.equals(userid.get()));

        allUsers.removeIf(user -> userid.get().getFollowing().contains(user.getId().toString()));

        if(!allUsers.isEmpty()){
            response = new ApiResponse<>(true, "All users fetcheds", allUsers);
            return ResponseEntity.ok(response);
        }
        else{
            response = new ApiResponse<>(false, "Users are already in friends",null);
                return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/addfriend/{mail}/{id}")
    public ResponseEntity<ApiResponse<User>> addFriends(@PathVariable String mail, @PathVariable String id){

        Optional<User> userStroreFriend = userService.getUserById(id);
        User addFriend = userService.getUserByMail(mail);

        if(!userStroreFriend.get().getFollowing().contains(addFriend.getId().toString())){
            userStroreFriend.get().getFollowing().add(addFriend.getId().toString());
            userService.save(userStroreFriend);
            response = new ApiResponse<>(true, "Friend Added", userStroreFriend);
            return ResponseEntity.ok(response);
        }else{
            response = new ApiResponse<>(false, "Mot added", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/getfollowersById/{id}")
    public ResponseEntity<ApiResponse<List<User>>> getFollowersById(@PathVariable String id){
        Optional<User> userFollowers = userService.getUserById(id);
        List<String> userFollowersList = userFollowers.get().getFollowing();
        List<User> returnUserFollowersList = new ArrayList<>();
        if(userFollowersList.size() > 0){
            for (int i = 0; i < userFollowersList.size(); i++) {
                Optional<User> follower = userService.getUserById(userFollowersList.get(i));
                follower.ifPresent(returnUserFollowersList::add);
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Followers", returnUserFollowersList));
        }else{
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "No Folloers Available", null));
        }
    }

    @PostMapping("/removefreind/{userId}/{friendId}")
    public ResponseEntity<ApiResponse<User>> removeFriendInUser(@PathVariable String userId, @PathVariable String friendId) {
        Optional<User> currentUser = userService.getUserById(userId);
        try {
            if(currentUser.get().getFollowing().contains(friendId)){
                currentUser.get().getFollowing().remove(friendId);
                userService.save(currentUser.get());
                return ResponseEntity.ok(new ApiResponse<>(true, "Removed Successfully", currentUser.get()));
            }else{
                return ResponseEntity.ok(new ApiResponse<>(false, "Removed not Successfully", null));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "User Not Found", null));
        }
    }

    @PutMapping("/status/{userId}")
    public ResponseEntity<String> updateUserStatus(@PathVariable String userId, @RequestBody Map<String, Boolean> payload) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            user.get().setOnline(payload.get("online"));
            user.get().setLastseen(LocalDateTime.now().toString());
            userService.save(user.get());
            
            // Broadcast the user status update
            simpMessagingTemplate.convertAndSend("/topic/userstatus", user.get());
            return ResponseEntity.ok("User status updated successfully.");
        }
        return ResponseEntity.status(404).body("User not found.");
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<ApiResponse<User>> logout(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        if(user != null){
            user.get().setOnline(false);
            user.get().setLastseen(LocalDateTime.now().toString());
            userService.save(user.get());
            simpMessagingTemplate.convertAndSend("/topic/userstatus", user);
            return ResponseEntity.ok(new ApiResponse<>(true, "LogOut SuccessFully", user.get()));
        }else{
            return ResponseEntity.ok(new ApiResponse<>(false, "LogOut unSuccessFully", null));
        }
    }

    @PostMapping("/savepost/{userId}/{postId}")
    public ResponseEntity<ApiResponse<User>> getMethodName(@PathVariable String userId, @PathVariable String postId) {
        Optional<User> user = userService.getUserById(userId);
        Post post = postService.getPostByPostId(postId);
        System.out.println("user posts :------"+ user.get().getSavepost());
        if(user != null){
            if (user.get().getSavepost() == null) {
                user.get().setSavepost(new ArrayList<>()); 
            }
            if(!user.get().getSavepost().contains(post)) {
                System.out.println("Entered " + user.get().getSavepost().contains(post));
                user.get().getSavepost().add(post);
                userService.save(user);
                return ResponseEntity.ok(new ApiResponse<>(true, "Post Saved", null));
            }else{
                user.get().getSavepost().remove(post);
                userService.save(user);
                return ResponseEntity.ok(new ApiResponse<>(false, "Post UnSaved", null));
            }
        }else{
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "not Saved", null));
        }
    }
}
