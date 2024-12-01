package com.oop.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.oop.R;
import com.google.android.material.textfield.TextInputEditText;
import com.oop.db.oop.Plan;
import com.oop.db.oop.PlanDatabase;

import java.util.Calendar;

public class CreatePlan extends AppCompatActivity {

    TextInputEditText amount, description;
    DatePicker datePicker;
    Button createOrUpdateBtn;
    Plan plan;
    boolean isEdit = false;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_plan);

        amount = findViewById(R.id.plan_amount);
        description = findViewById(R.id.plan_description);
        datePicker = findViewById(R.id.plan_date);
        createOrUpdateBtn = findViewById(R.id.start_plan_button);

        calendar = Calendar.getInstance();

        int planId = getIntent().getIntExtra("id", -1);
        if (planId != -1) {
            isEdit = true;
            PlanDatabase database = Room.databaseBuilder(getApplicationContext(), PlanDatabase.class, "PlanDB").allowMainThreadQueries().build();
            plan = database.getPlanDao().getPlanById(planId);
            amount.setText(String.valueOf(plan.getAmount()));
            description.setText(plan.getDescription());
            calendar.setTimeInMillis(plan.getDate());
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            createOrUpdateBtn.setText("Cập nhật kế hoạch");
        } else {
            plan = new Plan();
            createOrUpdateBtn.setText("Lên kế hoạch");
        }

        createOrUpdateBtn.setOnClickListener(v -> createOrUpdatePlan());
    }

    private void createOrUpdatePlan() {
        if (TextUtils.isEmpty(amount.getText())) {
            amount.setError("Cannot be empty");
        } else if (TextUtils.isEmpty(description.getText())) {
            description.setError("Cannot be empty");
        } else {
            try {
                double amountValue = Double.parseDouble(amount.getText().toString());
                PlanDatabase database = Room.databaseBuilder(getApplicationContext(), PlanDatabase.class, "PlanDB").allowMainThreadQueries().build();
                plan.setAmount(amountValue);
                plan.setDescription(description.getText().toString());
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                plan.setDate(calendar.getTimeInMillis());

                if (isEdit) {
                    database.getPlanDao().updatePlan(plan);
                } else {
                    database.getPlanDao().addPlan(plan);
                }

                finish();
            } catch (NumberFormatException e) {
                amount.setError("Must be a valid number");
            }
        }
    }

    private void deletePlan() {
        PlanDatabase database = Room.databaseBuilder(getApplicationContext(), PlanDatabase.class, "PlanDB").allowMainThreadQueries().build();
        database.getPlanDao().deletePlan(plan);
        finish();
    }
}