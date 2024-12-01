package com.oop.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.example.oop.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.oop.db.oop.CashItem;
import com.oop.helper.CalendarHelper;
import com.oop.helper.CashFlowHelper;

import java.util.ArrayList;
import java.util.List;

public class ChartFragment extends Fragment {

    private static ChartDataType currentChartDataType = ChartDataType.week;
    private TextView compareTextView;
    private TextView compareTextView2;
    private BarChart barChart;
    private BarChart barChart2;
    private Button weekBtn;
    private Button monthBtn;
    private LinearLayout describeIncomeList;
    private LinearLayout describeIncomeList2;
    private ToggleButton describeIncomeBtn;
    private ToggleButton describeIncomeBtn2;
    private LinearLayout elementList;
    private RadioGroup toggleGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_tab_chart, container, false);

        describeIncomeList = view.findViewById(R.id.described_income);
        describeIncomeList2 = view.findViewById(R.id.described_income2);
        describeIncomeBtn = view.findViewById(R.id.described_income_btn);
        describeIncomeBtn2 = view.findViewById(R.id.described_income_btn2);
        elementList = view.findViewById(R.id.linearLayoutGroup);
        describeIncomeBtn.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                hideViewWithMotion(describeIncomeList);
            } else {
                showViewWithMotion(describeIncomeList);
            }
        });

        describeIncomeBtn2.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                hideViewWithMotion(describeIncomeList2);
            } else {
                showViewWithMotion(describeIncomeList2);
            }
        });

        compareTextView = view.findViewById(R.id.tab_chart_compare);
        compareTextView2 = view.findViewById(R.id.tab_chart_compare2);
        barChart = view.findViewById(R.id.barChart);
        barChart2 = view.findViewById(R.id.barChart2);
//        weekBtn = view.findViewById(R.id.week_btn);
//        weekBtn.setOnClickListener(v -> {
//            Animation scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
//            v.startAnimation(scaleAnimation);
//            currentChartDataType = ChartDataType.week;
//            ChooseWeek();
//        });
//
//        monthBtn = view.findViewById(R.id.month_btn);
//        monthBtn.setOnClickListener(v -> {
//            Animation scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
//            v.startAnimation(scaleAnimation);
//            currentChartDataType = ChartDataType.month;
//            ChooseMonth();
//        });
        toggleGroup = view.findViewById(R.id.toggleGroup);

        toggleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.button1) {
                    ShowWeek();
                } else if (checkedId == R.id.button2) {
                    ShowMonth();
                }
            }
        });
