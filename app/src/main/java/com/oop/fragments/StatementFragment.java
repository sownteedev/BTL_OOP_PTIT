package com.oop.fragments;

import static com.oop.helper.Constants.STATEMENT_TYPE_CREDIT;
import static com.oop.helper.Constants.STATEMENT_TYPE_DEBIT;
import static com.oop.helper.Constants.STATEMENT_TYPE_UNKNOWN;
import static com.oop.helper.Constants.STATEMENT_VIEW_MODE_INDIVIDUAL;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oop.R;
import com.oop.adapter.CashFlowAdapter;
import com.oop.db.oop.CashItem;
import com.oop.helper.CashFlowHelper;
import com.oop.helper.Constants;
import com.oop.helper.DateTimeHelper;
import com.oop.helper.PrefHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class StatementFragment extends Fragment {

    private static final String TAG = "StatementFragment";
    public boolean isDateRangePicked = false;
    public TextView dateRangeView;
    TextView incomeTextView, expenseTextView, expenseTitleTextView, incomeTitleTextView;
    RecyclerView recyclerView;
    CashFlowAdapter adapter;
    List<CashItem> realStatementList = new ArrayList<>();
    View view;
    TextView emptyStatementTextView;
    RelativeLayout contentWrapper;
    public CashFlowHelper cashFlowHelper;

    public StatementFragment() {
        cashFlowHelper = new CashFlowHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tab_item, container, false);

        dateRangeView = view.findViewById(R.id.date_range_text_view);

        expenseTextView = view.findViewById(R.id.title_expense_textView);
        expenseTitleTextView = view.findViewById(R.id.title_expense_title_textView);
        expenseTextView.setTextColor(Color.parseColor("#da3633"));
        expenseTitleTextView.setTextColor(Color.parseColor("#da3633"));

        incomeTextView = view.findViewById(R.id.title_income_textView);
        incomeTitleTextView = view.findViewById(R.id.title_income_title_textView);
        incomeTextView.setTextColor(Color.parseColor("#3fb950"));
        incomeTitleTextView.setTextColor(Color.parseColor("#3fb950"));

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new CashFlowAdapter(realStatementList, getContext(), this);
        recyclerView.setAdapter(adapter);

        emptyStatementTextView = view.findViewById(R.id.empty_statement_text_view);
        emptyStatementTextView.setText("No statements to view.");
        contentWrapper = view.findViewById(R.id.content_wrapper);

        loadStatement(0, 0);
        return view;
    }

    public void loadStatementForDateRange(long start, long end) {
        dateRangeView.setText("Range selected: " + DateTimeHelper.getDate(start) + " to " + DateTimeHelper.getDate(end));
        dateRangeView.setVisibility(View.VISIBLE);
        isDateRangePicked = true;

        // add end of day millis to end datetime
        end += (24 * 60 * 60 * 1000) - 1000;

        int viewMode = new PrefHelper(getContext()).getIntPreference(Constants.CURRENT_VIEW_MODE, STATEMENT_VIEW_MODE_INDIVIDUAL);
        List<CashItem> statementList = CashFlowHelper.getCashItems(viewMode, start, end);

        long totalCount = statementList.size();
        if (totalCount > 0) {
            emptyStatementTextView.setVisibility(View.GONE);
            contentWrapper.setVisibility(View.VISIBLE);
        } else {
            emptyStatementTextView.setVisibility(View.VISIBLE);
            contentWrapper.setVisibility(View.GONE);
        }
        expenseTextView.setText(CashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_DEBIT, start, end) + " ₫");
        incomeTextView.setText(CashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_CREDIT, start, end) + " ₫");
        populateData(statementList);
    }

    public void loadStatement(long start, long end) {
        realStatementList.clear();
        recyclerView.removeAllViews();

        if (isDateRangePicked) {
            loadStatementForDateRange(start, end);
        } else {
            int viewMode = new PrefHelper(getContext()).getIntPreference(Constants.CURRENT_VIEW_MODE, STATEMENT_VIEW_MODE_INDIVIDUAL);
            List<CashItem> statementList = CashFlowHelper.getCashItems(viewMode, 0, 0);

            long totalCount = statementList.size();
            if (totalCount > 0) {
                emptyStatementTextView.setVisibility(View.GONE);
                contentWrapper.setVisibility(View.VISIBLE);
            } else {
                emptyStatementTextView.setVisibility(View.VISIBLE);
                contentWrapper.setVisibility(View.GONE);
            }
            expenseTextView.setText(CashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_DEBIT, start, end)+" ₫");
            incomeTextView.setText(CashFlowHelper.getTotalAmountForType(STATEMENT_TYPE_CREDIT, start, end)+" ₫");
            populateData(statementList);
        }
    }

    private void populateData(List<CashItem> statementList) {
        for (CashItem item : statementList) {
            if (!item.getType().equals(STATEMENT_TYPE_UNKNOWN)) {
                realStatementList.add(item);
            }
        }
        Collections.sort(realStatementList);
        adapter.notifyDataSetChanged();
    }

    public void updateData(List<CashItem> updatedCashItems) {
        realStatementList.clear();
        realStatementList.addAll(updatedCashItems);
        adapter.notifyDataSetChanged();

        if (realStatementList.isEmpty()) {
            emptyStatementTextView.setVisibility(View.VISIBLE);
            contentWrapper.setVisibility(View.GONE);
        } else {
            emptyStatementTextView.setVisibility(View.GONE);
            contentWrapper.setVisibility(View.VISIBLE);
        }
    }
}