package com.stiliyana.beaconproject.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stiliyana.beaconproject.R;

import java.text.DecimalFormat;
import java.util.List;

public class BeaconsRecyclerViewAdapter extends RecyclerView.Adapter<BeaconsRecyclerViewAdapter.BeaconViewHolder> {
    List<MyBeacon> beaconList;

    public BeaconsRecyclerViewAdapter(List<MyBeacon> beacons) {
        this.beaconList = beacons;
    }

    @Override
    public int getItemCount() {
        return beaconList.size();
    }

    @Override
    public BeaconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_item_row, parent, false);
        BeaconViewHolder beaconViewHolder = new BeaconViewHolder(v);
        return beaconViewHolder;
    }

    @Override
    public void onBindViewHolder(BeaconViewHolder holder, int position) {
        holder.itemName.setText(beaconList.get(position).getBeacon().getBluetoothName());
        holder.itemAddress.setText("Address: " + beaconList.get(position).getBeacon().getBluetoothAddress());
        holder.itemDescription.setText("TX: " + beaconList.get(position).getBeacon().getTxPower() + "");
        holder.itemDistance.setText(new DecimalFormat("##.##").format(beaconList.get(position).getBeacon().getDistance()) + "m");
        holder.itemDate.setText(beaconList.get(position).getDateTime());
    }

    public static class BeaconViewHolder extends RecyclerView.ViewHolder {

        TextView itemName;
        TextView itemAddress;
        TextView itemDescription;
        TextView itemDistance;
        TextView itemDate;
        ImageView photo;

        BeaconViewHolder(View itemView) {
            super(itemView);
            itemName = (TextView) itemView.findViewById(R.id.item_name);
            itemAddress = (TextView) itemView.findViewById(R.id.item_address);
            itemDescription = (TextView) itemView.findViewById(R.id.item_description);
            itemDistance = (TextView) itemView.findViewById(R.id.item_distance);
            itemDate = (TextView) itemView.findViewById(R.id.item_date);
            photo = (ImageView) itemView.findViewById(R.id.item_image);
        }
    }
}
