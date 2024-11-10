package facebook.backend.Service;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import facebook.backend.Credentials.User;
import facebook.backend.Repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void save(User body) {
        userRepository.save(body);
    }
    public User getUserByMail(String mail){
        return userRepository.findByMail(mail);
    }
    public Optional<User> getUserById(String userid){
        ObjectId objectId = new ObjectId(userid);
        return userRepository.findById(objectId);
    }
    public void save(Optional<User> user) {
        userRepository.save(user.get());
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
