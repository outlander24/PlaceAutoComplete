package com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.deprecated;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.deprecated.PlaceData;

/**
 * Created by ashish on 04/02/16.
 */
public class PlaceDataItemViewModel extends BaseObservable {

    public PlaceData placeData;
    private AddressItemViewEventListener mAddressItemViewEventListener;

    public PlaceDataItemViewModel(PlaceData placeData, AddressItemViewEventListener addressItemViewEventListener) {
        this.placeData = placeData;
        mAddressItemViewEventListener = addressItemViewEventListener;
    }

    @Bindable
    public View.OnClickListener getOnPlaceSelected() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddressItemViewEventListener != null) {
                    mAddressItemViewEventListener.onPlaceSelected(placeData);
                }
            }
        };
    }

    public interface AddressItemViewEventListener {
        void onPlaceSelected(PlaceData placeData);
    }

}
