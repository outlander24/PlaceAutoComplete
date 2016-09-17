package com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.library.outlander.placeautocomplete.AutoCompletePlaceFragment.viewModels.PlaceAutoCompleteViewModel;
import com.library.outlander.placeautocomplete.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aashish on 9/17/16.
 */
public class PlaceAutoCompleteResultsAdapter extends RecyclerView.Adapter<PlaceAutoCompleteResultsAdapter.MyViewHolder> {

    private List<PlaceAutoCompleteViewModel.PlaceAutocomplete> mResults;
    private OnResultItemClickListener mActivityListener;

    public PlaceAutoCompleteResultsAdapter(List<PlaceAutoCompleteViewModel.PlaceAutocomplete> results, OnResultItemClickListener listener) {
        if (mResults == null) {
            mResults = new ArrayList<>();
        }
        mActivityListener = listener;
        mResults = results;
    }

    public void setResults(List<PlaceAutoCompleteViewModel.PlaceAutocomplete> results) {
        if (mResults == null) {
            mResults = new ArrayList<>();
        }
        mResults.clear();
        if (results != null) {
            for (PlaceAutoCompleteViewModel.PlaceAutocomplete placeAutocomplete : results) {
                mResults.add(placeAutocomplete);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_autocomplete_result_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.mTvTitle.setText(mResults.get(position).title);
        holder.mTvDescription.setText(mResults.get(position).description);
        holder.mMainContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivityListener.onItemClicked(mResults.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mResults != null) {
            return mResults.size();
        }
        return 0;
    }

    protected class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mTvTitle;
        public TextView mTvDescription;
        public RelativeLayout mMainContainer;

        public MyViewHolder(View itemView) {
            super(itemView);
            mMainContainer = (RelativeLayout) itemView.findViewById(R.id.result_item_main_container);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mTvDescription = (TextView) itemView.findViewById(R.id.tv_description);
        }
    }

    public interface OnResultItemClickListener {
        void onItemClicked(PlaceAutoCompleteViewModel.PlaceAutocomplete placeAutocomplete);
    }
}
