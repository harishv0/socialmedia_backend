package facebook.backend.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import facebook.backend.Credentials.Post;
import facebook.backend.Repository.PostRepository;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public String generatePostId() {
        String prefix = "POST";
        long count = postRepository.count();
        return prefix + String.format("%03d", count + 1);
    }

    public void save(Post post){
        postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostById(String userId){
        return postRepository.findByUserId(userId);
    }

    public Post getPostByPostId(String postUserId) {
        return postRepository.findByPostId(postUserId);
    }

}
