/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.flipphone.listing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flipphone.MainActivity;
import com.flipphone.R;
import com.flipphone.adapter.RatingAdapter;
import com.flipphone.model.Phone;
import com.flipphone.model.Rating;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class PhoneDetailActivity extends AppCompatActivity implements
        View.OnClickListener,
        EventListener<DocumentSnapshot>,
        ChatDialogFragment.RatingListener {

    private static final String TAG = "PhoneDetail";
    private CardView descriptionView;
    public static final String KEY_PHONE_ID = "key_phone_id";
    String phoneId;
    private ImageView mImageView;
    private TextView mNameView;
    private MaterialRatingBar mRatingIndicator;
    private TextView mNumRatingsView;
    private TextView mConditionView;
    private TextView mCategoryView;
    private TextView mPriceView;
    private ViewGroup mEmptyView;
    private RecyclerView mRatingsRecycler;
    private ImageButton deleteButton;
    private TextView listingDescription;
    private ChatDialogFragment mRatingDialog;
    private TextView specText;

    private FirebaseFirestore mFirestore;
    private DocumentReference mPhoneRef;
    private ListenerRegistration mPhoneRegistration;
    private String listingPhoneNum;
    private RatingAdapter mRatingAdapter;
    private ProgressBar spinner;
    private LinearLayout showSpecs;
    private LinearLayout hideSpecs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_detail);
        specText = findViewById(R.id.spec_text);
        descriptionView = findViewById(R.id.desc_visibility);
        mImageView = findViewById(R.id.phone_image);
        mNameView = findViewById(R.id.phone_name);
        //mRatingIndicator = findViewById(R.id.phone_rating);
        //mNumRatingsView = findViewById(R.id.phone_num_ratings);
        mConditionView = findViewById(R.id.phone_condition);
        mCategoryView = findViewById(R.id.phone_category);
        mPriceView = findViewById(R.id.phone_price);
        mEmptyView = findViewById(R.id.view_empty_ratings);
        //mRatingsRecycler = findViewById(R.id.recycler_ratings);
        deleteButton = findViewById(R.id.delete_button);
        listingDescription = findViewById(R.id.listing_description);
        showSpecs = findViewById(R.id.with_specification);
        hideSpecs = findViewById(R.id.without_specification);
        findViewById(R.id.phone_button_back).setOnClickListener(this);
        findViewById(R.id.fab_show_rating_dialog).setOnClickListener(this);
        findViewById(R.id.phone_image).setOnClickListener(this);
        // Get phone ID from extras
        phoneId = getIntent().getExtras().getString(KEY_PHONE_ID);
        if (phoneId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_PHONE_ID);
        }

        String user = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        FirebaseFirestore mRef = FirebaseFirestore.getInstance();
        mRef.collection("users").document(phoneId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Phone phone = documentSnapshot.toObject(Phone.class);
                listingPhoneNum = phone.getUserid();
                if(!phone.getDescription().equals(""))
                {
                    descriptionView.setVisibility(View.VISIBLE);
                }
                if(!phone.getName().equals("null"))
                {
                    showSpecs.setVisibility(View.VISIBLE);
                    hideSpecs.setVisibility(View.GONE);
                    specText.setText(String.format(phone.getSpecifications().toString(),15));
                }
                if(phone.getUserid().equals(user)) {
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //mRef.collection("users").document(phoneId).delete();
                            AlertDialog alertDialog = new AlertDialog.Builder(PhoneDetailActivity.this).create();
                            alertDialog.setTitle("Delete");
                            alertDialog.setMessage("Would you like to delete the listing?");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Yes", ((dialog , which) -> {
                                spinner = (ProgressBar)findViewById(R.id.progress_circular_delete);
                                spinner.getIndeterminateDrawable().setColorFilter(Color.parseColor("#4285F4"), PorterDuff.Mode.SRC_IN);
                                spinner.setVisibility(View.VISIBLE);
                                FirebaseFirestore mRef = FirebaseFirestore.getInstance();
                                mRef.collection("users").document(phoneId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(PhoneDetailActivity.this, "Listing Deleted", Toast.LENGTH_SHORT).show();
                                        Intent newIntent = new Intent(PhoneDetailActivity.this, MainActivity.class);
                                        spinner.setVisibility(View.GONE);
                                        startActivity(newIntent);
                                    }
                                });
                                /*Intent intent = new Intent(PhoneDetailActivity.this, DeleteActivity.class);
                                //Bundle extras = new Bundle();
                                //extras.putString("DELETE", phoneId);
                                //intent.putExtras(extras);
                                startActivity(intent);*/}));
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", ((dialog , which) -> dialog.dismiss()));
                            alertDialog.show();

                        }
                    });
                }
            }
        });
        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the phone
        mPhoneRef = mFirestore.collection("users").document(phoneId);

        // Get ratings
        Query ratingsQuery = mPhoneRef
                .collection("users")
                .orderBy("price", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    //mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    //mRatingsRecycler.setVisibility(View.VISIBLE);
                    // mEmptyView.setVisibility(View.GONE);
                }
            }
        };

        //mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
       // mRatingsRecycler.setAdapter(mRatingAdapter);

        mRatingDialog = new ChatDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        mRatingAdapter.startListening();
        mPhoneRegistration = mPhoneRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        if (mPhoneRegistration != null) {
            mPhoneRegistration.remove();
            mPhoneRegistration = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.phone_button_back:
                onBackArrowClicked(v);
                break;
            case R.id.fab_show_rating_dialog:
                onAddRatingClicked(v);
                break;
            case R.id.phone_image:
                Intent intent = new Intent(this, ImageSliderActivity.class);
                intent.putExtra(PhoneDetailActivity.KEY_PHONE_ID, phoneId);
                startActivity(intent);
        }
    }

    private Task<Void> addRating(final DocumentReference phoneRef, final Rating rating) {
        // TODO(developer): Implement
        return Tasks.forException(new Exception("not yet implemented"));
    }


    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "phone:onEvent", e);
            return;
        }

        onPhoneLoaded(snapshot.toObject(Phone.class));
    }

    private void onPhoneLoaded(Phone phone) {
        try {
            mNameView.setText(phone.getName());
            //mRatingIndicator.setRating((float) phone.getAvgRating());
            //mNumRatingsView.setText(getString(R.string.fmt_num_ratings, phone.getNumRatings()));
            mConditionView.setText(phone.getCondition());
            mCategoryView.setText(phone.getCategory());
            mPriceView.setText("$" + phone.getPrice());
            listingDescription.setText(phone.getDescription());
            if(phone.getName() == null)
            {
                return;
            }
            mNameView.setText(phone.getName());
            // Background image
            Glide.with(mImageView.getContext())
                    .load(phone.getPhoto())
                    .into(mImageView);
        }
        catch (Exception e){
            Log.e("ERROR", e.toString() );
        }
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void onAddRatingClicked(View view) {
        Uri sms_uri = Uri.parse("smsto:"+ listingPhoneNum);
        Intent intent = new Intent(Intent.ACTION_SENDTO,sms_uri);
        intent.putExtra("sms_body", "I'm interested in your listing.");
        startActivity(intent);
    }

    @Override
    public void onRating(Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mPhoneRef, rating)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
