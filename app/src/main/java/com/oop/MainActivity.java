package com.oop;

import static com.oop.helper.Constants.STATEMENT_TYPE_CREDIT;
import static com.oop.helper.Constants.STATEMENT_TYPE_DEBIT;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.oop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;
import com.oop.adapter.BackgroundService;
import com.oop.adapter.NotificationWorker;
import com.oop.db.oop.CashFlowDatabase;
import com.oop.fragments.ChartFragment;
import com.oop.fragments.HomeFragment;
import com.oop.fragments.PlanFragment;
import com.oop.fragments.StatementFragment;
import com.oop.helper.CashFlowHelper;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityTAG";
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 1002;
    private static final int REQUEST_CODE_OPEN_FILE = 1;
    private static final int REQUEST_CODE_NOTIFICATION_LISTENER = 1003;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };
    private Uri selectedFileUri;

    ViewPager mainViewPager;
    TextView headerTextView, balanceTextView;
    TabLayout tabLayout;
    CardView actionbar;
    ImageButton statementFilterBtn, statementAddBtn, statementViewModeBtn;
    ImageButton reminderAddBtn;
    LinearLayout statementBtnWrapper, reminderBtnWrapper;
    StatementFragment statementFragment;
    ImageButton statementViewBtn2;
    long filterStart = 0, filterEnd = 0;
    private boolean exportButtonClicked = false;
    private boolean importButtonClicked = false;

    MaterialDatePicker dateRangePicker;

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!allPermissionsGranted()) {
            requestPermissionsBasedOnApiLevel();
        } else {
            initializeApp();
        }

        if (!isNotificationServiceEnabled()) {
            requestNotificationAccess();
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat != null) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (pkgName.equals(cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void requestNotificationAccess() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_LISTENER);
        Toast.makeText(this, "Please enable notification access for this app", Toast.LENGTH_LONG).show();
    }

    private void requestPermissionsBasedOnApiLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
            }, REQUEST_CODE_PERMISSIONS);
        } else { // Android 12 and below
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void initializeApp() {
        statementFragment = new StatementFragment(); // Initialize statementFragment

        Intent serviceIntent = new Intent(this, BackgroundService.class);
        startService(serviceIntent);

        CashFlowHelper.database = Room.databaseBuilder(MainActivity.this, CashFlowDatabase.class, "CashFlow").allowMainThreadQueries().build();

        bottomNavigationView = findViewById(R.id.bottomNavView);
        frameLayout = findViewById(R.id.frameLayout);

        loadFragment(new HomeFragment(), true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navHome) {
                    loadFragment(new HomeFragment(), false);
                } else if (itemId == R.id.navChart) {
                    loadFragment(new ChartFragment(), false);
                } else { // nav Plan
                    loadFragment(new PlanFragment(), false);
                }
                return true;
            }
        });

        scheduleDailyCheckNotification();
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(isAppInitialized) {
            fragmentTransaction.add(R.id.frameLayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("appClosedOnce", true);
        editor.apply();
    }

    private void setBalanceAmount(long start, long end) {
        double income = CashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_CREDIT, start, end);
        double expense = CashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_DEBIT, start, end);
        double diff = getSubtractedValue(income, expense);
        if (diff > 0) {
            headerTextView.setText("+ " +Math.abs(diff) + " ₫");
            headerTextView.setTextColor(Color.parseColor("#3fb950"));
            balanceTextView.setTextColor(Color.parseColor("#3fb950"));
        } else if (diff < 0) {
            headerTextView.setText("- " + Math.abs(diff) + " ₫");
            headerTextView.setTextColor(Color.parseColor("#da3633"));
            balanceTextView.setTextColor(Color.parseColor("#da3633"));
        } else {
            headerTextView.setText(Math.abs(diff) + " ₫");
        }
    }

    private double getSubtractedValue(double income, double expense) {
        String incomeStr = String.valueOf(income);
        String expenseStr = String.valueOf(expense);
        BigDecimal difference = new BigDecimal(incomeStr).subtract(new BigDecimal(expenseStr));
        return difference.doubleValue();
    }

    @Override
    public void onBackPressed() {
        if (mainViewPager.getCurrentItem() != 0) {
            mainViewPager.setCurrentItem(0);
        } else if (statementFragment.isDateRangePicked) {
            statementFragment.dateRangeView.setVisibility(View.GONE);
            statementFragment.isDateRangePicked = false;
            filterStart = 0;
            filterEnd = 0;
            setBalanceAmount(filterStart, filterEnd);
            statementFragment.loadStatement(filterStart, filterEnd);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private class MainFragmentAdapter extends FragmentPagerAdapter {

        public MainFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return statementFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Statement";
        }

        @Override
        public int getCount() {
            return 1;
        }
    }

    private void scheduleDailyCheckNotification() {
        SharedPreferences sharedPreferences = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
        boolean appClosedOnce = sharedPreferences.getBoolean("appClosedOnce", false);

        if (!appClosedOnce) {
            return; // Do not schedule the notification if the app hasn't been closed once
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 21); // Set to 9 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
        if (delay < 0) {
            delay += TimeUnit.DAYS.toMillis(1); // Schedule for the next day if the time has already passed
        }

        double dailyIncome = statementFragment.cashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_CREDIT, getStartOfDay(), getEndOfDay());
        double dailyExpense = statementFragment.cashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_DEBIT, getStartOfDay(), getEndOfDay());
        double balance = getSubtractedValue(dailyIncome, dailyExpense);

        Data inputData = new Data.Builder()
                .putDouble("dailyIncome", dailyIncome)
                .putDouble("dailyExpense", dailyExpense)
                .putDouble("balance", balance)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    private boolean allPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else { // Android 12 and below0
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                initializeApp();
            } else {
                showPermissionsDeniedDialog();
            }
        }
    }

    private void showPermissionsDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("This app requires storage permissions to function properly. Please grant the required permissions.")
                .setPositiveButton("Retry", (dialog, which) -> requestPermissionsBasedOnApiLevel())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}