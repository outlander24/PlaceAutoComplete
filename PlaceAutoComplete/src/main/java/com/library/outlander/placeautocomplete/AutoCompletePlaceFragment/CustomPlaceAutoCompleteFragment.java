package com.library.outlander.placeautocomplete.AutoCompletePlaceFragment;

import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.library.outlander.placeautocomplete.R;
import com.library.outlander.placeautocomplete.databinding.FragmentPlaceAutocompleteBinding;


/**
 * Created by aashish on 9/16/16.
 */
public class CustomPlaceAutoCompleteFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private FragmentPlaceAutocompleteBinding mBinding;
    private PlaceAutoCompleteViewModel mViewModel;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteResultsAdapter mAdapter;
    private IOnPlaceSelectedListener mListener;

    public static CustomPlaceAutoCompleteFragment getInstance(IOnPlaceSelectedListener listener) {
        CustomPlaceAutoCompleteFragment fragment = new CustomPlaceAutoCompleteFragment();
        fragment.mListener = listener;
        return fragment;
    }

    public void setUseMyLocationVisibility(int visibility) {
        mBinding.llUseMyLocationContainer.setVisibility(visibility);
    }

    public void setPlaceSelectedListener(IOnPlaceSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_autocomplete, container, false);
        mBinding = DataBindingUtil.bind(view);
        return view;
    }

    private void initViewModel() {
        mAdapter = new PlaceAutoCompleteResultsAdapter(null, new PlaceAutoCompleteResultsAdapter.OnResultItemClickListener() {
            @Override
            public void onItemClicked(PlaceAutoCompleteViewModel.PlaceAutocomplete placeAutocomplete) {
                // TODO: 9/17/16 Dum dum results here
                Toast.makeText(getContext(), placeAutocomplete.title + "   " + placeAutocomplete.description, Toast.LENGTH_LONG).show();
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeAutocomplete.placeId.toString());
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            Log.e(CustomPlaceAutoCompleteFragment.class.getName(), "Place query did not complete. Error: " +
                                    places.getStatus().toString());
                            mListener.onErrorOccured(ErrorCodes.NETWORK_ISSUE);
                        } else {
                            Place place = places.get(0);
                            if (mListener != null) {
                                mListener.onPlaceSelected(place);
                            } else {
                                throw new NullPointerException("IOnPlaceSelectedListener not initialised");
                            }
                        }
                    }
                });
            }
        });
        mViewModel = new PlaceAutoCompleteViewModel(getActivity(), getContext(), mBinding, mGoogleApiClient, mAdapter, mListener);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initViewModel();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        mViewModel.clearCache();
        mViewModel.freeReferences();
        mViewModel = null;
        super.onDestroy();
    }

    public interface IOnPlaceSelectedListener {
        void onPlaceSelected(Place place);
        void onErrorOccured(int errorCode);
    }
}
