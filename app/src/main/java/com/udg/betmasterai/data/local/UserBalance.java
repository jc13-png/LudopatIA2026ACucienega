package com.udg.betmasterai.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_balance")
public class UserBalance {
    @PrimaryKey
    private int id = 1; // Only one row for the user's balance
    
    private double currentBalance;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }
}
