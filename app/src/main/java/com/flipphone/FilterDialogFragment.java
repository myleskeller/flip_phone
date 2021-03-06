package com.flipphone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.flipphone.model.Phone;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class FilterDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "FilterDialog";

    interface FilterListener {

        void onFilter(Filters filters);

    }

    private View mRootView;

    private Spinner mCategorySpinner;
    private Spinner mConditionSpinner;
    private Spinner mSortSpinner;
    private Spinner mPriceSpinner;

    private FilterListener mFilterListener;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);

        mCategorySpinner = mRootView.findViewById(R.id.spinner_category);
        mConditionSpinner = mRootView.findViewById(R.id.spinner_city);
        //mSortSpinner = mRootView.findViewById(R.id.spinner_sort);
        mPriceSpinner = mRootView.findViewById(R.id.spinner_price);
        List<String> phones = new ArrayList<>();

        phones.add("All Phones");
        FirebaseFirestore mRef = FirebaseFirestore.getInstance();
        CollectionReference phoneRef = mRef.collection("users");


        phoneRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        String phoneName = document.getString("name");
                        phones.add(phoneName);
                    }
                    ArrayList<String> phones2 = new ArrayList<>(new LinkedHashSet<>(phones));
                    @SuppressLint("RestrictedApi") ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, phones2);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mCategorySpinner.setAdapter(adapter);
                }
            }
        });
        mRootView.findViewById(R.id.button_search).setOnClickListener(this);
        mRootView.findViewById(R.id.button_cancel).setOnClickListener(this);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FilterListener) {
            mFilterListener = (FilterListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_search:
                onSearchClicked();
                break;
            case R.id.button_cancel:
                onCancelClicked();
                break;
        }
    }

    public void onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }

        dismiss();
    }

    public void onCancelClicked() {
        dismiss();
    }

    @Nullable
    private String getSelectedCategory() {
        String selected = (String) mCategorySpinner.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedCondition() {
        String selected = (String) mConditionSpinner.getSelectedItem();
        if (getString(R.string.value_any_condition).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    private int getSelectedPrice() {
        String selected = (String) mPriceSpinner.getSelectedItem();
        if (selected.equals(getString(R.string.price_1))) {
            return 1;
        } else if (selected.equals(getString(R.string.price_2))) {
            return 2;
        }  else {
            return -1;
        }
    }

    @Nullable
    private String getSelectedSortBy() {
        String selected = (String) mSortSpinner.getSelectedItem();
         if (getString(R.string.sort_by_price).equals(selected)) {
            return Phone.FIELD_PRICE;
        }

        return null;
    }

    @Nullable
    private Query.Direction getSortDirection() {
        String selected = (String) mPriceSpinner.getSelectedItem();
        if (getString(R.string.price_1).equals(selected)) {
            return Query.Direction.DESCENDING;
        }else if (getString(R.string.price_2).equals(selected)) {
            return Query.Direction.ASCENDING;
        }

        return null;
    }

    public void resetFilters() {
        if (mRootView != null) {
            mCategorySpinner.setSelection(0);
            mConditionSpinner.setSelection(0);
            mPriceSpinner.setSelection(0);
            //mSortSpinner.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        if (mRootView != null) {
            filters.setCategory(getSelectedCategory());
            filters.setCondition(getSelectedCondition());
            filters.setPrice(getSelectedPrice());
            //filters.setSortBy(getSelectedSortBy());
            filters.setSortDirection(getSortDirection());
        }

        return filters;
    }
}