//        ShowWeek();
        barChart.invalidate();
        barChart2.invalidate();

        return view;
    }



    private void showViewWithMotion(View view) {
        view.setVisibility(View.VISIBLE);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator floatUp = ObjectAnimator.ofFloat(view, "translationY", 200f, 0f);
        fadeIn.setDuration(500);
        floatUp.setDuration(500);

        fadeIn.start();
        floatUp.start();
    }

    private void hideViewWithMotion(View view) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        ObjectAnimator floatDown = ObjectAnimator.ofFloat(view, "translationY", 0f, 200f);
        fadeOut.setDuration(500);
        floatDown.setDuration(500);

        fadeOut.start();
        floatDown.start();

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
    }


    public void ShowWeek() {
        Pair<Long, Long> range_1 = CalendarHelper.getWeekRangeInMillis(0);
        Pair<Long, Long> range_2 = CalendarHelper.getWeekRangeInMillis(1);
        Pair<Long, Long> range_3 = CalendarHelper.getWeekRangeInMillis(2);
        Pair<Long, Long> range_4 = CalendarHelper.getWeekRangeInMillis(3);

        InitElementDescription("credit", range_1.first, range_1.second);
        InitElementDescription("debit", range_1.first, range_1.second);

        double amount_credit_1 = getAmountForRange(range_1, true);
        double amount_credit_2 = getAmountForRange(range_2, true);
        double amount_credit_3 = getAmountForRange(range_3, true);
        double amount_credit_4 = getAmountForRange(range_4, true);

        double amount_debit_1 = getAmountForRange(range_1, false);
        double amount_debit_2 = getAmountForRange(range_2, false);
        double amount_debit_3 = getAmountForRange(range_3, false);
        double amount_debit_4 = getAmountForRange(range_4, false);

        amount_credit_1 = roundToTwoDecimal(amount_credit_1);
        amount_credit_2 = roundToTwoDecimal(amount_credit_2);
        amount_credit_3 = roundToTwoDecimal(amount_credit_3);
        amount_credit_4 = roundToTwoDecimal(amount_credit_4);

        amount_debit_1 = roundToTwoDecimal(amount_debit_1);
        amount_debit_2 = roundToTwoDecimal(amount_debit_2);
        amount_debit_3 = roundToTwoDecimal(amount_debit_3);
        amount_debit_4 = roundToTwoDecimal(amount_debit_4);

        int ratio_credit_Week = calculatePercentageChange(amount_credit_1, amount_credit_2);

        updateComparisonTextView(amount_credit_1, amount_credit_2, ratio_credit_Week);

        updateBarChart(barChart, new double[]{amount_credit_4, amount_credit_3, amount_credit_2, amount_credit_1},
                new String[]{
                        CalendarHelper.getWeekRange(2),
                        CalendarHelper.getWeekRange(1),
                        CalendarHelper.getWeekRange(0),
                        "Hiện tại"},
                "#3fb950", "#78ce84");

        updateBarChart(barChart2, new double[]{-amount_debit_4, -amount_debit_3, -amount_debit_2, -amount_debit_1},
                new String[]{
                        CalendarHelper.getWeekRange(2),
                        CalendarHelper.getWeekRange(1),
                        CalendarHelper.getWeekRange(0),
                        "Hiện tại"},
                "#da3633", "#e15e5b");
    }

    public void ShowMonth() {
        Pair<Long, Long> range_1 = CalendarHelper.getMonthRangeInMillis(0);
        Pair<Long, Long> range_2 = CalendarHelper.getMonthRangeInMillis(1);
        Pair<Long, Long> range_3 = CalendarHelper.getMonthRangeInMillis(2);
        Pair<Long, Long> range_4 = CalendarHelper.getMonthRangeInMillis(3);
        Pair<Long, Long> range_5 = CalendarHelper.getMonthRangeInMillis(4);
        Pair<Long, Long> range_6 = CalendarHelper.getMonthRangeInMillis(5);

        InitElementDescription("credit", range_1.first, range_1.second);
        InitElementDescription("debit", range_1.first, range_1.second);

        double amount_credit_1 = getAmountForRange(range_1, true);
        double amount_credit_2 = getAmountForRange(range_2, true);
        double amount_credit_3 = getAmountForRange(range_3, true);
        double amount_credit_4 = getAmountForRange(range_4, true);
        double amount_credit_5 = getAmountForRange(range_5, true);
        double amount_credit_6 = getAmountForRange(range_6, true);

        double amount_debit_1 = getAmountForRange(range_1, false);
        double amount_debit_2 = getAmountForRange(range_2, false);
        double amount_debit_3 = getAmountForRange(range_3, false);
        double amount_debit_4 = getAmountForRange(range_4, false);
        double amount_debit_5 = getAmountForRange(range_5, false);
        double amount_debit_6 = getAmountForRange(range_6, false);

        amount_credit_1 = roundToTwoDecimal(amount_credit_1);
        amount_credit_2 = roundToTwoDecimal(amount_credit_2);
        amount_credit_3 = roundToTwoDecimal(amount_credit_3);
        amount_credit_4 = roundToTwoDecimal(amount_credit_4);
        amount_credit_5 = roundToTwoDecimal(amount_credit_5);
        amount_credit_6 = roundToTwoDecimal(amount_credit_6);

        amount_debit_1 = roundToTwoDecimal(amount_debit_1);
        amount_debit_2 = roundToTwoDecimal(amount_debit_2);
        amount_debit_3 = roundToTwoDecimal(amount_debit_3);
        amount_debit_4 = roundToTwoDecimal(amount_debit_4);
        amount_debit_5 = roundToTwoDecimal(amount_debit_5);
        amount_debit_6 = roundToTwoDecimal(amount_debit_6);

        int ratioCreditMonth = calculatePercentageChange(amount_credit_1, amount_credit_2);

        updateComparisonTextView(amount_credit_1, amount_credit_2, ratioCreditMonth);

        updateBarChart(barChart, new double[]{amount_credit_6, amount_credit_5, amount_credit_4, amount_credit_3, amount_credit_2, amount_credit_1},
                new String[]{
                        CalendarHelper.getMonthName(5),
                        CalendarHelper.getMonthName(4),
                        CalendarHelper.getMonthName(3),
                        CalendarHelper.getMonthName(2),
                        CalendarHelper.getMonthName(1),
                        "Hiện tại"},
                "#3fb950", "#78ce84");

        updateBarChart(barChart2, new double[]{-amount_debit_6, -amount_debit_5, -amount_debit_4, -amount_debit_3, -amount_debit_2, -amount_debit_1},
                new String[]{
                        CalendarHelper.getMonthName(5),
                        CalendarHelper.getMonthName(4),
                        CalendarHelper.getMonthName(3),
                        CalendarHelper.getMonthName(2),
                        CalendarHelper.getMonthName(1),
                        "Hiện tại"},
                "#da3633", "#e15e5b");
    }

    private double getAmountForRange(Pair<Long, Long> range, boolean isCredit) {
        return isCredit ? CashFlowHelper.database.getCashFlowDao().getCreditAmountSumForDateRange(range.first, range.second) :
                CashFlowHelper.database.getCashFlowDao().getDebitAmountSumForDateRange(range.first, range.second);
    }

    private double roundToTwoDecimal(double value) {
        return ((int) (value * 100)) / 100.0;
    }

    private int calculatePercentageChange(double amount1, double amount2) {
        if (amount2 == 0) return 0;
        return (int) ((amount1 - amount2) / amount2 * 100);
    }

    private void updateComparisonTextView(double amount1, double amount2, int percentageChange) {
        if (amount1 >= amount2 && Math.abs(amount2 - 0) > 0.1f) {
            compareTextView.setText("So với tuần/tháng trước tăng (↑) " + percentageChange + "% (" + (amount1 - amount2) + ")");
            compareTextView2.setText("So với tuần/tháng trước tăng (↑) " + percentageChange + "% (" + (amount1 - amount2) + ")");
        } else if (Math.abs(amount2 - 0) > 0.1f) {
            compareTextView.setText("So với tuần/tháng trước giảm (↓) " + percentageChange + "% (" + (amount2 - amount1) + ")");
            compareTextView2.setText("So với tuần/tháng trước giảm (↓) " + percentageChange + "% (" + (amount2 - amount1) + ")");
        } else {
            compareTextView.setText("");
            compareTextView2.setText("");
        }
    }

    private void updateBarChart(BarChart barChart, double[] amounts, String[] labels, String barColor1, String barColor2) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (int i = 0; i < amounts.length; i++) {
            entries.add(new BarEntry(i, (float) amounts[i]));
        }
        for (int i = 0; i < amounts.length-1; i++) {
            colors.add(Color.parseColor(barColor2));
        }
        colors.add(Color.parseColor(barColor1));

        BarDataSet barDataSet = new BarDataSet(entries, "VNĐ");

        barDataSet.setColors(colors);

        barDataSet.setValueTextSize(8);

        BarData data = new BarData(barDataSet);
        barChart.setData(data);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000, Easing.EaseOutSine);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.getBarData().setBarWidth(0.5f);
        barChart.invalidate();
    }

    private void InitElementDescription(String type, long start, long end) {
        View view = getView();
        if (view == null) {
            return;
        }

        List<CashItem> cashItemList = new ArrayList<>();
        if ("credit".equals(type)) {
            elementList = view.findViewById(R.id.linearLayoutGroup);
            cashItemList = CashFlowHelper.database.getCashFlowDao().getCreditItems(start, end);
        } else {
            elementList = view.findViewById(R.id.linearLayoutGroup2);
            cashItemList = CashFlowHelper.database.getCashFlowDao().getDebitItems(start, end);
        }
        elementList.removeAllViews();

        for (CashItem item : cashItemList) {
            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding(10, 10, 10, 10);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            GradientDrawable border = new GradientDrawable();
            border.setColor(Color.WHITE);
            border.setStroke(2, Color.GRAY);
            border.setCornerRadius(8);

            itemLayout.setBackground(border);

            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(40, 100));
            imageView.setImageResource(R.drawable.ic_outline_monetization_on_24);
            itemLayout.addView(imageView);

            TextView descriptionTextView = new TextView(getContext());
            LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            descriptionParams.setMargins(20, 20, 20, 20);
            descriptionTextView.setLayoutParams(descriptionParams);
            descriptionTextView.setPadding(20, 20, 10, 20);
            descriptionTextView.setText(item.getDesc());
            descriptionTextView.setTextSize(16);
            descriptionTextView.setTextColor(Color.BLACK);
            itemLayout.addView(descriptionTextView);

            TextView totalTextView = new TextView(getContext());
            LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            totalParams.setMargins(20, 20, 20, 20);
            totalTextView.setLayoutParams(totalParams);
            totalTextView.setGravity(Gravity.CENTER);
            totalTextView.setText(String.format("%.2f", item.getAmount()));
            totalTextView.setTextSize(14);
            totalTextView.setTextColor(Color.BLACK);
            itemLayout.addView(totalTextView);

            elementList.addView(itemLayout);
        }
    }
    public enum ChartDataType {
        week,
        month,
        option
    }
}