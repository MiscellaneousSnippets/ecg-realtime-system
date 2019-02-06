package com.example.abdulsalam.ecgandroidclient;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.abdulsalam.ecgandroidclient.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientsActivity extends AppCompatActivity {

    private List<Patient> patients;
    private ListView patientsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);
        patientsListView = findViewById(R.id.list);

        Intent intent = getIntent();
        patients = intent.getParcelableArrayListExtra("patients_list");

        patientsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent to_the_map = new Intent(PatientsActivity.this, MapsActivity.class);
                to_the_map.putExtra("selected_patient", patients.get(position));
                startActivity(to_the_map);
            }
        });

        ArrayList<String> patientNames = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++)
            patientNames.add(patients.get(i).getName());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (PatientsActivity.this, android.R.layout.simple_list_item_1, patientNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                /// Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                TextView tv = view.findViewById(android.R.id.text1);

                // Set the text size 25 dip for ListView each item
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                tv.setTextColor(Color.parseColor("#FFEA0839"));

                // Return the view
                return view;
            }
        };

        // DataBind ListView with items from ArrayAdapter
        patientsListView.setAdapter(arrayAdapter);
    }
}
