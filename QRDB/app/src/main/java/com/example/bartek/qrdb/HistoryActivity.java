package com.example.bartek.qrdb;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";

    DatabaseHelper mDatabaseHelper;

    private ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mListView = (ListView) findViewById(R.id.list);
        mDatabaseHelper = new DatabaseHelper(this);

        showListView();
    }

    private void showListView() {

        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<>();
        while (data.moveToNext()) {
            listData.add(data.getString(1) + " " + data.getString(3));
        }




        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        mListView.setAdapter(adapter);

        //set an onItemClickListener to the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String itemFromList = adapterView.getItemAtPosition(position).toString();
                String[] parts = itemFromList.split(" ");
                String name = parts[0];

                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(name));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(HistoryActivity.this, "It's not a website", Toast.LENGTH_LONG).show();
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {


            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    String itemFromList = adapterView.getItemAtPosition(position).toString();
                    String[] parts = itemFromList.split(" ");
                    String[] location = new String[0];

                    Cursor cursor = mDatabaseHelper.getDataID(parts[0], parts[1] +" "+ parts [2]);
                    cursor.moveToPosition(-1);

                    while (cursor.moveToNext()) {
                        if (cursor.getString(2) != null) {
                            location = cursor.getString(2).split(" ");
                        }
                    }

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + location[0] + "," + location[1]));
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Toast.makeText(HistoryActivity.this, "Can't find location", Toast.LENGTH_LONG).show();
                }

                return true;
            }
        });
    }
}
