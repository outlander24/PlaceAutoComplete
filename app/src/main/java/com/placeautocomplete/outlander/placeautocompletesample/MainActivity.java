package com.placeautocomplete.outlander.placeautocompletesample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.CustomPlaceAutoCompleteFragment;
import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.ErrorCodes;
import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.PlaceAutoCompleteFragment;
import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.PlaceData;

public class MainActivity extends AppCompatActivity {

    private TextView mTvPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvPlace = (TextView) findViewById(R.id.place);

        mTvPlace.setOnClickListener(mOpenPlaceAutoCompleteFragment);
    }

    private View.OnClickListener mOpenPlaceAutoCompleteFragment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CustomPlaceAutoCompleteFragment customPlaceAutoCompleteFragment = CustomPlaceAutoCompleteFragment.getInstance(new CustomPlaceAutoCompleteFragment.IOnPlaceSelectedListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    mTvPlace.setText(place.getAddress());
                    getSupportFragmentManager().popBackStackImmediate();
                }

                @Override
                public void onErrorOccured(int errorCode) {
                    getSupportFragmentManager().popBackStackImmediate();
                    switch (errorCode) {
                        case ErrorCodes.NETWORK_ISSUE:
                            Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                            break;

                        case ErrorCodes.ZERO_RESULTS:
                            Toast.makeText(getApplicationContext(), "No results found!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });

//            PlaceAutoCompleteFragment placeAutoCompleteFragment = PlaceAutoCompleteFragment.getInstance(getBaseContext(), true, false);
//            placeAutoCompleteFragment.addOnPlaceSelectedListener(new PlaceAutoCompleteFragment.IOnPlaceSelectedListener() {
//                @Override
//                public void onPlaceSelected(PlaceData placeData) {
//                    mTvPlace.setText(placeData.formattedAddress);
//                }
//
//                @Override
//                public void onErrorOccurred(int errorCode) {
//                    switch (errorCode) {
//                        case ErrorCodes.NETWORK_ISSUE:
//                            Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
//                            break;
//
//                        case ErrorCodes.ZERO_RESULTS:
//                            Toast.makeText(getApplicationContext(), "No results found!", Toast.LENGTH_SHORT).show();
//                            break;
//                    }
//                }
//            });
            addFragment(customPlaceAutoCompleteFragment);
        }
    };

    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.container) != null) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }
}
