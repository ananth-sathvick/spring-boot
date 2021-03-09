package com.example.controller;

import com.example.service.ReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(path = "/report")
public class ReportController {
    @Autowired
    ReportService reportService;
    
    @PostMapping("/send") // send 
    public ResponseEntity<String> uploadReport(@RequestParam("file") MultipartFile file) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("file is empty");
        }
        reportService.sendReport(userDetails.getUsername(), file);
        System.out.println("file recieved: " + file.getOriginalFilename());
        return new ResponseEntity<>("Report Sent", HttpStatus.OK);
    }
}
