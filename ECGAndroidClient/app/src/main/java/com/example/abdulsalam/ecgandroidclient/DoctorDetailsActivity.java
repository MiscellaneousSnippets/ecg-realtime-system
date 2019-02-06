package com.example.abdulsalam.ecgandroidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.abdulsalam.ecgandroidclient.models.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DoctorDetailsActivity extends AppCompatActivity {

    private Doctor doctor;
    private EditText editTextDoctorName, editTextDoctorPhone, editTextDoctorAddress, editTextDoctorHospital;
    private Button buttonRegister;
    private String email, name, phone, address, hospital, userID;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_details);

        editTextDoctorName = findViewById(R.id.doctor_name);
        editTextDoctorPhone = findViewById(R.id.doctor_phone);
        editTextDoctorAddress = findViewById(R.id.doctor_address);
        editTextDoctorHospital = findViewById(R.id.doctor_hospital);
        buttonRegister = findViewById(R.id.btn_register);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        userID = user.getUid();
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

    }

    void register() {
        name = editTextDoctorName.getText().toString().trim();
        phone = editTextDoctorPhone.getText().toString().trim();
        address = editTextDoctorAddress.getText().toString().trim();
        hospital = editTextDoctorHospital.getText().toString().trim();

        if (name.isEmpty()) {
            editTextDoctorName.setError("Name is required");
            editTextDoctorName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextDoctorPhone.setError("Phone is required");
            editTextDoctorPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            editTextDoctorAddress.setError("Address is required");
            editTextDoctorAddress.requestFocus();
            return;
        }

        if (hospital.isEmpty()) {
            editTextDoctorHospital.setError("Hospital is required");
            editTextDoctorHospital.requestFocus();
            return;
        }

        doctor = new Doctor(name, phone, address, email, hospital, "doctor");
        myRef.child(userID).setValue(doctor);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("source", "doctor");
        intent.putExtra("email", email);
        startActivity(intent);

    }
}
