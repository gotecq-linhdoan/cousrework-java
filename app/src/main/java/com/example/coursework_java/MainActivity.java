package com.example.coursework_java;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab, deleteAll;
    RecyclerView recyclerView;
    List<Hike> hikeList;
    DatabaseHelper dbHelper;
    HikeAdapter adapter;
    SearchView searchView;

    @Override
    protected void onResume() {
        super.onResume();
        loadHikesFromSQLite();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        deleteAll = findViewById(R.id.deleteAllFab);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        hikeList = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);
        adapter = new HikeAdapter(MainActivity.this, hikeList);
        loadHikesFromSQLite();
        recyclerView.setAdapter(adapter);



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete all hike")
                    .setMessage("Are you sure you want to delete all hike record?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                            dbHelper.deleteAllHikes();
                            hikeList.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "All hikes have been deleted.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            }
        });
    }

    public void searchList(String text) {
        ArrayList<Hike> searchList = new ArrayList<>();
        for( Hike hikeClass: hikeList) {
            if(hikeClass.getHikeName().toLowerCase().contains(text.toLowerCase())){
                searchList.add(hikeClass);
            }
        }
        adapter.searchDataList(searchList);
    }
    private void loadHikesFromSQLite() {
        hikeList.clear();

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_HIKE, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.HIKE_NAME);
            int descColumnIndex = cursor.getColumnIndex(DatabaseHelper.HIKE_DESCRIPTION);
            int locationColumnIndex = cursor.getColumnIndex(DatabaseHelper.HIKE_LOCATION);
            int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.HIKE_DATE);
            int lengthColumnIndex = cursor.getColumnIndex(DatabaseHelper.HIKE_LENGTH);
            int levelColumnIndex = cursor.getColumnIndex(DatabaseHelper.HIKE_LEVEL);
            int hasParkingColumnIndex = cursor.getColumnIndex(DatabaseHelper.HIKE_HAS_PARKING);
            int hasIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.HIKE_ID);

            do {
                String name = cursor.getString(nameColumnIndex);
                String desc = cursor.getString(descColumnIndex);
                String location = cursor.getString(locationColumnIndex);
                String date = cursor.getString(dateColumnIndex);
                String length = cursor.getString(lengthColumnIndex);
                String level = cursor.getString(levelColumnIndex);
                int hasParking = cursor.getInt(hasParkingColumnIndex);
                int key = cursor.getInt(hasIdColumnIndex);

                Hike hike = new Hike(name, location, desc, date, level, length, hasParking);
                hike.setKey(key);
                hikeList.add(hike);
            } while (cursor.moveToNext());

            cursor.close();
        }

        cursor.close();
        database.close();

        adapter.notifyDataSetChanged();
    }
}