package com.example.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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

    public void sendEmail(String fromEmailId,String toEmailId,String subject,String body, byte[] file) {
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
                helper.addAttachment("Report.pdf", new ByteArrayDataSource(file, "application/octet-stream"));
            } catch (Exception e) {
                System.out.println(e);
            }
                
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        mailSender.send(message);
    }

}
