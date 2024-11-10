package facebook.backend.Config;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;


@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinaryconfiguration(@Value("${cloudinary.cloud_name}") String cloudName, 
                                            @Value("${cloudinary.api_key}")String cloudKey,
                                            @Value("${cloudinary.api_secret}") String cloudSecret){
                                              
                                                HashMap<String, String> map = new HashMap<>();
                                                map.put("cloud_name", cloudName);
                                                map.put("api_key", cloudKey);
                                                map.put("api_secret", cloudSecret);

                                                return new Cloudinary(map);
                                            }
}
