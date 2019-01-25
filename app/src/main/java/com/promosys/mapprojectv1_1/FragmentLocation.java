package com.promosys.mapprojectv1_1;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Fimrware 2 on 4/6/2017.
 */

public class FragmentLocation extends Fragment {

    private View rootView;
    private Context context;
    private MainActivity mainActivity;

    private SearchView sv;

    private RecyclerView locationRecyclerView;
    public ArrayList<LocationObject> locationList,backgroundLocationList,planTravelList;

    private RecyclerView.LayoutManager mLayoutManager;
    public LocationAdapter locationAdapter;

    public Button btnViewJourney;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_fragment_name, container, false);
        context = rootView.getContext();
        mainActivity = (MainActivity)context;

        btnViewJourney = (Button)rootView.findViewById(R.id.btn_view_journey);
        btnViewJourney.setVisibility(View.GONE);
        btnViewJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.viewMyTravelActivity();
            }
        });

        initRecyclerView();
        getLocationList();

        return rootView;
    }

    /*
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        sv= (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (sv != null) {
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    locationAdapter.getFilter().filter(query);
                    locationRecyclerView.scrollToPosition(locationAdapter.getPosition());

                    return false;
                }
                @Override
                public boolean onQueryTextChange(String query) {
                    //FILTER AS YOU TYPE
                    locationAdapter.getFilter().filter(query);
                    Log.i("filterLocationList","onQueryTextChange: " + query);
                    locationRecyclerView.scrollToPosition(locationAdapter.getPosition());
                    //mRecyclerView.scrollToPosition(30);
                    return false;
                }

            });
        }
        super.onCreateOptionsMenu(menu, inflater);

    }
    */

    private void initRecyclerView(){
        locationRecyclerView = (RecyclerView) rootView.findViewById(R.id.recview_name);
        locationRecyclerView.setHasFixedSize(true);
        //mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager = new GridLayoutManager(context,2);
        locationRecyclerView.setLayoutManager(mLayoutManager);
        locationList = new ArrayList<LocationObject>();
        planTravelList = new ArrayList<LocationObject>();
        backgroundLocationList = new ArrayList<LocationObject>();

        locationAdapter = new LocationAdapter(locationList);
        locationRecyclerView.setAdapter(locationAdapter);

        locationAdapter.setOnItemLongClickListener(new LocationAdapter.MyClickListener() {
            @Override
            public void onItemLongClick(int position, View v) {
                Log.i("Location","LongClicked: " + locationList.get(position).getLocName());
                deleteLocationDialog(position);
            }
        });

        locationAdapter.setOnItemClickListener(new LocationAdapter.MyClickListener2() {
            @Override
            public void onItemClick(int position, View v) {
                if (mainActivity.isPlanMyTravel){
                    String locationName = locationList.get(position).getLocName();
                    String locationLat = locationList.get(position).getLocLatitude();
                    String locationLongitude = locationList.get(position).getLocLongitude();

                    locationList.get(position).setIsClicked("isClicked");
                    locationAdapter.notifyDataSetChanged();

                    LocationObject locationObject = new LocationObject("PlanLocation",locationName,locationLat,locationLongitude,"isClicked");
                    planTravelList.add(locationObject);

                    mainActivity.savePlanMyTravelList();

                    //Toast.makeText(context,"Location Is Added",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void refreshClickedList(){
        for (int i = 0;i<locationList.size();i++){
            if (locationList.get(i).getIsClicked().equals("isClicked")){
                locationList.get(i).setIsClicked("notClicked");
            }
        }

        locationAdapter.notifyDataSetChanged();
        btnViewJourney.setVisibility(View.GONE);
    }

    public void addLocation(){
        final Dialog openDialog = new Dialog(context);
        openDialog.setContentView(R.layout.layout_add_location_dialog);
        openDialog.setTitle("Add Location");

        final EditText addLocName = (EditText)openDialog.findViewById(R.id.edt_add_loc_name);
        final EditText addLocLatitude = (EditText)openDialog.findViewById(R.id.edt_add_loc_latitude);
        final EditText addLocLongitude = (EditText)openDialog.findViewById(R.id.edt_add_loc_longitude);

        Button dialogGetLocation = (Button)openDialog.findViewById(R.id.btn_get_location);
        dialogGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mainActivity.startLocationConnection();
                addLocLatitude.setText(mainActivity.strLatitude);
                addLocLongitude.setText(mainActivity.strLongitude);
            }
        });

        Button dialogAddLocationBtn = (Button)openDialog.findViewById(R.id.btn_add_site);
        dialogAddLocationBtn.setText("Add Location");
        dialogAddLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String locName = addLocName.getText().toString();
                String locLatitude = addLocLatitude.getText().toString();
                String locLongitude = addLocLongitude.getText().toString();
                String locGroupName = mainActivity.strGroupName;

                LocationObject locObject = new LocationObject(locGroupName,locName,locLatitude,locLongitude,"notClicked");

                backgroundLocationList.add(locObject);
                locationList.add(locObject);
                locationAdapter.notifyDataSetChanged();

                mainActivity.saveLocationList();
                //mainActivity.stopLocationConnection();
                openDialog.dismiss();
            }
        });

        Button dialogCancel = (Button)openDialog.findViewById(R.id.btn_cancel);
        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog.dismiss();
            }
        });

        openDialog.show();
    }

    public void editLocation(){
        final int position = mainActivity.intEditLocationPosition;

        final Dialog openDialog = new Dialog(context);
        openDialog.setContentView(R.layout.layout_add_location_dialog);
        openDialog.setTitle("Edit Location");

        final EditText edtLocName = (EditText)openDialog.findViewById(R.id.edt_add_loc_name);
        final EditText edtLocLatitude = (EditText)openDialog.findViewById(R.id.edt_add_loc_latitude);
        final EditText edtLocLongitude = (EditText)openDialog.findViewById(R.id.edt_add_loc_longitude);

        edtLocName.setText(locationList.get(position).getLocName());
        edtLocLatitude.setText(locationList.get(position).getLocLatitude());
        edtLocLongitude.setText(locationList.get(position).getLocLongitude());

        Button dialogGetLocation = (Button)openDialog.findViewById(R.id.btn_get_location);
        dialogGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mainActivity.startLocationConnection();
                edtLocLatitude.setText(mainActivity.strLatitude);
                edtLocLongitude.setText(mainActivity.strLongitude);
            }
        });

        Button dialogEditLocationBtn = (Button)openDialog.findViewById(R.id.btn_add_site);
        dialogEditLocationBtn.setText("Edit Site");
        dialogEditLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String locName = edtLocName.getText().toString();
                String locLatitude = edtLocLatitude.getText().toString();
                String locLongitude = edtLocLongitude.getText().toString();
                String locGroupName = mainActivity.strGroupName;

                for (int i=0;i<backgroundLocationList.size();i++){
                    String editLocGroup = backgroundLocationList.get(i).getLocGroup();
                    if (editLocGroup.equals(locGroupName)){
                        String editLocName = backgroundLocationList.get(i).getLocName();
                        if (editLocName.equals(locationList.get(position).getLocName())){
                            backgroundLocationList.get(i).setLocName(locName);
                            backgroundLocationList.get(i).setLocLatitude(locLatitude);
                            backgroundLocationList.get(i).setLocLongitude(locLongitude);
                        }
                    }
                }

                locationList.get(position).setLocName(locName);
                locationList.get(position).setLocLatitude(locLatitude);
                locationList.get(position).setLocLongitude(locLongitude);

                locationAdapter.notifyDataSetChanged();

                mainActivity.saveLocationList();
                openDialog.dismiss();
            }

        });

        Button dialogCancel = (Button)openDialog.findViewById(R.id.btn_cancel);
        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog.dismiss();
            }
        });
        openDialog.show();
    }

    public void deleteLocationDialog(final int position){
        final Dialog openDialog = new Dialog(context);
        openDialog.setContentView(R.layout.layout_add_location_dialog);
        openDialog.setTitle("Delete Location");

        final EditText delLocName = (EditText)openDialog.findViewById(R.id.edt_add_loc_name);
        final EditText delLocLatitude = (EditText)openDialog.findViewById(R.id.edt_add_loc_latitude);
        final EditText delLocLongitude = (EditText)openDialog.findViewById(R.id.edt_add_loc_longitude);

        delLocName.setText(locationList.get(position).getLocName());
        delLocLatitude.setText(locationList.get(position).getLocLatitude());
        delLocLongitude.setText(locationList.get(position).getLocLongitude());

        delLocName.setEnabled(false);
        delLocLatitude.setEnabled(false);
        delLocLongitude.setEnabled(false);

        final String locGroupName = locationList.get(position).getLocGroup();

        Button dialogGetLocation = (Button)openDialog.findViewById(R.id.btn_get_location);
        dialogGetLocation.setVisibility(View.INVISIBLE);

        Button dialogDElLocationBtn = (Button)openDialog.findViewById(R.id.btn_add_site);
        dialogDElLocationBtn.setText("Delete Location");
        dialogDElLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                for (int i=0;i<backgroundLocationList.size();i++){
                    String editLocGroup = backgroundLocationList.get(i).getLocGroup();
                    if (editLocGroup.equals(locGroupName)){
                        String editLocName = backgroundLocationList.get(i).getLocName();

                        if (editLocName.equals(locationList.get(position).getLocName())){
                            backgroundLocationList.remove(i);
                            break;
                        }

                    }
                }

                locationList.remove(position);

                locationAdapter.notifyDataSetChanged();
                mainActivity.saveLocationList();
                //mainActivity.stopLocationConnection();
                openDialog.dismiss();
            }
        });

        Button dialogCancel = (Button)openDialog.findViewById(R.id.btn_cancel);
        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog.dismiss();
            }
        });

        openDialog.show();

    }

    public void getLocationList(){
        String jsonLocation = mainActivity.getLocationList();
        if (!(jsonLocation.isEmpty())){

            try {
                JSONArray jsonArray = new JSONArray(jsonLocation);
                for (int i = 0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String groupName = jsonObject.getString("locGroup");
                    String name = jsonObject.getString("locName");
                    String latitude = jsonObject.getString("locLatitude");
                    String longitude = jsonObject.getString("locLongitude");

                    LocationObject locationObject = new LocationObject(groupName,name,latitude,longitude,"notClicked");
                    backgroundLocationList.add(locationObject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            filterLocationList();
        }
    }


    public void filterLocationList(){
        for (int i = 0;i<backgroundLocationList.size();i++){
            String groupName = backgroundLocationList.get(i).getLocGroup();
            if (groupName.equals(mainActivity.strGroupName)){
                String name = backgroundLocationList.get(i).getLocName();
                String latitude = backgroundLocationList.get(i).getLocLatitude();
                String longitude = backgroundLocationList.get(i).getLocLongitude();

                LocationObject locationObject = new LocationObject(groupName,name,latitude,longitude,"notClicked");
                locationList.add(locationObject);
                locationAdapter.notifyDataSetChanged();
            }

        }
    }

    public void editGroup(String oldGroup,String newGroup){
        for (int i = 0;i<backgroundLocationList.size();i++){
            String groupName = backgroundLocationList.get(i).getLocGroup();
            if (groupName.equals(oldGroup)){
                backgroundLocationList.get(i).setLocGroup(newGroup);
            }
        }
        mainActivity.saveLocationList();
    }

    public void deleteGroupLocation(String deletedGroupName){
        ArrayList<LocationObject> tempLocationList = new ArrayList<LocationObject>();

        for (int i=0;i<backgroundLocationList.size();i++){
            String groupName = backgroundLocationList.get(i).getLocGroup();
            if (!(groupName.equals(deletedGroupName))){
                String locationName = backgroundLocationList.get(i).getLocName();
                String locationLatitude = backgroundLocationList.get(i).getLocLatitude();
                String locationLongitude = backgroundLocationList.get(i).getLocLongitude();

                LocationObject locationObject = new LocationObject(groupName,locationName,locationLatitude,locationLongitude,"notClicked");
                tempLocationList.add(locationObject);
            }
        }

        backgroundLocationList.clear();

        for (int i = 0;i<tempLocationList.size();i++){
            String tempGroupName = tempLocationList.get(i).getLocGroup();
            String tempLocName = tempLocationList.get(i).getLocName();
            String tempLocLatitude = tempLocationList.get(i).getLocLatitude();
            String tempLocLongitude = tempLocationList.get(i).getLocLongitude();

            LocationObject locationObject = new LocationObject(tempGroupName,tempLocName,tempLocLatitude,tempLocLongitude,"notClicked");
            backgroundLocationList.add(locationObject);

        }
        mainActivity.saveLocationList();

    }


}
