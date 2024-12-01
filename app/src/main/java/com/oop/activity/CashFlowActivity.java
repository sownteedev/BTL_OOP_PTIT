package com.oop.activity;

import static com.oop.helper.Constants.STATEMENT_TYPE_DEBIT;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.oop.R;
import com.google.android.material.textfield.TextInputEditText;
import com.oop.MainActivity;
import com.oop.db.oop.CashFlowDatabase;
import com.oop.db.oop.CashItem;

import java.util.Calendar;

public class CashFlowActivity extends AppCompatActivity {

    private static final String TAG = "CashFlowActivity";
    String type = "";
    TextInputEditText amount, desc;
    TextView typeTextView;
    CashItem cashFlowItem;
    boolean isEdit = false;
    Button createOrUpdateBtn, deleteBtn;

    TextView timeTextView;
    DatePicker datePicker;

    Calendar calendar;

    public void createCashItem(View v) {

        if (amount.getText().toString().length() == 0) {
            amount.setError("Không thể để trống");
        } else if (desc.getText().toString().length() == 0) {
            desc.setError("Không thể để trống");
        } else {
            CashFlowDatabase database = Room.databaseBuilder(getApplicationContext(), CashFlowDatabase.class, "CashFlow").allowMainThreadQueries().build();
            double amountVal = Double.parseDouble(amount.getText().toString());
            if (type.equals(STATEMENT_TYPE_DEBIT)) {
                amountVal *= -1;
            }
            String descVal = desc.getText().toString();
            long selectedDateTime = calendar.getTimeInMillis();

            if (isEdit) {
                database.getCashFlowDao().updateItem(new CashItem(cashFlowItem.getId(), descVal, amountVal, cashFlowItem.getType(), selectedDateTime));
            } else {
                database.getCashFlowDao().addItem(new CashItem(0, descVal, amountVal, type, selectedDateTime));
            }

            onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_flow);

        createOrUpdateBtn = findViewById(R.id.create_or_update_btn);
        deleteBtn = findViewById(R.id.delete_btn);
        typeTextView = findViewById(R.id.actionbar_textView);
        type = getIntent().getExtras().getString("type");
        long cashFlowItemId = getIntent().getExtras().getLong("id");
        amount = findViewById(R.id.amount);
        desc = findViewById(R.id.desc);

        if (!TextUtils.isEmpty(type)) {
            isEdit = false;
            String tmp = type;
            if (type.equals("credit")) type = "thu";
            else type = "chi";
            typeTextView.setText("Thêm nguồn " + type);
            type = tmp;
            createOrUpdateBtn.setText("Thêm mới");
            deleteBtn.setVisibility(View.GONE);
        } else {
            isEdit = true;
            deleteBtn.setVisibility(View.VISIBLE);
            createOrUpdateBtn.setText("Cập nhật");
            CashFlowDatabase database = Room.databaseBuilder(CashFlowActivity.this, CashFlowDatabase.class, "CashFlow")
                    .allowMainThreadQueries().build();
            cashFlowItem = database.getCashFlowDao().getItemById(cashFlowItemId);
            amount.setText("" + Math.abs(cashFlowItem.getAmount()));
            type = cashFlowItem.getType();
            desc.setText(cashFlowItem.getDesc());
            String tmp = cashFlowItem.getType();
            if (tmp.equals("credit")) tmp = "thu";
            else tmp = "chi";
            typeTextView.setText("Sửa nguồn " + tmp);
        }

        // Initialize calendar
        calendar = Calendar.getInstance();

        timeTextView = findViewById(R.id.time);
        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(CashFlowActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                timeTextView.setText(String.format("%02d:%02d", hourOfDay, minute));
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        calendar = Calendar.getInstance();

        // Initialize date picker
        datePicker = findViewById(R.id.date);
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(CashFlowActivity.this)
                        .setTitle("Xóa " + cashFlowItem.getType())
                        .setMessage("Bạn có chắc chắn muốn xóa?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                CashFlowDatabase database = Room.databaseBuilder(CashFlowActivity.this, CashFlowDatabase.class, "CashFlow").allowMainThreadQueries().build();
                                database.getCashFlowDao().deleteItem(cashFlowItem);
                                onBackPressed();
                            }
                        }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CashFlowActivity.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}