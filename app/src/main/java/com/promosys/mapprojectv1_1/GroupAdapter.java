package com.promosys.mapprojectv1_1;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Fimrware 2 on 4/6/2017.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

    private List<GroupObject> groupList;
    private static MyClickListener myClickListener;
    private static MyClickListener2 myClickListener2;

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements
            View.OnClickListener,
            View.OnLongClickListener {
        public TextView groupName;

        public MyViewHolder(View view) {
            super(view);
            groupName = (TextView)view.findViewById(R.id.txt_group_name);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View view) {
            myClickListener.onItemClick(getAdapterPosition(), view);
        }

        @Override
        public boolean onLongClick(View view) {
            myClickListener2.onItemLongClick(getAdapterPosition(), view);
            return false;
        }
    }


    public GroupAdapter(List<GroupObject> groupList) {
        this.groupList = groupList;
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public void setOnItemLongClickListener(MyClickListener2 myClickListener2) {
        this.myClickListener2 = myClickListener2;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_group_list, parent, false);
        //.inflate(R.layout.monitor_list_row2, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GroupObject groupObject = groupList.get(position);
        holder.groupName.setText(groupObject.getGroupName());

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }

    public interface MyClickListener2 {
        public void onItemLongClick(int position, View v);
    }

    public void clear() {
        groupList.clear();
        notifyDataSetChanged();

    }
}

