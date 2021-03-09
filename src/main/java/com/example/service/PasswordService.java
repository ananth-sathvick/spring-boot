package com.example.service;

import java.util.Random;

import com.example.model.ChangePassword;
import com.example.model.User;
import com.example.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service("PasswordService")
public class PasswordService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    EmailService emailService;


    public boolean changePassword(ChangePassword changePassword) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByEmail(username);
        if (bcryptEncoder.matches(changePassword.getCurrentPassword(), user.getPassword())) {
            user.setPassword(bcryptEncoder.encode(changePassword.getNewPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean forgotPassword(String email) {
        User user = userRepository.findByEmail(email);
        if(user == (null))
        {
            return false;
            
        }
        String newPassword = GenerateRandomPassword(7);
        user.setPassword(bcryptEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendEmail("admin@expense.tracker.com", email, "Password Reset - Expense Tracker", "<h1>Welcome to Expense Tracker</h1><h3>Hello, "+ user.getFname() +" "+ user.getLname() +"</h3><p>Your Password has been changed successfully</p>"+
        "<p>"+
        "username :"+ user.getEmail() + "</p><p>" +
        "password :"+ newPassword +
        "</p>", null);
        return true;
        
    }

    public String GenerateRandomPassword(int len) {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

}
