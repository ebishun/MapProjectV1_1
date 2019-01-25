package com.promosys.mapprojectv1_1;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fimrware 2 on 4/6/2017.
 */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.MyViewHolder>
                             implements Filterable {

    private ArrayList<LocationObject> locationList;
    private static MyClickListener myClickListener;
    private static MyClickListener2 myClickListener2;
    private int scrolledPosition;
    CustomFilterLocation filter;

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements
            View.OnLongClickListener,
            View.OnClickListener{
        public TextView locName,locLatitude,locLongitude;
        public LinearLayout btnEdit,btnLocation;
        public MainActivity mainActivity;
        public CardView cardLocation;

        public MyViewHolder(View view) {
            super(view);
            mainActivity = (MainActivity)view.getContext();
            locName = (TextView)view.findViewById(R.id.txt_name);
            locLatitude = (TextView)view.findViewById(R.id.txt_latitude);
            locLongitude = (TextView)view.findViewById(R.id.txt_longitude);

            btnEdit = (LinearLayout)view.findViewById(R.id.btn_edit_location);
            btnLocation = (LinearLayout)view.findViewById(R.id.btn_go_location);
            cardLocation = (CardView)view.findViewById(R.id.card_view);

            view.setOnLongClickListener(this);
            view.setOnClickListener(this);

        }


        @Override
        public boolean onLongClick(View view) {
            myClickListener.onItemLongClick(getAdapterPosition(), view);
            return false;
        }

        @Override
        public void onClick(View v) {
            myClickListener2.onItemClick(getAdapterPosition(), v);
        }
    }


    public LocationAdapter(ArrayList<LocationObject> locationList) {
        this.locationList = locationList;
    }

    public void setOnItemLongClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public void setOnItemClickListener(MyClickListener2 myClickListener2) {
        this.myClickListener2 = myClickListener2;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_name_list, parent, false);
        //.inflate(R.layout.monitor_list_row2, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        LocationObject locationObject = locationList.get(position);

        final String latitude = locationObject.getLocLatitude();
        final String longitude = locationObject.getLocLongitude();

        holder.locName.setText(locationObject.getLocName());
        holder.locLatitude.setText("Latitude: " + latitude);
        holder.locLongitude.setText("Longitude: " + longitude);

        holder.btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Location","Location Clicked");
                holder.mainActivity.goToLocation(latitude,longitude);
            }
        });

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mainActivity.intEditLocationPosition = position;
                holder.mainActivity.openEditDialog();
            }
        });

        if (locationObject.getIsClicked().equals("isClicked")){
            holder.cardLocation.setCardBackgroundColor(holder.mainActivity.getResources().getColor(R.color.card_is_clicked));
        }else {
            holder.cardLocation.setCardBackgroundColor(Color.WHITE);
        }

    }

    public ArrayList<LocationObject> getFilteredList(){
        return  locationList;
    }

    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter=new CustomFilterLocation(locationList,this);
        }
        return filter;
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public interface MyClickListener {
        public void onItemLongClick(int position, View v);
    }

    public interface MyClickListener2 {
        public void onItemClick(int position, View v);
    }

    public void clear() {
        locationList.clear();
        notifyDataSetChanged();

    }

    public void setPosition(int position){
        this.scrolledPosition = position;
    }

    public int getPosition(){
        return this.scrolledPosition;
    }
}
