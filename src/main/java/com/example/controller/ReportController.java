package com.example.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import org.apache.commons.codec.binary.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/report")
public class ReportController {

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;
    
    @PostMapping("/send") 
    public ResponseEntity<String> uploadReport(@RequestBody String base64) {
        if(base64.length() == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("file is empty");
        }
        byte[] decoded = Base64.decodeBase64(base64);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        emailService.sendEmail("admin@expense.tracker.com", userDetails.getUsername(), "Password Reset - Expense Tracker", "<h1>Welcome to Expense Tracker</h1><h3>Hello, "+ user.getFname() +" "+ user.getLname() +"</h3><p>Your report is attached to this mail</p>", decoded);
        return new ResponseEntity<>("Report Sent", HttpStatus.OK);
    }
}
