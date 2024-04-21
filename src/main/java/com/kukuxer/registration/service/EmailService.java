package com.kukuxer.registration.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Value("${spring.mail.username}")
    private String fromMail;


    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendMailRecoverPasswordTo(String to, String url, String name) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(fromMail);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setCc("nazardaun5551@gmail.com");
            mimeMessageHelper.setSubject("Your Recovery password link");
            mimeMessageHelper.setText(
                    "<h1> hi " + name + ", this is your recovery password link<h1>" +
                            "<a href=\"" + url + "\" " +
                            "style=\"display: inline-block; padding: 10px 20px; " +
                            "background-color: #007bff; color: #fff; " +
                            "text-decoration: none; border-radius: 5px;\">Change password</a>"
                    , true);
            System.out.println(url);

            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
