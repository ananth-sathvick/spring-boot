package com.example.service;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("EmailService")
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public JavaMailSender getMailSender() {
        return this.mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String fromEmailId,String toEmailId,String subject,String body, MultipartFile file) {
        String from = fromEmailId;
        String to = toEmailId;
        MimeMessageHelper helper = null;
        MimeMessage message = mailSender.createMimeMessage();
        try {
            helper = new MimeMessageHelper(message, true);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        try {
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setTo(to);
            boolean html = true;
            helper.setText(body, html);
            if(file != null)
            try {
                helper.addAttachment("Report.pdf", file);
            } catch (Exception e) {
                System.out.println(e);
            }
                
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        mailSender.send(message);
    }

}
