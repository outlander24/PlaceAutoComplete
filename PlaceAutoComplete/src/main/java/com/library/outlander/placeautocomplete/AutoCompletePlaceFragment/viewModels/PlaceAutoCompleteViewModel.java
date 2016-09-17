package com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.viewModels;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.gson.annotations.SerializedName;
import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.adapters.PlaceAutoCompleteResultsAdapter;
import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.constants.ErrorCodes;
import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.view.CustomPlaceAutoCompleteFragment;
import com.library.outlander.placeautocomplete.R;
import com.library.outlander.placeautocomplete.databinding.FragmentPlaceAutocompleteBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by aashish on 9/16/16.
 */
public class PlaceAutoCompleteViewModel extends BaseObservable implements TextWatcher {

    private FragmentPlaceAutocompleteBinding mBinding;
    private Context mContext;
    private Activity mActivity;
    private String mPreviousQuery;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteResultsAdapter mAdapter;
    private HashMap<String, List<PlaceAutocomplete>> mCachedResults;
    private CustomPlaceAutoCompleteFragment.IOnPlaceSelectedListener mListener;

    public static final int PERMISSION_REQUEST_LOCATION = 666;

    private static final String TAG = "PlaceAutoCompleteViewModel";

    public PlaceAutoCompleteViewModel(Activity activity, Context context, FragmentPlaceAutocompleteBinding binding,
                                      GoogleApiClient googleApiClient, PlaceAutoCompleteResultsAdapter adapter,
                                      CustomPlaceAutoCompleteFragment.IOnPlaceSelectedListener listener) {
        mBinding = binding;
        mContext = context;
        mActivity = activity;
        mGoogleApiClient = googleApiClient;
        mAdapter = adapter;
        mCachedResults = new HashMap<>();
        mListener = listener;

        mBinding.etSearch.addTextChangedListener(this);
        mBinding.ivClearIcon.setColorFilter(mContext.getResources().getColor(R.color.gray_dark));
        mBinding.ivSearchIcon.setColorFilter(mContext.getResources().getColor(android.R.color.black));
        mBinding.ivLocation.setColorFilter(mContext.getResources().getColor(R.color.gray_dark));
        mBinding.rvSearchResults.setAdapter(mAdapter);
        mBinding.rvSearchResults.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }

    @Bindable
    public View.OnClickListener getOnClearClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.etSearch.setText("");
            }
        };
    }

    @Bindable
    public View.OnClickListener getOnFetchCurrentLocationClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLocationServicesAvailable(mContext)) {
                    PendingResult<PlaceLikelihoodBuffer> currentPlaceResult = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                    currentPlaceResult.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                            if (placeLikelihoods.getStatus().isSuccess()) {
                                mListener.onPlaceSelected(placeLikelihoods.get(0).getPlace());
                            } else {
                                mListener.onErrorOccured(ErrorCodes.NETWORK_ISSUE);
                            }
                        }
                    });
                }
            }
        };
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.toString().trim().isEmpty()) {
            mBinding.ivClearIcon.setVisibility(View.GONE);
            mAdapter.setResults(null);
        } else {
            mBinding.ivClearIcon.setVisibility(View.VISIBLE);
        }

        //Start API call
        if (editable.toString().trim().length() > 2) {
            mPreviousQuery = editable.toString();
            if (mCachedResults.get(mPreviousQuery) == null || mCachedResults.get(mPreviousQuery).size() == 0) {
                getPredictions();
            } else {
                mAdapter.setResults(mCachedResults.get(mPreviousQuery));
            }
        }
    }

    private void getPredictions() {
        if (mGoogleApiClient != null) {
            Log.i(TAG, "Executing autocomplete query for: " + mPreviousQuery);
            showProgressBar();
            final PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, mPreviousQuery,
                                    null, null);
            results.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                @Override
                public void onResult(@NonNull AutocompletePredictionBuffer autocompletePredictions) {
                    final Status status = autocompletePredictions.getStatus();
                    if (!status.isSuccess()) {
                        Toast.makeText(mContext, "Error: " + status.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error getting place predictions: " + status
                                .toString());
                        mListener.onErrorOccured(ErrorCodes.NETWORK_ISSUE);
                        autocompletePredictions.release();
                        //return null;
                    }

                    Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                            + " predictions.");
                    Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
                    ArrayList<PlaceAutocomplete> resultList = new ArrayList<>(autocompletePredictions.getCount());
                    while (iterator.hasNext()) {
                        AutocompletePrediction prediction = iterator.next();
                        resultList.add(new PlaceAutocomplete(prediction.getPrimaryText(null), prediction.getPlaceId(),
                                prediction.getFullText(null)));
                    }
                    // Buffer release
                    autocompletePredictions.release();
                    mAdapter.setResults(resultList);
                    mCachedResults.put(mPreviousQuery, resultList);
                    hideProgressBar();
                    if (resultList.size() == 0) {
                        mListener.onErrorOccured(ErrorCodes.ZERO_RESULTS);
                    }
                }
            });
        } else {
            mListener.onErrorOccured(ErrorCodes.NETWORK_ISSUE);
            Log.e(TAG, "Google API client is not connected.");
        }
    }

    public static class PlaceAutocomplete implements Parcelable {

        @SerializedName("place_id")
        public CharSequence placeId;

        @SerializedName("description")
        public CharSequence description;

        @SerializedName("title")
        public CharSequence title;

        public PlaceAutocomplete(CharSequence title, CharSequence placeId, CharSequence description) {
            this.title = title;
            this.placeId = placeId;
            this.description = description;
        }

        @Override
        public String toString() {
            return description.toString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title.toString());
            dest.writeString(description.toString());
            dest.writeString(placeId.toString());

        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

            public PlaceAutocomplete createFromParcel(Parcel in) {
                return new PlaceAutocomplete(in);
            }

            @Override
            public Object[] newArray(int size) {
                return new PlaceAutocomplete[size];
            }
        };

        //De-Parcel
        private PlaceAutocomplete(Parcel in) {
            title = in.readString();
            description = in.readString();
            placeId = in.readString();
        }
    }

    //Utilities methods start
    public void clearCache() {
        mCachedResults.clear();
    }

    public void freeReferences() {
        mActivity = null;
        mBinding = null;
        mContext = null;
        mCachedResults = null;
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClient = null;
    }

    private void showProgressBar() {
        mBinding.pbAutocomplete.setVisibility(View.VISIBLE);
        mBinding.ivClearIcon.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        mBinding.pbAutocomplete.setVisibility(View.GONE);
        if (!mBinding.etSearch.getText().toString().trim().isEmpty()) {
            mBinding.ivClearIcon.setVisibility(View.VISIBLE);
        }
    }

    public boolean isLocationServicesAvailable(Context context) {
        int locationMode = 0;
        String locationProviders;
        boolean isAvailable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }

        if (!isAvailable) {
            mContext.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return false;
        }

        boolean coarsePermissionCheck = (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean finePermissionCheck = (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!coarsePermissionCheck && !finePermissionCheck) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.CALL_PHONE},
                    PERMISSION_REQUEST_LOCATION);
        }

        return isAvailable && (coarsePermissionCheck || finePermissionCheck);
    }
    //Utilities methods end
}

