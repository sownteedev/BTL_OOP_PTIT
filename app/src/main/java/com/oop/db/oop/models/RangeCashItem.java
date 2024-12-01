package com.oop.db.oop.models;

import com.oop.db.oop.CashItem;

public class RangeCashItem extends CashItem {

    public double credit;

    public double debit;

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public double getDebit() {
        return debit;
    }

    public void setDebit(double debit) {
        this.debit = debit;
    }

    public CashItem getCashItem()
    {
        CashItem item = new CashItem(getId(),getDesc(),getAmount(),getType(),getTime());
        item.setStartDate(getStartDate());
        item.setEndDate(getEndDate());
        item.setCount(getCount());
        item.setViewMode(getViewMode());
        return item;
    }
}
