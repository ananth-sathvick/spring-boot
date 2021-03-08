package com.example.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import com.example.model.Expense;
import com.example.model.User;
import com.example.repository.ExpenseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ExpenseService")
public class ExpenseService {

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    EmailService emailService;


    public void checkTarget(User user, Expense expense) {
        Integer currentMonthExpense = (expenseRepository.getCurrentMonthExpense(user.getId().toString(),expense.getDate()));
        // Double previousMonthAverage = 
       Map<String,Object> avg_count = (expenseRepository.getPreviousMonthAverageExpense(user.getId().toString(),expense.getDate()));

        BigDecimal previousMonthAverage = (BigDecimal)avg_count.get("avg");
        BigInteger count = (BigInteger)avg_count.get("count");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expense.getDate());


        if(((1.25 *  previousMonthAverage.doubleValue()) < currentMonthExpense) && (count.intValue() > 3))
        {
            emailService.sendEmail("admin@expense.tracker.com", user.getEmail(), "Welcome to Expense Tracker",
            "<h1>Welcome to Expense Tracker</h1>"+
            "<h3>Hello, "+user.getFname()+" " +user.getLname()+ " </h3>" +
            "<h4>You have reached your expenditure target for the month "+ expense.getDate().toLocalDate().getMonth().getDisplayName(TextStyle.FULL_STANDALONE,Locale.ENGLISH)+" "+ expense.getDate().toLocalDate().getYear()+"</h4>" +
            "<p>Total expense for the month "+ expense.getDate().toLocalDate().getMonth().getDisplayName(TextStyle.FULL_STANDALONE,Locale.ENGLISH)+" "+ expense.getDate().toLocalDate().getYear()+" - <b>"+ currentMonthExpense +"</b></p>" +
            "<p>Set target based on previous expenditures - <b>"+ (1.25 *  previousMonthAverage.doubleValue())  +"</b></p>" +
            "<p style='color:gray'>*Target is calculated as 125% of average of previous months expenditure</p>");
        }

        
    }
    
}
