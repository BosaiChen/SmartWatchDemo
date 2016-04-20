package com.thefuture.smartwatchdemo.trustwifi;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.thefuture.smartwatchdemo.FindMyPhone.FindPhoneService;
import com.thefuture.smartwatchdemo.R;
import com.thefuture.smartwatchdemo.trustwifi.ScanWifiFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

public class ScanWifiRecyclerViewAdapter extends RecyclerView.Adapter<ScanWifiRecyclerViewAdapter.ViewHolder> {

    private final List<WifiInfoItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ScanWifiRecyclerViewAdapter(List<WifiInfoItem> wifiItems, OnListFragmentInteractionListener listener) {
        mValues = wifiItems;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_scanwifi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mWifiNameView.setText(mValues.get(position).displayName);
        holder.mWifiAPAddress.setText(mValues.get(position).bssID);
        holder.mWifiCheckboxView.setChecked(mValues.get(position).trust);

        holder.mWifiCheckboxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TrustWifiDbHelper.updateWifi(buttonView.getContext(), holder.mItem.bssID, isChecked);
                List<WifiInfoItem> wifis = new ArrayList<>();
                holder.mItem.trust = isChecked;
                wifis.add(holder.mItem);
                FindPhoneService.sendMsgToUpdateTrustWifi(buttonView.getContext(), TrustWifiDbHelper.convertWifisToJSON(buttonView.getContext(), wifis));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mWifiNameView;
        public final TextView mWifiAPAddress;
        public final AppCompatCheckBox mWifiCheckboxView;
        public WifiInfoItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mWifiNameView = (TextView) view.findViewById(R.id.wifi_name);
            mWifiAPAddress = (TextView) view.findViewById(R.id.wifi_access_point_address);
            mWifiCheckboxView = (AppCompatCheckBox) view.findViewById(R.id.wifi_checkbox);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mWifiCheckboxView.getText() + "'";
        }
    }
}
