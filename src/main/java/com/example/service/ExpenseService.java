package com.example.service;

import java.time.format.TextStyle;
import java.util.Locale;

import com.example.model.Expense;
import com.example.model.User;
import com.example.repository.ExpenseRepository;
import com.example.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ExpenseService")
public class ExpenseService {

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    public void checkTarget(User user, Expense expense) {
        int currentMonth = expenseRepository.getCurrentMonthExpenses(user.getId().toString(),expense.getDate());
        if(user.getTarget() != 0 && user.getTarget() < (double) currentMonth) {
            emailService.sendEmail("admin@expense.tracker.com", user.getEmail(), "Welcome to Expense Tracker",
            "<h1>Welcome to Expense Tracker</h1>"+
            "<h3>Hello, "+user.getFname()+" " +user.getLname()+ " </h3>" +
            "<h4>You have reached your expenditure target for the month "+
            expense.getDate().toLocalDate().getMonth().getDisplayName(TextStyle.FULL_STANDALONE,Locale.ENGLISH)+" "+ expense.getDate().toLocalDate().getYear()+"</h4>" +
            "<p>Total expense for the month "+
            expense.getDate().toLocalDate().getMonth().getDisplayName(TextStyle.FULL_STANDALONE,Locale.ENGLISH)+" "+ expense.getDate().toLocalDate().getYear()+" - <b>"+ currentMonth +
            "</b></p>" +
            "<p>Set target based on previous expenditures - <b>"+ user.getTarget() +"</b></p>" +
            "<p style='color:gray'>*Target is calculated based on your previous expenses</p>",null);
        }
    }

}
