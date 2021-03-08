package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("ReportService")
public class ReportService {
    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    public boolean sendReport(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email);
        if(user == (null)){
            return false;  
        }
        emailService.sendEmail("admin@expense.tracker.com", email, "Password Reset - Expense Tracker", "<h1>Welcome to Expense Tracker</h1><h3>Hello, "+ user.getFname() +" "+ user.getLname() +"</h3><p>Your report is attached to this mail</p>", file);
        return true;
        
    }
}