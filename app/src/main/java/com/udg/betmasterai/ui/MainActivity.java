package com.udg.betmasterai.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.udg.betmasterai.R;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    
    private MatchesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MatchesViewModel.class);
        
        RecyclerView rvMatches = findViewById(R.id.rvMatches);
        MatchesAdapter adapter = new MatchesAdapter();
        rvMatches.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvMatches.setAdapter(adapter);

        // Aquí conectamos el observer al ViewModel para actualizar el UI
        viewModel.getMatches().observe(this, matches -> {
            adapter.setMatches(matches);
        });
    }
}
