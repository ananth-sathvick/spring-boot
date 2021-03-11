package com.example.scheduler;

import java.math.BigInteger;
import java.util.ArrayList;

import com.example.model.User;
import com.example.repository.ExpenseRepository;
import com.example.repository.UserRepository;
import com.example.util.LinearRegression;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TargetTask {
    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    UserRepository userRepository;
    
    // Scheduled task to update the target for each user at start of each month
    @Scheduled(cron = "0 1 0 1 * ?")
    public void updateTarget() {
        //Sets target for each user using linear regression
        ArrayList<User> users = (ArrayList<User>)userRepository.findAll();
        for(int i = 0; i < users.size(); i++) {
            ArrayList<BigInteger> expenses = expenseRepository.getExpenseAvgByMonth(users.get(i).getId().toString());
            int size = expenses.size();
            double target = 0.0;
            if (size >= 5) {
                //users should have data for atleast 5 months
                double[] x = new double[size], y = new double[size];
                for (int j = 0; j < size; j++) {
                    x[j] = (double) j;
                    y[j] = expenses.get(j).doubleValue();
                }
                LinearRegression linearRegression = new LinearRegression(x, y);
                target = linearRegression.predict(size);
            }
            System.out.println(target);
            users.get(i).setTarget((int)target);
            userRepository.save(users.get(i));
            System.out.println("this is scheduled job");
        }
    }
}
