package com.library.outlander.placeautocomplete.AutoCompletePlaceFragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.library.outlander.placeautocomplete.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashish on 03/02/16.
 */
public class PlaceAutoCompleteFragment extends Fragment{

    private EditText mSearchBox;
    private ImageView mBtnClearSearch;
    private ImageView mSearchIcon;
    private LinearLayout mContainer;
    private View mBlackOutView;
    private RecyclerView mSearchResults;
    private MySearchResultsAdapter mAdapter;
    private List<PlaceData> mPlaceDataList = new ArrayList<>();
    private PlaceDataItemViewModel.AddressItemViewEventListener mAddressItemViewEventListener;
    private ProgressBar mProgressBar;
    private boolean mShowAnimation, mStopListeningToTextChanges, mShowFullScreen;

    private IOnPlaceSelectedListener mOnPlaceSelectedListener;

    private static final String KEY_SHOW_ANIMATION = "key_show_animation";
    private static final String KEY_SHOW_FULLSCREEN = "key_show_fullscreen";

    /**
     * Get an instance of the fragment to use.
     * @param enableAnimations  enable search bar animations
     * @param isFullScreen  show the fragment full screen or as an overlay over some other fragment/activity-UI
     * @return  Instance of this fragment which can be added to the fragment container
     */
    public static PlaceAutoCompleteFragment getInstance(Context context, boolean enableAnimations, boolean  isFullScreen) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_SHOW_ANIMATION, enableAnimations);
        args.putBoolean(KEY_SHOW_FULLSCREEN, isFullScreen);
        PlaceAutoCompleteFragment placeAutoCompleteFragment = (PlaceAutoCompleteFragment) Fragment.instantiate(context,
                PlaceAutoCompleteFragment.class.getName(), args);

        return placeAutoCompleteFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShowAnimation = getArguments().getBoolean(KEY_SHOW_ANIMATION);
            mShowFullScreen = getArguments().getBoolean(KEY_SHOW_FULLSCREEN);
        } else {
            mShowAnimation = true;
            mShowFullScreen = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_auto_complete, container, false);

        mContainer = (LinearLayout) view.findViewById(R.id.container);
        mBlackOutView = view.findViewById(R.id.blackout_view);
        mBlackOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFragment();
            }
        });

        mSearchBox = (EditText) view.findViewById(R.id.search_box);
        addTextWatcherOnSearchBox();
        initAddressItemViewEvenListener();
        showSoftKeyboard(mSearchBox);

        mSearchIcon = (ImageView) view.findViewById(R.id.search_icon);

        mSearchResults = (RecyclerView) view.findViewById(R.id.search_results);

        mProgressBar = (ProgressBar) view.findViewById(R.id.place_autocomplete_progress);

        mBtnClearSearch = (ImageView) view.findViewById(R.id.clear_search);
        mBtnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchBox.setText("");
                mPlaceDataList.clear();
                updateRecyclerViewHeightBasedOnNumberOfAddresses();
            }
        });

        if (mShowAnimation) {
            Animation slideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_top);
            slideAnimation.setInterpolator(new BounceInterpolator());
            mContainer.startAnimation(slideAnimation);
            mBlackOutView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
            mSearchIcon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.bounce_vertically));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapter();
    }

    private void initAddressItemViewEvenListener() {
        mAddressItemViewEventListener = new PlaceDataItemViewModel.AddressItemViewEventListener() {
            @Override
            public void onPlaceSelected(PlaceData placeData) {
                if (mOnPlaceSelectedListener != null) {
                    mOnPlaceSelectedListener.onPlaceSelected(placeData);
                    mStopListeningToTextChanges = true;
                    mSearchBox.setText(placeData.name);
                    mPlaceDataList.clear();
                    updateRecyclerViewHeightBasedOnNumberOfAddresses();
                    removeFragment();
                }
            }
        };
    }

    private void removeFragment() {
        if (mShowAnimation) {
            Animation slideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_top);
            mContainer.startAnimation(slideAnimation);
            mBlackOutView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
            slideAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    hideKeyBoardAndGoBack();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            hideKeyBoardAndGoBack();

        }
    }

    private void hideKeyBoardAndGoBack() {
        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        getActivity().onBackPressed();
    }

    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.toggleSoftInput(0, 0);
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new MySearchResultsAdapter(mPlaceDataList, mAddressItemViewEventListener);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mSearchResults.setLayoutManager(layoutManager);
        mSearchResults.setAdapter(mAdapter);
        updateRecyclerViewHeightBasedOnNumberOfAddresses();
    }

    private void updateRecyclerViewHeightBasedOnNumberOfAddresses() {

        if (isVisible()) {
            int itemHeight = (int) (TypedValue.applyDimension
                    (TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics()));
            int totalHeight = itemHeight * mPlaceDataList.size();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int screenHeight = displaymetrics.heightPixels;
            if (totalHeight > screenHeight - itemHeight) {
                mSearchResults.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                mSearchResults.getLayoutParams().height = totalHeight;
            }
            mSearchResults.requestLayout();
        }
    }

    private void updateSearchResults(List<PlaceData> placeDataList) {
        mAdapter.updateAddressDataList(placeDataList);
        updateRecyclerViewHeightBasedOnNumberOfAddresses();
    }

    private void addTextWatcherOnSearchBox() {
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                if (query.trim().length() > 0 && !mStopListeningToTextChanges) {
                    mBtnClearSearch.setVisibility(View.VISIBLE);
                    query = query.replace(' ', '+');
                    startMyTask(new FindPlace(), query);
                } else {
                    mBtnClearSearch.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mBtnClearSearch.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mBtnClearSearch.setVisibility(View.VISIBLE);
    }

    private void parseResultsJson(JSONObject resultsJsonObject) {
        if (resultsJsonObject != null) {
            try {
                if (resultsJsonObject.getString(Constants.STATUS).equalsIgnoreCase(Constants.OK)) {
                    mPlaceDataList.clear();

                    JSONArray resultsArray = resultsJsonObject.getJSONArray(Constants.RESULTS);
                    for (int i = 0; i < resultsArray.length(); i++) {

                        JSONObject addressObject = resultsArray.getJSONObject(i);
                        JSONArray addressComponents = addressObject.getJSONArray(Constants.ADDRESS_COMPONENTS);
                        PlaceData placeData = new PlaceData();
                        placeData.name = addressComponents.getJSONObject(0).getString(Constants.LONG_NAME);
                        placeData.formattedAddress = addressObject.getString(Constants.FORMATTED_ADDRESS);
                        placeData.placeId = addressObject.getString(Constants.PLACE_ID);
                        placeData.latitude = addressObject.getJSONObject(Constants.GEOMETRY).
                                getJSONObject(Constants.LOCATION).getDouble(Constants.LAT);
                        placeData.longitude = addressObject.getJSONObject(Constants.GEOMETRY).getJSONObject(Constants.LOCATION)
                                .getDouble(Constants.LONG);

                        mPlaceDataList.add(placeData);
                    }

                    updateSearchResults(mPlaceDataList);
                } else if (resultsJsonObject.getString(Constants.STATUS).equalsIgnoreCase(Constants.ZERO_RESULTS)) {
                    mOnPlaceSelectedListener.onErrorOccurred(ErrorCodes.ZERO_RESULTS);
                } else {
                    mOnPlaceSelectedListener.onErrorOccurred(ErrorCodes.NETWORK_ISSUE);
                }

            } catch (JSONException je) {
                je.printStackTrace();
            }
        } else {
            if (mOnPlaceSelectedListener != null) {
                mOnPlaceSelectedListener.onErrorOccurred(ErrorCodes.NETWORK_ISSUE);
            }

        }
    }

    public void addOnPlaceSelectedListener(IOnPlaceSelectedListener placeSelectedListener) {
        mOnPlaceSelectedListener = placeSelectedListener;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    private void startMyTask(AsyncTask asyncTask, Object params) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

    public interface IOnPlaceSelectedListener {
        void onPlaceSelected(PlaceData placeData);

        /***
         * Called when the API returns with some error.
         * @param errorCode error code, use this to show a proper message to users.
         *                  0 - network issue
         *                  1 - zero results
         */
        void onErrorOccurred(int errorCode);
    }

    @Override
    public void onDestroyView() {
        if (mShowAnimation) {
            Animation slideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_top);
            mContainer.startAnimation(slideAnimation);
            mBlackOutView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
        }
        super.onDestroyView();
    }

    private class FindPlace extends AsyncTask<Object, Void, JSONObject> {

        String params;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        protected JSONObject doInBackground(Object... params) {
            this.params = params[0].toString().replace("+", " ");
            String uri = Constants.URL + params[0];
            HttpGet httpGet = new HttpGet(uri);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
                return jsonObject;
            } catch (JSONException e) {
                return null;
            }
        }

        protected void onPostExecute(JSONObject result) {
            if (params.equalsIgnoreCase(mSearchBox.getText().toString().trim())) {
                hideProgressBar();
            }
            parseResultsJson(result);
        }
    }
}
