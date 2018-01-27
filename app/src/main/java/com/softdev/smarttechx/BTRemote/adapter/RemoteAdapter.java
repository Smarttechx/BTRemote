package com.softdev.smarttechx.BTRemote.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.softdev.smarttechx.BTRemote.ControlActivity;
import com.softdev.smarttechx.BTRemote.R;
import com.softdev.smarttechx.BTRemote.data.SaveData;
import com.softdev.smarttechx.BTRemote.model.Remotedevice;

import java.util.ArrayList;

/**
 * Created by SMARTTECHX on 9/17/2017.
 */

public class RemoteAdapter extends RecyclerView.Adapter<RemoteAdapter.remoteViewHolder> {

    private Context mContext;
    private ArrayList<Remotedevice> RemoteList;
    private ArrayList<String> macList;
    private Remotedevice device;
    private int devicePosition;


    public class remoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView displayAvatar;
        TextView displayName;
        ImageButton displayOverflowButton;

        public remoteViewHolder(View view) {
            super(view);
            displayAvatar = (ImageView) itemView.findViewById(R.id.display_imageView);
            displayName = (TextView) itemView.findViewById(R.id.display_name_textView);
            displayOverflowButton = (ImageButton) itemView.findViewById(R.id.display_overflow);
            itemView.setOnClickListener(this);
            displayOverflowButton.setOnClickListener(this);

        }
        @Override
        public void onClick(View v) {
            if (v == itemView) {
                //stopService();
                Intent controlIntent=new Intent(mContext, ControlActivity.class);
                controlIntent.putExtra("remote",(Remotedevice)itemView.getTag());
                mContext.startActivity(controlIntent);


            } else if (v == displayOverflowButton) {
                showPopupMenu(displayOverflowButton);
                displayOverflowButton.getTag();
                devicePosition = getLayoutPosition();
            }

        }

    }
    public  static boolean isNumeric(String text){
        for(char c: text.toCharArray()){
            if(!Character.isDigit(c)) return false;
        }
        return true;
    }
    public RemoteAdapter(Context mContext, ArrayList<Remotedevice> RemoteList) {
        this.mContext = mContext;
        this.RemoteList = RemoteList;

    }


    @Override
    public remoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.remote_list_item, parent, false);
        return new remoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final remoteViewHolder holder, final int position) {
        device = RemoteList.get(position);
        holder.displayName.setText(device.getNickName());
        holder.displayOverflowButton.setTag(device);
        holder.itemView.setTag(device);

    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_home_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new RemoteMenuItemClickListener());
        // popup.inflate(R.menu.menu_home_item);
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
   private class RemoteMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        SaveData saveData = new SaveData(mContext);

        public RemoteMenuItemClickListener() {
        }

        public void updateRemote() {
            LayoutInflater li = LayoutInflater.from(mContext);
            View promptsView = li.inflate(R.layout.dialog_edit_text, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    mContext);
            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);
            final EditText NickNameInput = (EditText) promptsView
                    .findViewById(R.id.add_RemoteName);
            final EditText DeviceIDInput = (EditText) promptsView
                    .findViewById(R.id.device_id);

            final Spinner Mac_Add = ( Spinner) promptsView
                    .findViewById(R.id.mac_address);
            macList=saveData.loadMac_Add();
            final ArrayAdapter<String> spinneradapter= new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item,macList);
            spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            Mac_Add.setAdapter(spinneradapter);
            // deviceID.append(String.valueOf(Deviceid));
            String currentMac=RemoteList.get(devicePosition).getMac_Add();
            Mac_Add.setSelection(macList.indexOf(currentMac));
            NickNameInput.setText(RemoteList.get(devicePosition).getNickName());
            DeviceIDInput.setText(RemoteList.get(devicePosition).getDeviceid());
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Save",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    if(isNumeric(DeviceIDInput.getText().toString().substring(0,1))){
                                        RemoteList.get(devicePosition).setNickName( NickNameInput.getText().toString());
                                        RemoteList.get(devicePosition).setDeviceID( DeviceIDInput.getText().toString());
                                        RemoteList.get(devicePosition).setMacAdd( Mac_Add.getSelectedItem().toString());
                                        saveData.save(RemoteList);
                                        notifyDataSetChanged();
                                    }
                                    else{
                                       Toast.makeText(mContext, "Please confirm the Device ID", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }



        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_edit:
                    updateRemote();
                    return true;
                case R.id.action_delete:
                    //updateRemoteListItems( saveData.deleteData(devicePosition));
                    RemoteList.remove(devicePosition);
                    saveData.save(RemoteList);
                    notifyDataSetChanged();
                    return true;
                default:
            }
            return false;
        }
    }


    @Override
    public int getItemCount() {
        return RemoteList.size();
    }

}
