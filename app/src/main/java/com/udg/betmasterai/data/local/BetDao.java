package com.udg.betmasterai.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BetDao {
    @Insert
    void insertBet(BetHistory bet);

    @Query("UPDATE bet_history SET result = :result WHERE id = :id")
    void updateBetResult(int id, String result);

    @Query("SELECT * FROM bet_history ORDER BY timestamp DESC")
    LiveData<List<BetHistory>> getAllBets();

    @Query("SELECT * FROM user_balance WHERE id = 1")
    LiveData<UserBalance> getUserBalance();

    @Insert
    void insertBalance(UserBalance balance);

    @Update
    void updateBalance(UserBalance balance);
}
