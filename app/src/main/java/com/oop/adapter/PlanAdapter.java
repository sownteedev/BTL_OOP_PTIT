package com.oop.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.room.Room;

import com.example.oop.R;
import com.oop.activity.CreatePlan;
import com.oop.db.oop.Plan;
import com.oop.db.oop.PlanDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PlanAdapter extends BaseAdapter {

    private Context context;
    private List<Plan> plans;
    private PlanDatabase database;

    public PlanAdapter(Context context, List<Plan> plans) {
        this.context = context;
        this.plans = plans;
        this.database = Room.databaseBuilder(context.getApplicationContext(), PlanDatabase.class, "PlanDB").allowMainThreadQueries().build();
    }

    @Override
    public int getCount() {
        return plans.size();
    }

    @Override
    public Object getItem(int position) {
        return plans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return plans.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.plan_item, parent, false);
        }

        Plan plan = plans.get(position);

        TextView amount = convertView.findViewById(R.id.plan_amount);
        TextView description = convertView.findViewById(R.id.plan_description);
        TextView date = convertView.findViewById(R.id.plan_date);
        LinearLayout planItemLayout = convertView.findViewById(R.id.plan_item_layout);
        ImageButton okButton = convertView.findViewById(R.id.ok_plan);
        ImageButton deleteButton = convertView.findViewById(R.id.delete_plan);

        amount.setText(String.valueOf(plan.getAmount()) + " ₫");
        description.setText(plan.getDescription());
        date.setText(getDateAndTime(plan.getDate()));

        // Set background color and visibility of the tick button based on isOk state
        if (plan.isOk()) {
            planItemLayout.setBackgroundColor(Color.parseColor("#AA61E273"));
            okButton.setVisibility(View.GONE);
        } else {
            planItemLayout.setBackgroundColor(Color.parseColor("#AAEB5769"));
            okButton.setVisibility(View.VISIBLE);
        }

        okButton.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.scale);
            v.startAnimation(scaleAnimation);
            plan.setOk(true);
            database.getPlanDao().updatePlan(plan);
            notifyDataSetChanged();
        });

        deleteButton.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.scale);
            v.startAnimation(scaleAnimation);
            new AlertDialog.Builder(context)
                    .setTitle("Xóa kế hoạch")
                    .setMessage("Bạn có chắc chắn muốn xóa?")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            database.getPlanDao().deletePlan(plan);
                            plans.remove(position);
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });

        convertView.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.scale);
            v.startAnimation(scaleAnimation);
            Intent intent = new Intent(context, CreatePlan.class);
            intent.putExtra("id", plan.getId());
            context.startActivity(intent);
        });

        return convertView;
    }

    private String getDateAndTime(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(date);
    }
}