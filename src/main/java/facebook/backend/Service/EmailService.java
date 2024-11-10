package facebook.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOtp(String tomail, String otp){
        try {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(tomail);
        simpleMailMessage.setSubject("Your OTP is:");
        simpleMailMessage.setText("Otp: " + otp);
        
        javaMailSender.send(simpleMailMessage);
        System.out.println("OTP sent successfully to " + tomail);
    } catch (MailException e) {
        System.err.println("Failed to send OTP to " + tomail);
        e.printStackTrace();
    }
    }
}
