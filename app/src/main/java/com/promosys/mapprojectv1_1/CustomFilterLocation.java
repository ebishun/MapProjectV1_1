package com.promosys.mapprojectv1_1;

import android.util.Log;
import android.widget.Filter;

import java.util.ArrayList;


public class CustomFilterLocation extends Filter {
    public static final String TAG = "filterLocationList";
    LocationAdapter adapter;
    ArrayList<LocationObject> filterList;
    ArrayList filteredPosition;
    int position;

    public CustomFilterLocation(ArrayList<LocationObject> filterList, LocationAdapter adapter)
    {
        this.adapter=adapter;
        this.filterList=filterList;
    }
    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<LocationObject> filteredData = new ArrayList<>();
            filteredPosition = new ArrayList();
            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                String filterName = filterList.get(i).getLocName().toUpperCase().substring(0,1);
                Log.i(TAG,"filterName: "+filterName);
                if(filterList.get(i).getLocName().toUpperCase().contains(constraint))
                //if(filterName.contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredData.add(filterList.get(i));
                    filteredPosition.add(i);
                    position = i;
                }
            }
            results.count=filteredData.size();
            results.values=filteredData;
        }else
        {
            results.count=filterList.size();
            results.values=filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //REFRESH
        if(filteredPosition.isEmpty()){
            Log.i(TAG, "empty list");
        }else {
            Log.i(TAG, "position: " + filteredPosition.size());
            int pose = Integer.parseInt(filteredPosition.get(0).toString());
            adapter.setPosition(pose);
        }

    }
}
