package com.example.abdulsalam.ecgandroidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.abdulsalam.ecgandroidclient.models.Doctor;
import com.example.abdulsalam.ecgandroidclient.models.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PatientDetailsActivity extends AppCompatActivity {

    private Patient patient;
    private EditText editTextPatientName, editTextPatientPhone, editTextPatientAddress, editTextPatientAge,
                     editTextPatientHeight, editTextPatientWeight ;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button buttonRegister;
    private String email, name, phone, address, age, height, weight, gender, doctorName, userID;
    private DatabaseReference myRef;
    private List<String> doctors;
    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        editTextPatientName = findViewById(R.id.patient_name);
        editTextPatientPhone = findViewById(R.id.patient_phone);
        editTextPatientAddress = findViewById(R.id.patient_address);
        editTextPatientAge = findViewById(R.id.patient_age);
        editTextPatientHeight = findViewById(R.id.patient_height);
        editTextPatientWeight = findViewById(R.id.patient_weight);
        buttonRegister = findViewById(R.id.btn_register);
        radioGroup = findViewById(R.id.radio);
        spinner = findViewById(R.id.spinner);

        doctors = new ArrayList<>();

        Intent intent = getIntent();
        email = intent.getStringExtra("email");


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        userID = user.getUid();
        getDoctorsList(new GetDoctorsCallback() {
            @Override
            public void getDoctors(List<String> doctorsList) {
                String [] doctorsArray = new String[doctorsList.size()];
                doctorsArray = doctorsList.toArray(doctorsArray);
                System.out.println(doctorsArray[0] + "   555555");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(PatientDetailsActivity.this,
                        android.R.layout.simple_spinner_item,doctorsArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                doctorName = (String) spinner.getSelectedItem();
            }
        });
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

    }

    void register(){


        name = editTextPatientName.getText().toString().trim();
        phone = editTextPatientPhone.getText().toString().trim();
        address = editTextPatientAddress.getText().toString().trim();
        age = editTextPatientAge.getText().toString().trim();
        height = editTextPatientHeight.getText().toString().trim();
        weight = editTextPatientWeight.getText().toString().trim();
        age = editTextPatientAge.getText().toString().trim();

        if (name.isEmpty()) {
            editTextPatientName.setError("Name is required");
            editTextPatientName.requestFocus();
            return;
        }

        if(phone.isEmpty()){
            editTextPatientPhone.setError("Phone is required");
            editTextPatientPhone.requestFocus();
            return;
        }

        if(address.isEmpty()){
            editTextPatientAddress.setError("Address is required");
            editTextPatientAddress.requestFocus();
            return;
        }

        if(age.isEmpty()){
            editTextPatientAge.setError("Age is required");
            editTextPatientAge.requestFocus();
            return;
        }

        if(height.isEmpty()){
            editTextPatientAge.setError("Height is required");
            editTextPatientAge.requestFocus();
            return;
        }

        if(weight.isEmpty()){
            editTextPatientAge.setError("Weight is required");
            editTextPatientAge.requestFocus();
            return;
        }

        int selectedId = radioGroup.getCheckedRadioButtonId();
        radioButton = radioGroup.findViewById(R.id.radio_male);
        if (selectedId == -1) {
            radioButton.setError("Gender is required");
            radioButton.requestFocus();
            return;
        }
        radioButton = findViewById(selectedId);
        gender = (String) radioButton.getText();



        patient = new Patient(name, phone, address, email, age, gender, weight, height, doctorName, "patient");
        myRef.child(userID).setValue(patient);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("source","patient");
        intent.putExtra("patient", patient);
        intent.putExtra("email", email);
        startActivity(intent);

    }


    public interface GetDoctorsCallback {
        void getDoctors(List<String> doctorsList);
    }

    public void getDoctorsList(final PatientDetailsActivity.GetDoctorsCallback myCallback) {

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                doctors.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getValue(Doctor.class).getType().equals("doctor")) {
                        doctors.add(ds.getValue(Doctor.class).getName());
                    }
                }
                myCallback.getDoctors(doctors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
