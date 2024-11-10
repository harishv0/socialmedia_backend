package facebook.backend.Service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {
    
    private Cloudinary cloudinary;

    @Autowired
    public void Cloudinary(Cloudinary cloudinary){
        this.cloudinary = cloudinary;
    }

    public String uploadProfile(MultipartFile image, String foldername, String userName) throws IOException{
        String url = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap("folder",foldername,
                                                 "public_id",userName,"resource_type","auto")).get("url").toString();
        return url;
    }

    public String uploadNewPost(MultipartFile image, String foldername, String userName) throws IOException{
        String url = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap("folder",foldername,
                                                 "public_id",userName,"resource_type","auto")).get("url").toString();
        return url;
    }
}


