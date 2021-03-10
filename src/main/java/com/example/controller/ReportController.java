package com.example.controller;

import java.io.IOException;
import java.sql.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.model.Category;
import com.example.model.Expense;
import com.example.model.User;
import com.example.repository.CategoryRepository;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import com.example.util.ReadPDF;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping(path = "/report")
public class ReportController {

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @PostMapping("/send")
    public ResponseEntity<String> uploadReport(@RequestBody String base64) {
        if (base64.length() == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("file is empty");
        }
        byte[] decoded = Base64.decodeBase64(base64);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        emailService.sendEmail("admin@expense.tracker.com", userDetails.getUsername(),
                "Password Reset - Expense Tracker", "<h1>Welcome to Expense Tracker</h1><h3>Hello, " + user.getFname()
                        + " " + user.getLname() + "</h3><p>Your report is attached to this mail</p>",
                decoded);
        return new ResponseEntity<>("Report Sent", HttpStatus.OK);
    }

    @PostMapping("/readfrompdf")
    public ResponseEntity<Expense> readFromPDF(@RequestBody String base64) throws IOException {
        if (base64.length() == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        byte[] decoded = Base64.decodeBase64(base64);
        ReadPDF readPDF = new ReadPDF();
        String pdfFileInText = readPDF.readPDF(decoded);

        Expense expense = new Expense();

        Matcher dateMatcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher(pdfFileInText);
        Matcher amountMatcher = Pattern.compile("(?i)Total \\d+").matcher(pdfFileInText);
        Matcher shopMatcher = Pattern.compile("(?i)Shop name [a-z|0-9]+").matcher(pdfFileInText);
        Matcher categoryMatcher = Pattern.compile("(?i)Category [a-z|0-9]+").matcher(pdfFileInText);

        if (dateMatcher.find()) {
            expense.setDate(Date.valueOf(dateMatcher.group()));
        }

        if (amountMatcher.find()) {
            expense.setAmount(Integer.parseInt(amountMatcher.group().split(" ")[1]));
        }

        if (shopMatcher.find()) {
            expense.setShopName(shopMatcher.group().split(" ")[2]);
        }

        if (categoryMatcher.find()) {
            Category category = categoryRepository.findByCategoryName(categoryMatcher.group().split(" ")[1]);
            expense.setCategory(category);
        }

        return new ResponseEntity<>(expense, HttpStatus.OK);
    }
}
