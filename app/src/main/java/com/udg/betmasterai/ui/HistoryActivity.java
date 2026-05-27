package com.udg.betmasterai.ui;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.udg.betmasterai.R;

public class HistoryActivity extends AppCompatActivity {

    private MatchesViewModel viewModel;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        adapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MatchesViewModel.class);
        viewModel.getAllBets().observe(this, bets -> {
            if (bets != null) {
                adapter.setBets(bets);
            }
        });
    }
}
