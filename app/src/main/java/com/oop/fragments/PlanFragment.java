package com.oop.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.oop.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.oop.activity.CreatePlan;
import com.oop.adapter.PlanAdapter;
import com.oop.db.oop.Plan;
import com.oop.db.oop.PlanDatabase;

import java.util.List;

public class PlanFragment extends Fragment {

    private PlanDatabase database;
    private ListView plansListView;
    private PlanAdapter planAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_plan, container, false);

        database = Room.databaseBuilder(requireContext(), PlanDatabase.class, "PlanDB")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        plansListView = rootView.findViewById(R.id.plans_listview);
        FloatingActionButton fab = rootView.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreatePlan.class));
            }
        });

        loadPlans();
        return rootView;
    }

    private void loadPlans() {
        List<Plan> plans = database.getPlanDao().getAllPlans();
        planAdapter = new PlanAdapter(requireContext(), plans);
        plansListView.setAdapter(planAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlans();
    }
}
