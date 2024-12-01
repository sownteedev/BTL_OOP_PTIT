package com.oop.helper;

import static com.oop.helper.Constants.STATEMENT_TYPE_CREDIT;
import static com.oop.helper.Constants.STATEMENT_TYPE_DEBIT;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_INDIVIDUAL;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_MONTHLY;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_WEEKLY;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_YEARLY;

import android.content.Context;

import androidx.room.Room;

import com.oop.db.oop.CashFlowDatabase;
import com.oop.db.oop.CashItem;
import com.oop.db.oop.models.RangeCashItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CashFlowHelper {

    public static CashFlowDatabase database;

    public static CashFlowDatabase getDatabaseInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(), CashFlowDatabase.class, "CashFlow").allowMainThreadQueries().build();
        }
        return database;
    }

    public CashFlowHelper() {
    }

    public static List<CashItem> getCashItems(int viewMode, long startTime, long endTime) {
        switch (viewMode) {
            case STATEMENT_VIEW_MODE_WEEKLY:
                return getWeeklyCashItem(startTime, endTime);
            case STATEMENT_VIEW_MODE_MONTHLY:
                return getMonthlyCashItems(startTime, endTime);
            case STATEMENT_VIEW_MODE_YEARLY:
                return getYearlyCashItems(startTime, endTime);
            default:
                return getIndividualCashItems(startTime, endTime);
        }
    }

    public static double getTotalAmountForType(String type, long start, long end) {
        if (start == 0 && end == 0) {
            return database.getCashFlowDao().getAmountSum(type);
        } else {
            return database.getCashFlowDao().getAmountSumForDateRangeByType(type, start, end);
        }
    }
    public static double getTotalAmountForMonth(long start, long end) {
        if (start == 0 && end == 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            long monthStart = calendar.getTimeInMillis();

            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            long monthEnd = calendar.getTimeInMillis() - 1;

            return database.getCashFlowDao().getAmountSumForDateRange(monthStart, monthEnd);
        } else {
            return database.getCashFlowDao().getAmountSumForDateRange(start, end);
        }
    }

    public static List<CashItem> getWeeklyCashItem(long startTime, long endTime) {

        List<RangeCashItem> itemList;
        List<CashItem> cashItemList = new ArrayList<>();

        if (startTime == 0 && endTime == 0) {
            itemList = database.getCashFlowDao().getWeeklyItems();
        } else {
            itemList = database.getCashFlowDao().getWeeklyItemsForDateRange(startTime, endTime);
        }

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < itemList.size(); i++) {
            RangeCashItem item = itemList.get(i);
            String[] yearWeekNumber = item.getWeekKey().split("-");

            if (yearWeekNumber.length < 2 || yearWeekNumber[0].isEmpty() || yearWeekNumber[1].isEmpty()) {
                continue; // Skip this item if the week key is invalid
            }

            calendar.clear();
            calendar.set(Calendar.YEAR, Integer.parseInt(yearWeekNumber[0]));
            calendar.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(yearWeekNumber[1]));
            calendar.setFirstDayOfWeek(Calendar.SUNDAY);
            calendar.set(Calendar.DAY_OF_WEEK, 1);
            Date weekStartDate = calendar.getTime();
            long weekEndDate = weekStartDate.getTime() + 6 * (24 * 60 * 60 * 1000);
            item.setStartDate(weekStartDate.getTime());
            item.setEndDate(weekEndDate + (24 * 60 * 60 * 1000) - 1000);
            item.setViewMode(STATEMENT_VIEW_MODE_WEEKLY);
            item.setTime(item.getStartDate());

            BigDecimal credit = new BigDecimal(String.valueOf(item.getCredit()));
            BigDecimal debit = new BigDecimal(String.valueOf(item.getDebit()));
            item.setAmount(credit.subtract(debit).doubleValue());

            if (item.getCount() != 1) {
                item.setDesc(item.getCount() + " transactions");
            } else {
                item.setDesc(item.getCount() + " transaction");
            }
            if (item.getAmount() > -1) {
                item.setType(STATEMENT_TYPE_CREDIT);
            } else {
                item.setType(STATEMENT_TYPE_DEBIT);
            }
            cashItemList.add(item.getCashItem());
        }
        return cashItemList;
    }

    public static List<CashItem> getMonthlyCashItems(long startTime, long endTime) {

        List<RangeCashItem> itemList;
        List<CashItem> cashItemList = new ArrayList<>();

        if (startTime == 0 && endTime == 0) {
            itemList = database.getCashFlowDao().getMonthlyItems();
        } else {
            itemList = database.getCashFlowDao().getMonthlyItemsForDateRange(startTime, endTime);
        }

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < itemList.size(); i++) {
            RangeCashItem item = itemList.get(i);
            String[] yearMonthNumber = item.getMonthKey().split("-");

            if (yearMonthNumber.length < 2) {
                continue; // Skip this item if the month key is invalid
            }

            calendar.clear();
            int processingMonth = Integer.parseInt(yearMonthNumber[1]) - 1;
            calendar.set(Calendar.YEAR, Integer.parseInt(yearMonthNumber[0]));
            calendar.set(Calendar.MONTH, processingMonth);

            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date monthStartDate = calendar.getTime();

            int year = Integer.parseInt(yearMonthNumber[0]);
            int lastDay = 30;
            if (processingMonth == 1) {
                if ((year % 100 == 0 && year % 400 == 0) || (year % 100 != 0 && year % 4 == 0)) {
                    lastDay = 29;
                } else {
                    lastDay = 28;
                }
            } else if ((processingMonth == 0) || (processingMonth == 2 || (processingMonth == 4) || (processingMonth == 6) || (processingMonth == 7) || (processingMonth == 9) || (processingMonth == 11))) {
                lastDay = 31;
            }
            calendar.set(Calendar.DAY_OF_MONTH, lastDay);
            Date monthEndDate = calendar.getTime();

            BigDecimal credit = new BigDecimal(String.valueOf(item.getCredit()));
            BigDecimal debit = new BigDecimal(String.valueOf(item.getDebit()));
            item.setAmount(credit.subtract(debit).doubleValue());

            item.setStartDate(monthStartDate.getTime());
            item.setEndDate(monthEndDate.getTime() + (24 * 60 * 60 * 1000) - 1000);
            item.setTime(item.getStartDate());
            item.setViewMode(STATEMENT_VIEW_MODE_MONTHLY);
            if (item.getCount() != 1) {
                item.setDesc(item.getCount() + " transactions");
            } else {
                item.setDesc(item.getCount() + " transaction");
            }
            if (item.getAmount() > -1) {
                item.setType(STATEMENT_TYPE_CREDIT);
            } else {
                item.setType(STATEMENT_TYPE_DEBIT);
            }
            cashItemList.add(item.getCashItem());
        }
        return cashItemList;
    }

    private static List<CashItem> getYearlyCashItems(long startTime, long endTime) {

        List<RangeCashItem> itemList;
        List<CashItem> cashItemList = new ArrayList<>();

        if (startTime == 0 && endTime == 0) {
            itemList = database.getCashFlowDao().getYearlyItems();
        } else {
            itemList = database.getCashFlowDao().getYearlyItemsForDateRange(startTime, endTime);
        }

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < itemList.size(); i++) {
            RangeCashItem item = itemList.get(i);

            String yearNumberKey = item.getYearKey();
            if (yearNumberKey == null || yearNumberKey.isEmpty()) {
                continue; // Skip this item if the year key is invalid
            }

            calendar.clear();
            calendar.set(Calendar.YEAR, Integer.parseInt(yearNumberKey));
            Date yearStartDate = calendar.getTime();

            int year = Integer.parseInt(yearNumberKey);

            calendar.set(Calendar.YEAR, year + 1);
            Date yearEndDate = calendar.getTime();

            item.setStartDate(yearStartDate.getTime());
            item.setEndDate(yearEndDate.getTime() - 1000);

            BigDecimal credit = new BigDecimal(String.valueOf(item.getCredit()));
            BigDecimal debit = new BigDecimal(String.valueOf(item.getDebit()));
            item.setAmount(credit.subtract(debit).doubleValue());

            item.setTime(item.getStartDate());
            item.setViewMode(STATEMENT_VIEW_MODE_YEARLY);
            if (item.getCount() != 1) {
                item.setDesc(item.getCount() + " transactions");
            } else {
                item.setDesc(item.getCount() + " transaction");
            }
            if (item.getAmount() > -1) {
                item.setType(STATEMENT_TYPE_CREDIT);
            } else {
                item.setType(STATEMENT_TYPE_DEBIT);
            }
            cashItemList.add(item.getCashItem());
        }
        return cashItemList;
    }

    public static List<CashItem> getIndividualCashItems(long startTime, long endTime) {

        List<CashItem> itemList;

        if (startTime == 0 && endTime == 0) {
            itemList = database.getCashFlowDao().getAllItems();
        } else {
            itemList = database.getCashFlowDao().getAllItemsForDateRange(startTime, endTime);
        }


        for (int i = 0; i < itemList.size(); i++) {
            CashItem item = itemList.get(i);

            item.setViewMode(STATEMENT_VIEW_MODE_INDIVIDUAL);
        }

        return itemList;
    }


}
