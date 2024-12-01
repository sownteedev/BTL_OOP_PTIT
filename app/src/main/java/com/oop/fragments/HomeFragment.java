package com.oop.fragments;

import static com.oop.helper.Constants.STATEMENT_TYPE_CREDIT;
import static com.oop.helper.Constants.STATEMENT_TYPE_DEBIT;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_INDIVIDUAL;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_MONTHLY;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_WEEKLY;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_YEARLY;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.oop.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;
import com.oop.activity.CashFlowActivity;
import com.oop.adapter.NotificationWorker;
import com.oop.db.oop.CashFlowDatabase;
import com.oop.db.oop.CashItem;
import com.oop.helper.CashFlowHelper;
import com.oop.helper.Constants;
import com.oop.helper.CsvUtils;
import com.oop.helper.PrefHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragmentTAG";
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 1002;
    private static final int REQUEST_CODE_OPEN_FILE = 1;
    private ViewPager mainViewPager;
    private TextView headerTextView, balanceTextView;
    private TabLayout tabLayout;
    private CardView actionbar;
    private ImageButton statementFilterBtn, statementAddBtn, statementViewModeBtn, statementReminderBtn;
    private ImageButton reminderAddBtn;
    private LinearLayout statementBtnWrapper, reminderBtnWrapper;
    private StatementFragment statementFragment;
    private long filterStart = 0, filterEnd = 0;

    private MaterialDatePicker dateRangePicker;

    private Uri selectedFileUri;
    private boolean exportButtonClicked = false;
    private boolean importButtonClicked = false;

    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        CashFlowHelper.database = Room.databaseBuilder(requireContext(), CashFlowDatabase.class, "CashFlow").allowMainThreadQueries().build();
        statementFragment = new StatementFragment();

        statementBtnWrapper = view.findViewById(R.id.actionbar_statement_btn_wrapper);
        reminderBtnWrapper = view.findViewById(R.id.actionbar_reminder_btn_wrapper);
        statementFilterBtn = view.findViewById(R.id.statement_filter_btn);
        statementAddBtn = view.findViewById(R.id.statement_add_btn);
        statementViewModeBtn = view.findViewById(R.id.statement_view_btn);
        statementReminderBtn = view.findViewById(R.id.statement_reminder_btn); // Ensure this ID is correct

        reminderAddBtn = view.findViewById(R.id.reminder_add_btn);

        actionbar = view.findViewById(R.id.actionbar);
        tabLayout = view.findViewById(R.id.tab_layout);
        headerTextView = view.findViewById(R.id.actionbar_textView);
        balanceTextView = view.findViewById(R.id.actionbar_balance_textView);
        mainViewPager = view.findViewById(R.id.main_view_pager);
        mainViewPager.setAdapter(new MainFragmentAdapter(getChildFragmentManager(), 0));
        tabLayout.setupWithViewPager(mainViewPager);
        tabLayout.setVisibility(View.GONE);

        setBalanceAmount(0, 0);

        dateRangePicker = MaterialDatePicker.Builder.dateRangePicker().build();
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            statementFragment.isDateRangePicked = true;
            String[] ranges = selection.toString().replace("Pair{", "").replace("}", "").trim().split(" ");
            long offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
            filterStart = Long.parseLong(ranges[0]) - offset;
            filterEnd = Long.parseLong(ranges[1]) - offset;
            setBalanceAmount(filterStart, filterEnd + (24 * 60 * 60 * 1000) - 1000);
            statementFragment.loadStatement(filterStart, filterEnd);
        });

        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    statementBtnWrapper.setVisibility(View.VISIBLE);
                    reminderBtnWrapper.setVisibility(View.GONE);
                } else {
                    statementBtnWrapper.setVisibility(View.GONE);
                    reminderBtnWrapper.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        statementAddBtn.setOnClickListener(view1 -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), statementAddBtn);
            Animation scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
            view1.startAnimation(scaleAnimation);
            popupMenu.getMenuInflater().inflate(R.menu.statement_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.export_btn){
                    exportButtonClicked = true;
                    importButtonClicked = false;
                    openDirectoryPicker();
                } else if (menuItem.getItemId() == R.id.import_btn) {
                    exportButtonClicked = false;
                    importButtonClicked = true;
                    openFilePicker();
                } else if (menuItem.getItemId() == R.id.income) {
                    startActivity(new Intent(requireContext(), CashFlowActivity.class).putExtra("type", STATEMENT_TYPE_CREDIT));
                } else {
                    startActivity(new Intent(requireContext(), CashFlowActivity.class).putExtra("type", STATEMENT_TYPE_DEBIT));
                }
                return true;
            });
            Context wrapper = new ContextThemeWrapper(getContext( ), R.style.PopupMenu);
            @SuppressLint("RestrictedApi") MenuPopupHelper menuPopupHelper = new MenuPopupHelper(wrapper, (MenuBuilder) popupMenu.getMenu(), statementAddBtn );
            menuPopupHelper.setForceShowIcon(true);

            menuPopupHelper.show();
        });

        statementFilterBtn.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
            v.startAnimation(scaleAnimation);
            if (!dateRangePicker.isVisible()) {
                dateRangePicker.show(getParentFragmentManager(), dateRangePicker.getTag());
            }
        });

        statementViewModeBtn.setOnClickListener(view12 -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
            view12.startAnimation(scaleAnimation);
            PopupMenu popupMenu = new PopupMenu(requireContext(), statementViewModeBtn);
            popupMenu.getMenuInflater().inflate(R.menu.statement_view_mode, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.view_mode_monthly)
                    new PrefHelper(requireContext()).setPreference(Constants.CURRENT_VIEW_MODE, STATEMENT_VIEW_MODE_MONTHLY);
                else if (itemId == R.id.view_mode_yearly) {
                    new PrefHelper(requireContext()).setPreference(Constants.CURRENT_VIEW_MODE, STATEMENT_VIEW_MODE_YEARLY);
                } else if (itemId == R.id.view_mode_weekly) {
                    new PrefHelper(requireContext()).setPreference(Constants.CURRENT_VIEW_MODE, STATEMENT_VIEW_MODE_WEEKLY);
                } else {
                    new PrefHelper(requireContext()).setPreference(Constants.CURRENT_VIEW_MODE, STATEMENT_VIEW_MODE_INDIVIDUAL);
                }
                statementFragment.loadStatement(filterStart, filterEnd);
                return true;
            });

            popupMenu.show();
        });

        statementReminderBtn.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
            v.startAnimation(scaleAnimation);
            showTimePickerDialog();
        });

        return view;
    }

    private void exportCsv() {
        if (selectedFileUri == null) {
            Toast.makeText(requireContext(), "No directory selected", Toast.LENGTH_LONG).show();
            return;
        }

        ContentResolver contentResolver = requireContext().getContentResolver();
        DocumentFile pickedDir = DocumentFile.fromTreeUri(requireContext(), selectedFileUri);
        if (pickedDir == null) {
            Toast.makeText(requireContext(), "Failed to open directory", Toast.LENGTH_LONG).show();
            return;
        }

        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = "OOP_" + date + ".csv";
        DocumentFile newFile = pickedDir.createFile("text/csv", fileName);

        try (OutputStream outputStream = contentResolver.openOutputStream(newFile.getUri())) {
            if (outputStream == null) {
                Toast.makeText(requireContext(), "Failed to open output stream", Toast.LENGTH_LONG).show();
                return;
            }

            List<CashItem> cashItems = CashFlowHelper.database.getCashFlowDao().getAllItems();
            CsvUtils.exportToCsv(outputStream, cashItems);
            Toast.makeText(requireContext(), "Exported to " + newFile.getUri().getPath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_LONG).show();
        }
    }

    private void importCsv() {
        if (selectedFileUri == null) {
            Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_LONG).show();
            return;
        }

        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedFileUri)) {
            if (inputStream == null) {
                Toast.makeText(requireContext(), "Failed to open input stream", Toast.LENGTH_LONG).show();
                return;
            }

            // Read and parse the CSV file
            List<CashItem> cashItems = CsvUtils.importFromCsv(inputStream);

            // Import the parsed data into the database
            for (CashItem item : cashItems) {
                if (!isItemExist(item)) {
                    CashFlowHelper.database.getCashFlowDao().addItem(item);
                }
            }
            Toast.makeText(requireContext(), "Imported successfully", Toast.LENGTH_LONG).show();
            refreshData();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Import failed", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshData() {
        List<CashItem> updatedCashItems = CashFlowHelper.database.getCashFlowDao().getAllItems();
        statementFragment.updateData(updatedCashItems);
    }

    private boolean isItemExist(CashItem item) {
        CashItem existingItem = CashFlowHelper.database.getCashFlowDao().getItemById(item.getId());
        return existingItem != null;
    }

    private void openDirectoryPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                if (requestCode == REQUEST_CODE_OPEN_FILE && importButtonClicked) {
                    importCsv();
                    importButtonClicked = false;
                } else if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && exportButtonClicked) {
                    exportCsv();
                    exportButtonClicked = false;
                }
            }
        }
    }

    private void showTimePickerDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_time_picker, null);

        TextView currentTimeText = dialogView.findViewById(R.id.current_time_text);
        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        int savedHour = sharedPreferences.getInt("hourOfDay", 21); // Default to 9 PM if not set
        int savedMinute = sharedPreferences.getInt("minute", 0);

        currentTimeText.setText(String.format("Thời gian thông báo hiện tại: %02d:%02d", savedHour, savedMinute));

        timePicker.setHour(savedHour);
        timePicker.setMinute(savedMinute);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setPositiveButton("Set", (dialog, which) -> {
                    int hourOfDay = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    scheduleUserDefinedNotification(hourOfDay, minute);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();

        applyMotionEffect(dialogView);
    }

    private void applyMotionEffect(View dialogView) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(dialogView, "alpha", 0f, 1f);
        fadeIn.setDuration(500);

        ObjectAnimator floatUp = ObjectAnimator.ofFloat(dialogView, "translationY", 200f, 0f);
        floatUp.setDuration(500);

        fadeIn.start();
        floatUp.start();
    }

    private void scheduleUserDefinedNotification(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long filterStart = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long filterEnd = calendar.getTimeInMillis();

        double dailyIncome = statementFragment.cashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_CREDIT, filterStart, filterEnd);
        double dailyExpense = statementFragment.cashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_DEBIT, filterStart, filterEnd);

        double totalIncome = statementFragment.cashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_CREDIT, 0, System.currentTimeMillis());
        double totalExpense = statementFragment.cashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_DEBIT, 0, System.currentTimeMillis());
        double balance = getSubtractedValue(totalIncome, totalExpense);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("hourOfDay", hourOfDay);
        editor.putInt("minute", minute);
        editor.apply();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        Data inputData = new Data.Builder()
                .putDouble("dailyIncome", dailyIncome)
                .putDouble("dailyExpense", dailyExpense)
                .putDouble("balance", balance)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(requireContext()).enqueue(workRequest);
    }

    private void setBalanceAmount(long start, long end) {
        double income = statementFragment.cashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_CREDIT, start, end);
        double expense = statementFragment.cashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_DEBIT, start, end);
        double diff = getSubtractedValue(income, expense);
        if (diff > 0) {
            headerTextView.setText(Math.abs(diff) + " ₫");
            headerTextView.setTextColor(Color.parseColor("#3fb950"));
            balanceTextView.setTextColor(Color.parseColor("#3fb950"));
        } else if (diff < 0) {
            headerTextView.setText(Math.abs(diff) + " ₫");
            headerTextView.setTextColor(Color.parseColor("#da3633"));
            balanceTextView.setTextColor(Color.parseColor("#da3633"));
        } else {
            headerTextView.setText(Math.abs(diff) + " ₫");
        }
    }

    private double getSubtractedValue(double income, double expense) {
        BigDecimal difference = BigDecimal.valueOf(income).subtract(BigDecimal.valueOf(expense));
        return difference.doubleValue();
    }

    private class MainFragmentAdapter extends FragmentPagerAdapter {

        public MainFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = statementFragment;
            applyMotionEffect(fragment.getView());
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Statement";
        }

        @Override
        public int getCount() {
            return 1;
        }

        private void applyMotionEffect(View view) {
            if (view != null) {
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                fadeIn.setDuration(500); // Thời gian fade-in
                fadeIn.start();

                ObjectAnimator slideIn = ObjectAnimator.ofFloat(view, "translationY", 200f, 0f);
                slideIn.setDuration(500); // Thời gian slide-in
                slideIn.start();
            }
        }
    }

}