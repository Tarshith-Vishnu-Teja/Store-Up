package com.storeup;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


public class UserProfile extends ListFragment implements AdapterView.OnItemClickListener{

    String[] tags = {"Username","Password","Street","City","State","Zipcode","Phone"};
    String[] values = {"google","google1234","655 S Fair Oaks Ave","Sunnyvale","CA","94086","9254435543"};

    ArrayList<HashMap<String, String>> data = new ArrayList();
    SimpleAdapter adapter;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("My Profile");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_profile_frag, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        HashMap<String, String> map = new HashMap();



        // Keys in HashMap
        String[] from = {"Tag","Value"};

        //Ids of view
        int[] to = {R.id.tag, R.id.value};

        for (int i=0;i<values.length;i++) {

            map.put("Tag",tags[i]);
            map.put("Value",values[i]);
            data.add(map);
        }


        //Adapter
        adapter = new SimpleAdapter(getActivity(), data, R.layout.user_profile_frag, from, to);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
    }
}
