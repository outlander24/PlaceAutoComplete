package com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.deprecated;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.library.outlander.placeautocomplete.R;
import com.library.outlander.placeautocomplete.databinding.AutoCompleteAddressItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashish on 04/02/16.
 */
public class MySearchResultsAdapter extends RecyclerView.Adapter<MySearchResultsAdapter.BindingHolder> {

    private List<PlaceData> mPlaceData = new ArrayList<>();
    private PlaceDataItemViewModel.AddressItemViewEventListener mAddressItemViewEventListener;

    public MySearchResultsAdapter(List<PlaceData> placeDataList, PlaceDataItemViewModel.AddressItemViewEventListener addressItemViewEventListener) {
        if (placeDataList != null) {
            mPlaceData.clear();
            mPlaceData = placeDataList;
        }
        mAddressItemViewEventListener = addressItemViewEventListener;
    }

    public void updateAddressDataList(List<PlaceData> placeDatas) {
        if (placeDatas != null) {
            notifyDataSetChanged();
        }
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AutoCompleteAddressItemBinding autoCompleteAddressItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.auto_complete_address_item,
                parent,
                false);

        return new BindingHolder(autoCompleteAddressItemBinding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        AutoCompleteAddressItemBinding autoCompleteAddressItemBinding = holder.mAutoCompleteAddressItemBinding;
        // TODO: 04/02/16 Set View Model to it.
        autoCompleteAddressItemBinding.setViewModel(new PlaceDataItemViewModel(mPlaceData.get(position), mAddressItemViewEventListener));
    }

    @Override
    public int getItemCount() {
        return mPlaceData.size();
    }

    public class BindingHolder extends RecyclerView.ViewHolder {
        private AutoCompleteAddressItemBinding mAutoCompleteAddressItemBinding;

        public BindingHolder(AutoCompleteAddressItemBinding autoCompleteAddressItemBinding) {
            super(autoCompleteAddressItemBinding.getRoot());
            mAutoCompleteAddressItemBinding = autoCompleteAddressItemBinding;
        }
    }
}
