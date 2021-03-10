package com.example.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.model.Category;
import com.example.model.Expense;
import com.example.model.User;
import com.example.repository.CategoryRepository;
import com.example.repository.ExpenseRepository;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import com.example.util.ReadPDF;

import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

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

    @Autowired
    ExpenseRepository expenseRepository;
    String dir = "/home/swax/projects/Accolite Project/spring-boot/src/main/resources/static";

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

    @PostMapping("/extract")
    public ResponseEntity<?> extractExpense(@RequestParam("file") MultipartFile file) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        String filePath = dir + File.separator + user.getEmail() + File.separator+file.getOriginalFilename();
        SpreadsheetExtractionAlgorithm extractor = new SpreadsheetExtractionAlgorithm();
        try {
            File directory = new File(dir + File.separator + user.getEmail());
            if (! directory.exists()){
                directory.mkdir();
                // If you require it to make the entire directory path including parents,
                // use directory.mkdirs(); here instead.
            }
            Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            File savedfile = new File(filePath);
            PDDocument document = PDDocument.load(savedfile);
            ObjectExtractor oe = new ObjectExtractor(document);
            Page page = oe.extract(1);
            List<Table> table = extractor.extract(page);
            
            for(Table tables: table) {
                List<List<RectangularTextContainer>> rows = tables.getRows();
                for(int i = 1; i < rows.size(); i++) {
                    List<RectangularTextContainer> cells = rows.get(i);
                    if(cells.get(0).getText().replaceAll("\\s+","").length() == 0) break;
                    Expense expense = new Expense();
                    expense.setShopName(cells.get(0).getText().trim());
                    expense.setAmount(Integer.parseInt(cells.get(1).getText().replaceAll("\\s+","")));
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date date = sdf1.parse(cells.get(2).getText().replaceAll("\\s+",""));
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    expense.setDate(sqlDate);
                    Optional<Category> ocategory = categoryRepository.findById(Integer.parseInt(cells.get(3).getText().replaceAll("\\s+","")));
                    Category category = null;
                    if(ocategory.isPresent()){
                        category = ocategory.get();
                    }
                    expense.setCategory(category);
                    expense.setUser(user);
                    category.getExpenseList().add(expense);
                    user.getExpenseList().add(expense);
                    categoryRepository.save(category);
                    userRepository.save(user);
                    expenseRepository.save(expense);
                    // for(int j=0; j<cells.size(); j++) {
                    //     System.out.print(cells.get(j).getText().replaceAll("\\s+","")+"|");
                    // }
                    // System.out.print("|");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return new ResponseEntity<>("ok" ,HttpStatus.OK);
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
        Matcher amountMatcher = Pattern.compile("(?i)\\bTotal.*\\d+").matcher(pdfFileInText);
        Matcher shopMatcher = Pattern.compile("(?i)Shop name [a-z|0-9]+").matcher(pdfFileInText);
        Matcher categoryMatcher = Pattern.compile("(?i)Category [a-z|0-9]+").matcher(pdfFileInText);

        if (dateMatcher.find()) {
            expense.setDate(Date.valueOf(dateMatcher.group()));
        }

        if (amountMatcher.find()) {
            String[] amountArray = amountMatcher.group().split(" ");
            expense.setAmount(Integer.parseInt(amountArray[amountArray.length - 1]));
        }

        if (shopMatcher.find()) {
            String[] shopNameArray = shopMatcher.group().split(" ");
            expense.setShopName(shopNameArray[shopNameArray.length - 1]);
        }

        if (categoryMatcher.find()) {
            String[] categoryArray = categoryMatcher.group().split(" ");
            Category category = categoryRepository.findByCategoryName(categoryArray[categoryArray.length - 1]);
            expense.setCategory(category);
        }

        return new ResponseEntity<>(expense ,HttpStatus.OK);
      
    }
}
