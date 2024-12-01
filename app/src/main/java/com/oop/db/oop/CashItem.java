package com.oop.db.oop;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "CashFlow")
public class CashItem implements Comparable<CashItem> {

    public long startDate;
    public long endDate;
    public int viewMode;
    public int count;

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)

    private long id;
    @ColumnInfo(name = "desc")
    private String desc;
    @ColumnInfo(name = "amount")
    private double amount;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "time")
    private long time;
    @ColumnInfo(name = "year_key")
    private String yearKey;
    // Combination of month and year - tham khao chatGPT
    @ColumnInfo(name = "month_key")
    private String monthKey;
    // Combination of week and year - tham khao chatGPT
    @ColumnInfo(name = "week_key")
    private String weekKey;

    public CashItem() {
    }

    public CashItem(long id, String desc, double amount, String type, long time) {
        this.id = id;
        this.desc = desc;
        this.amount = amount;
        this.type = type;
        this.time = time;

        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        SimpleDateFormat weekKeyFormat = new SimpleDateFormat("yyyy-ww");
        weekKeyFormat.setCalendar(calendar);
        this.weekKey = weekKeyFormat.format(date);

        SimpleDateFormat monthKeyFormat = new SimpleDateFormat("yyyy-MM");
        String yearMonthNumberKey = monthKeyFormat.format(date);
        this.monthKey = yearMonthNumberKey;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String yearNumberKey = sdf.format(date);
        this.yearKey = yearNumberKey;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getViewMode() {
        return viewMode;
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }


    public String getYearKey() {
        return yearKey;
    }

    public void setYearKey(String yearKey) {
        this.yearKey = yearKey;
    }

    public String getMonthKey() {
        return monthKey;
    }

    public void setMonthKey(String monthKey) {
        this.monthKey = monthKey;
    }

    public String getWeekKey() {
        return weekKey;
    }

    public void setWeekKey(String weekKey) {
        this.weekKey = weekKey;
    }

    @Override
    public int compareTo(CashItem cashItem) {
        return (this.getTime() - cashItem.getTime()) > 0 ? 1 : -1;
    }
}
