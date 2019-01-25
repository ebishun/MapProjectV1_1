package com.promosys.mapprojectv1_1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import com.aditya.filebrowser.Constants;
//import com.aditya.filebrowser.FileChooser;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;

    public FragmentGroup fragmentGroup;
    public FragmentLocation fragmentLocation;
    FragmentTransaction transaction;

    private Toolbar toolbar;

    public boolean isFragmentLocation = false;
    public boolean isFragmentGroup = false;

    public String strGroupName = "";

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    int updates;

    public String strLongitude,strLatitude;
    public int intEditLocationPosition;

    public SearchView sv;

    public static final int RequestPermissionCode = 1;
    public static final int RequestPermissionCode2 = 2;
    public static final int RequestPermissionCode3 = 3;


    /************Saving backup variables***********/
    CSVWriter writer;
    String baseDir;
    String fileName;
    String filePath;
    File f;

    String importFileName = "";


    //Timer variables
    private long timeElapsed;
    private final long startTime = 10000;
    private final long interval = 1000;
    public EnableLocationTimer enableLocationTimer;


    public ArrayList<GroupObject> groupFBList,tempGroupList;
    public ArrayList<LocationObject> tempLocationList;

    public boolean isPlanMyTravel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("mapLocationProject", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        enableLocationTimer = new EnableLocationTimer(startTime, interval);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("Select Group");

        fragmentGroup = new FragmentGroup();
        fragmentLocation = new FragmentLocation();

        transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.content, fragmentGroup, "1");
        transaction.add(R.id.content, fragmentLocation, "2");
        transaction.hide(fragmentLocation);
        transaction.commit();

        isFragmentGroup = true;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(checkPermission()){
            enableLocationTimer.start();
            startLocationConnection();
        }
        else {
            requestPermission();
        }

        clearPlanMyTravelList();

        //firebaseInit();

    }

    /*
    private void firebaseInit(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("user1");

        groupFBList = new ArrayList<GroupObject>();
        tempGroupList = new ArrayList<GroupObject>();
        tempLocationList = new ArrayList<LocationObject>();

        tempGroupList.clear();
        groupFBList.clear();
        tempLocationList.clear();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = snapshotIterator.iterator();

                LocationObject locationObject2 = dataSnapshot.getValue(LocationObject.class);
                Log.i("EditLocation","LocName: " + locationObject2.getLocName());

                while (iterator.hasNext()){
                    LocationObject locationObject = iterator.next().getValue(LocationObject.class);
                    //Log.i("EditLocation","locLocationName: " + locationObject.getLocName() + "\n"
                                        //+"locGroup: " + locationObject.getLocGroup());
                    GroupObject groupObject = new GroupObject(locationObject.getLocGroup());
                    tempGroupList.add(groupObject);
                    tempLocationList.add(locationObject);
                }

                filterDatabaseGroup();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void filterDatabaseGroup(){
        groupFBList.clear();
        for (int i = 0;i<tempGroupList.size();i++){
            String tempGroupName = tempGroupList.get(i).groupName;
            //Log.i("ReadFromFirebase","tempGroupName: " + tempGroupName);
            boolean isDuplicate = false;
            if (groupFBList.size()<1){
                GroupObject groupObject = new GroupObject(tempGroupName);
                groupFBList.add(groupObject);
            }else {
                for (int j=0;j<groupFBList.size();j++){
                    String dbGroupName = groupFBList.get(j).groupName;

                    if (tempGroupName.equals(dbGroupName)){
                        isDuplicate = true;
                        break;
                    }

                }

                if (!isDuplicate){
                    GroupObject groupObject = new GroupObject(tempGroupName);
                    groupFBList.add(groupObject);
                }

            }

        }

        if (fragmentGroup != null){
            fragmentGroup.getGroupListFromDatabase();
        }

        for (int i=0;i<groupFBList.size();i++){
            Log.i("ReadFromFirebase","DbGroupName: " + groupFBList.get(i).getGroupName());
        }

        tempGroupList.clear();
        //tempLocationList.clear();

        //final FirebaseDatabase database = FirebaseDatabase.getInstance();


    }
    */


    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            requestPermission();
            return;
        }

        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            requestPermission();
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    public void calculateDistance(Location location){
        mLocation.distanceTo(location);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"Connection Failed",Toast.LENGTH_SHORT).show();
        //Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onStart() {
        super.onStart();
        createBackupFolder();
        //mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("Location","Location: " + location.getLongitude() + "\n" + location.getLatitude());
        strLatitude = String.valueOf(location.getLatitude());
        strLongitude = String.valueOf(location.getLongitude());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        //sv= (SearchView) menu.findItem(R.id.menu_search).getActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.menu_add_group:
                if (isFragmentGroup){
                    fragmentGroup.addGroup();
                }else {
                    Toast.makeText(this,"Please return to group page",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_add_location:
                if (isFragmentLocation){
                    startLocationConnection();
                    enableLocationTimer.start();
                    fragmentLocation.addLocation();
                }else {
                    Toast.makeText(this,"Please select group first",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_backup_data:
                savingBackupDialog();
                //startCreateBackup();
                break;

            case R.id.menu_load_backup_data:
                if (!isFragmentGroup){
                    Toast.makeText(this,"Please return to group page",Toast.LENGTH_SHORT).show();
                }else {
                    startReadBackup();
                }
                break;

            case R.id.menu_map_activity:
                if (isFragmentLocation){
                    isPlanMyTravel = true;
                    Toast.makeText(this,"Plan My Trip mode is on",Toast.LENGTH_SHORT).show();
                    fragmentLocation.btnViewJourney.setVisibility(View.VISIBLE);

                    fragmentLocation.planTravelList.clear();
                    savePlanMyTravelList();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void viewMyTravelActivity(){
        Bundle mBundle = new Bundle();
        mBundle.putString("locLatitude", strLatitude);
        mBundle.putString("locLongitude", strLongitude);

        Intent intent = new Intent(this,MapsActivity.class);
        //Intent intent = new Intent(this,MapsActivity2.class);
        startActivity(intent.putExtras(mBundle));
    }

    @Override
    public void onBackPressed() {
        if (isFragmentLocation){
            if (isPlanMyTravel){
                isPlanMyTravel = false;
                fragmentLocation.refreshClickedList();
            }else {
                changeFragment("FragmentGroup");
            }
        }else {
            finish();
        }
        //super.onBackPressed();
    }

    public void startLocationConnection(){
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            requestPermission();
            return;
        }
        mGoogleApiClient.connect();
    }

    public void stopLocationConnection(){
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void clearPlanMyTravelList(){
        Gson gson = new Gson();

        editor.putString("PlanMyTravelLocation","");
        editor.apply();
    }

    public String getGroupName(){
        String jsonGroup;
        if (sharedPreferences.getString("GroupName","").isEmpty()){
            jsonGroup = "";
        }else {
            jsonGroup = sharedPreferences.getString("GroupName","");
        }
        return jsonGroup;
    }

    public String getLocationList(){
        String jsonGroup;
        if (sharedPreferences.getString("LocationName","").isEmpty()){
            jsonGroup = "";
        }else {
            jsonGroup = sharedPreferences.getString("LocationName","");
        }
        return jsonGroup;
    }

    public void saveGroupList(){
        Gson gson = new Gson();
        String saveToJson = gson.toJson(fragmentGroup.groupList);

        editor.putString("GroupName",saveToJson);
        editor.apply();
    }

    public void saveLocationList(){
        Gson gson = new Gson();
        String saveToJson = gson.toJson(fragmentLocation.backgroundLocationList);

        editor.putString("LocationName",saveToJson);
        editor.apply();
    }

    public void savePlanMyTravelList(){
        Gson gson = new Gson();
        String saveToJson = gson.toJson(fragmentLocation.planTravelList);

        editor.putString("PlanMyTravelLocation",saveToJson);
        editor.apply();
    }

    public void changeFragment(String whichFragment){
        transaction = getFragmentManager().beginTransaction();
        switch (whichFragment){
            case "FragmentGroup":
                isFragmentLocation = false;
                isFragmentGroup = true;

                toolbar.setSubtitle("Select Group");

                transaction.hide(fragmentLocation);
                transaction.show(fragmentGroup);
                transaction.commit();
                break;

            case "FragmentLocation":
                fragmentLocation.locationList.clear();
                fragmentLocation.locationAdapter.notifyDataSetChanged();

                toolbar.setSubtitle("Group: " + strGroupName);

                if (fragmentLocation.backgroundLocationList.isEmpty()){
                    fragmentLocation.getLocationList();
                }else {
                    fragmentLocation.filterLocationList();
                }

                isFragmentGroup = false;
                isFragmentLocation = true;

                transaction.hide(fragmentGroup);
                transaction.show(fragmentLocation);
                transaction.commit();
                break;
        }
    }

    //send intent to Google Map app
    public void goToLocation(String latitude, String longitude){
        String strUri = "http://maps.google.com/maps?daddr="+ latitude+","+longitude;
        Log.i("Location","Location Clicked: " + strUri);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse(strUri));
        startActivity(intent);
    }

    public void openEditDialog(){
        startLocationConnection();
        fragmentLocation.editLocation();
        enableLocationTimer.start();
    }

    public void editGroupLocation(String oldGroup,String newGroup){
        fragmentLocation.editGroup(oldGroup,newGroup);
    }

    public void deleteGroupLocation(String groupName){
        fragmentLocation.deleteGroupLocation(groupName);
    }


    public class EnableLocationTimer extends CountDownTimer
    {

        public EnableLocationTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            enableLocationTimer.cancel();
            stopLocationConnection();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            timeElapsed = startTime - millisUntilFinished;
        }
    }


    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
            {
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,

            }, RequestPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {
                    boolean FineLocationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean CoarseLocationPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (FineLocationPermission && CoarseLocationPermission) {
                        //Toast.makeText(MainActivity.this, "Location Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Location Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case RequestPermissionCode2:
                boolean WriteToStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (WriteToStoragePermission){
                    //Toast.makeText(MainActivity.this, "Permission Granted. Creating backup now..", Toast.LENGTH_SHORT).show();
                    saveBackupToPhone();
                }else {
                    Toast.makeText(MainActivity.this,"Unable to create backup since permission is denied",Toast.LENGTH_LONG).show();
                }
                break;

            case RequestPermissionCode3:
                boolean ReadFromStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (ReadFromStoragePermission){
                    //Toast.makeText(MainActivity.this, "Permission Granted. Reading backup now..", Toast.LENGTH_SHORT).show();
                    //readBackupData(importFileName);
                    readStorageDirectory();
                }else {
                    Toast.makeText(MainActivity.this,"Unable to read backup since permission is denied",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    public void savingBackupDialog(){
        final Dialog openDialog = new Dialog(this);
        openDialog.setContentView(R.layout.layout_backup_dialog);
        openDialog.setTitle("Create Backup");

        final EditText edtBackupName = (EditText)openDialog.findViewById(R.id.edt_filename_title);

        Button dialogCreateBackup = (Button)openDialog.findViewById(R.id.btn_create_backup);
        dialogCreateBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileName = edtBackupName.getText().toString() + ".csv";
                startCreateBackup();
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


    /***************Backup data in excel********************/
    public void createBackupFolder(){
        boolean isGotPermission =  isWriteToStoragePermissionGranted();
        if(isGotPermission){
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "Promosys Map Data");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
        }

        baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        //fileName = "Promosys Map" +".csv";
        //filePath = baseDir + File.separator + "Promosys Map Data" + File.separator + fileName;
    }


    public void startCreateBackup(){
        boolean isGotPermission =  isWriteToStoragePermissionGranted();
        if(isGotPermission){
            saveBackupToPhone();
        }else {
            requestWriteToStoragePermissionDialog();
        }
    }

    public boolean isWriteToStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {

            boolean WriteStoragePermission = checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;

            if (WriteStoragePermission) {
                //Toast.makeText(this,"Write to storage permission granted",Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    public void requestWriteToStoragePermissionDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Requesting Permission");
        alertDialog.setMessage("Requesting permission to write to external storage?");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestWriteStoragePermission();
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    public void requestWriteStoragePermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
            {
                WRITE_EXTERNAL_STORAGE
            }, RequestPermissionCode2);
    }

    //After finish downloading log, convert the list to .csv file and save to phone's internal phone
    private void saveBackupToPhone(){
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Promosys Map Data");
        boolean success = true;

        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        //fileName = "Promosys Map" +".csv";
        filePath = baseDir + File.separator + "Promosys Map Data" + File.separator + fileName;
        f = new File(filePath );
        try {
            String strTimeCreated = getCurrentTime();
            writer = new CSVWriter(new FileWriter(filePath));
            //String[] createdOn = {"Created On: ",strTimeCreated,"","",""};
            String[] createdOn = {"Created On: ",strTimeCreated};
            writer.writeNext(createdOn);
            String[] data = {"No.","Group", "Location Name","Latitude","Longitude"};
            writer.writeNext(data);
            importDataFromJsonList();
            writer.close();
            Toast.makeText(this,"Backup Created",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void importDataFromJsonList(){
        for (int i = 0;i<fragmentLocation.backgroundLocationList.size();i++){
            String locGroup = fragmentLocation.backgroundLocationList.get(i).getLocGroup();
            String locLocationName = fragmentLocation.backgroundLocationList.get(i).getLocName();
            String locLatitude = fragmentLocation.backgroundLocationList.get(i).getLocLatitude();
            String locLongitude = fragmentLocation.backgroundLocationList.get(i).getLocLongitude();

            String[] data = {String.valueOf(i+1),locGroup, locLocationName,locLatitude,locLongitude};
            writer.writeNext(data);
        }
    }

    private String getCurrentTime(){
        String currentTime = "";
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy  h:mm:ss a");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        currentTime = sdf.format(date);
        return currentTime;
    }


    /******************read backup from excell*******************/

    public void startReadBackup(){
        boolean isGotPermission =  isReadFromStoragePermissionGranted();
        if(isGotPermission){
            readStorageDirectory();
            //readBackupData(importFileName);
        }else {
            requestReadStoragePermission();
        }
    }

    public void requestReadStoragePermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                    READ_EXTERNAL_STORAGE

                }, RequestPermissionCode3);
    }

    public boolean isReadFromStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {

            boolean ReadStoragePermission = checkSelfPermission(READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;

            if (ReadStoragePermission) {
                return true;
            } else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    public void readBackupData(String filePath){
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        //FirebaseDatabase database = FirebaseDatabase.getInstance();

        baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String filePathDirectory = baseDir + File.separator + "Promosys Map Data" + File.separator + filePath;
        Log.i("listDirectory", "filePathDirectory: " + filePathDirectory);

        try {
            fragmentLocation.backgroundLocationList.clear();
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] backupList = line.split(cvsSplitBy);

                if (backupList.length>4){
                    String locGroup = backupList[1].replace("\"", "");
                    String locName = backupList[2].replace("\"", "");
                    String locLatitude = backupList[3].replace("\"", "");
                    String locLongitude = backupList[4].replace("\"", "");
                    Log.i("readFromBackup","Group: " + locGroup + "\n"
                            +"Name: " + locName + "\n"
                            +"Latitude: " + locLatitude + "\n"
                            +"Longitude: " + locLongitude);

                    if (locGroup.equals("Group") && locName.equals("Location Name")){

                    }else{
                        LocationObject locationObject = new LocationObject(locGroup,locName,locLatitude,locLongitude,"notClicked");
                        fragmentLocation.backgroundLocationList.add(locationObject);

                        /*
                        final DatabaseReference myRef = database.getReference().child("user1").child(locName);

                        myRef.setValue(locationObject);
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {

                                LocationObject locationObject = snapshot.getValue(LocationObject.class);

                                if (locationObject == null) {
                                    return;
                                }
                                //Log.i("ReadFromFirebase","LocationName: " + locationObject.getLocName());
                            }

                            @Override
                            public void onCancelled(DatabaseError firebaseError) {
                                Log.e("ReadFromFirebase","error: " +firebaseError.getMessage());
                            }
                        });
                        */
                    }

                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //save background list in json format
        saveLocationList();

        insertGroupName();

    }

    public void insertGroupName(){
        fragmentGroup.groupList.clear();
        for (int i = 0;i<fragmentLocation.backgroundLocationList.size();i++){
            String backGroupName = fragmentLocation.backgroundLocationList.get(i).getLocGroup();
            boolean duplicateGroup = false;
            for (int j=0;j<fragmentGroup.groupList.size();j++){
                String groupName = fragmentGroup.groupList.get(j).getGroupName();
                if (groupName.equals(backGroupName)){
                    duplicateGroup = true;
                    break;
                }
            }
            if (!duplicateGroup){
                GroupObject groupObject = new GroupObject(backGroupName);
                fragmentGroup.groupList.add(groupObject);
                fragmentGroup.groupAdapter.notifyDataSetChanged();
            }
        }
        saveGroupList();

    }

    public void readStorageDirectory(){
        final ArrayList<BackupObject> lstFile = new ArrayList<BackupObject>();
        String path = Environment.getExternalStorageDirectory().toString();
        File f = new File(path);
        File file[] = f.listFiles();

        for (int i =0;i<file.length;i++){
            BackupObject backupObject;

            if (file[i].isDirectory()){
                File directoryFile[] = file[i].listFiles();
                for (int j=0;j<directoryFile.length;j++){
                    if (directoryFile[j].getName().contains(".csv")){
                        backupObject = new BackupObject(directoryFile[j].getName(),directoryFile[j].getPath());
                        lstFile.add(backupObject);
                    }
                }
            }else {
                if (file[i].getName().contains(".csv")){
                    backupObject = new BackupObject(file[i].getName(),file[i].getPath());
                    lstFile.add(backupObject);
                }
            }

        }

        ArrayList displayBackupList = new ArrayList();
        for (int i = 0; i<lstFile.size();i++){
            Log.i("listDirectory","name: " + lstFile.get(i).getBackupName() + "\n"
                                 +"directory: " + lstFile.get(i).getBackupDirectory());

            displayBackupList.add(lstFile.get(i).getBackupName());
        }

        new MaterialDialog.Builder(this)
                .title("Import from...")
                .items(displayBackupList)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which != -1){
                            Log.i("listDirectory","selected: " + lstFile.get(which));
                            importFileName = lstFile.get(which).getBackupDirectory();
                            readBackupData(importFileName);
                            //startReadBackup();
                        }

                        return true;
                    }
                })
                .positiveText("OK")
                .show();

    }


}
