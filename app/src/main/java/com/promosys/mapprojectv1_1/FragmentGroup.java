package com.promosys.mapprojectv1_1;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Fimrware 2 on 4/6/2017.
 */

public class FragmentGroup extends Fragment {

    private View rootView;
    private Context context;
    private MainActivity mainActivity;

    private RecyclerView groupRecyclerView;
    public ArrayList<GroupObject> groupList;

    private RecyclerView.LayoutManager mLayoutManager;
    public GroupAdapter groupAdapter;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_fragment_group, container, false);
        context = rootView.getContext();
        mainActivity = (MainActivity)context;

        initRecyclerView();
        getGroupList();
        //getGroupListFromDatabase();
        return rootView;
    }

    private void initRecyclerView(){
        groupRecyclerView = (RecyclerView) rootView.findViewById(R.id.recview_group);
        groupRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        //mLayoutManager = new GridLayoutManager(context,2);
        groupRecyclerView.setLayoutManager(mLayoutManager);
        groupList = new ArrayList<GroupObject>();
        groupAdapter = new GroupAdapter(groupList);

        //groupAdapter = new GroupAdapter(mainActivity.groupFBList);
        groupRecyclerView.setAdapter(groupAdapter);

        groupAdapter.setOnItemClickListener(new GroupAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i("GroupName","groupClicked: " + groupList.get(position).getGroupName());
                mainActivity.strGroupName = groupList.get(position).getGroupName();
                mainActivity.changeFragment("FragmentLocation");
            }
        });


        /*
        groupAdapter.setOnItemClickListener(new GroupAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i("GroupName","groupClicked: " + mainActivity.groupFBList.get(position).getGroupName());
                mainActivity.strGroupName = mainActivity.groupFBList.get(position).getGroupName();
                mainActivity.changeFragment("FragmentLocation");
            }
        });
        */

        groupAdapter.setOnItemLongClickListener(new GroupAdapter.MyClickListener2() {
            @Override
            public void onItemLongClick(int position, View v) {
                editGroup(position);
            }
        });
    }

    private void getGroupList(){
        String jsonGroup = mainActivity.getGroupName();
        if (!(jsonGroup.isEmpty())){
            try {
                //JSONObject jsonObject = new JSONObject(jsonGroup);
                JSONArray jsonArray = new JSONArray(jsonGroup);
                for (int i = 0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String groupName = jsonObject.getString("groupName");
                    Log.i("GroupName","groupName: " + groupName);
                    GroupObject groupObject = new GroupObject(groupName);
                    groupList.add(groupObject);
                    groupAdapter.notifyDataSetChanged();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getGroupListFromDatabase(){
        groupAdapter.notifyDataSetChanged();
    }

    public void addGroup(){
        final Dialog openDialog = new Dialog(context);
        openDialog.setContentView(R.layout.layout_add_group_dialog);
        openDialog.setTitle("Add Group");

        final EditText addGroupName = (EditText)openDialog.findViewById(R.id.edt_add_group);

        Button dialogDeleteBtn = (Button)openDialog.findViewById(R.id.btn_delete_group);
        dialogDeleteBtn.setVisibility(View.GONE);

        Button dialogAddSubunitBtn = (Button)openDialog.findViewById(R.id.btn_add_group);
        dialogAddSubunitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String groupName = addGroupName.getText().toString();
                GroupObject groupObject = new GroupObject(groupName);
                groupList.add(groupObject);
                groupAdapter.notifyDataSetChanged();

                mainActivity.saveGroupList();
                openDialog.dismiss();


            }
        });

        Button dialogCancelBtn = (Button)openDialog.findViewById(R.id.btn_cancel_group);
        dialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog.dismiss();
            }
        });

        openDialog.show();
    }

    public void editGroup(final int position){
        final Dialog openDialog = new Dialog(context);
        openDialog.setContentView(R.layout.layout_add_group_dialog);
        openDialog.setTitle("Edit Group");

        final EditText edtGroupName = (EditText)openDialog.findViewById(R.id.edt_add_group);
        edtGroupName.setText(groupList.get(position).getGroupName());

        Button dialogEditGroupBtn = (Button)openDialog.findViewById(R.id.btn_add_group);
        dialogEditGroupBtn.setText("Edit");
        dialogEditGroupBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String newGroupName = edtGroupName.getText().toString();
                String oldGroupName = groupList.get(position).getGroupName();

                mainActivity.editGroupLocation(oldGroupName,newGroupName);

                groupList.get(position).setGroupName(newGroupName);
                groupAdapter.notifyDataSetChanged();

                mainActivity.saveGroupList();
                openDialog.dismiss();


            }
        });

        Button dialogCancelBtn = (Button)openDialog.findViewById(R.id.btn_cancel_group);
        dialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog.dismiss();
            }
        });

        Button dialogDeleteBtn = (Button)openDialog.findViewById(R.id.btn_delete_group);
        dialogDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.deleteGroupLocation(groupList.get(position).getGroupName());

                groupList.remove(position);
                groupAdapter.notifyDataSetChanged();

                mainActivity.saveGroupList();

                openDialog.dismiss();
            }
        });


        openDialog.show();
    }


}
