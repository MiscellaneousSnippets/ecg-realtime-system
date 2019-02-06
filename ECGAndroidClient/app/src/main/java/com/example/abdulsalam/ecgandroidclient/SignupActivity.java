package com.example.abdulsalam.ecgandroidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abdulsalam.ecgandroidclient.models.Doctor;
import com.example.abdulsalam.ecgandroidclient.models.Patient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignupActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText editTextEmail, editTextPassword, editTextConfirm;
    TextView textViewLogin;
    AppCompatButton buttonSignUp;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String user;
//    private Doctor doctor;
//    private Patient patient;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextEmail = findViewById(R.id.input_email);
        editTextPassword = findViewById(R.id.input_pass);
        editTextConfirm = findViewById(R.id.input_confirm_pass);
        progressBar = findViewById(R.id.progressbar);
        textViewLogin = findViewById(R.id.link_login);
        buttonSignUp = findViewById(R.id.btn_signup);
        radioGroup = findViewById(R.id.radio);


        mAuth = FirebaseAuth.getInstance();

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });


    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPass = editTextConfirm.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(this.getString(R.string.email_req));
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError(getString(R.string.pass_req));
            editTextPassword.requestFocus();
            return;
        }

        if (confirmPass.isEmpty()) {
            editTextConfirm.setError(getString(R.string.confirmation));
            editTextConfirm.requestFocus();
            return;
        }

        if (!password.equals(confirmPass)) {
            editTextConfirm.setError(getString(R.string.no_match));
            editTextConfirm.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError(getString(R.string.length_error));
            editTextPassword.requestFocus();
            return;
        }

        int selectedId = radioGroup.getCheckedRadioButtonId();
        radioButton = radioGroup.findViewById(R.id.radio_doctor);
        if (selectedId == -1) {
            radioButton.setError(getString(R.string.make_selection));
            radioButton.requestFocus();
            return;
        }
        radioButton = findViewById(selectedId);
        user = (String) radioButton.getText();


        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    finish();
                    Intent intent;
                    if (user.equals("Doctor"))
                        intent = new Intent(SignupActivity.this, DoctorDetailsActivity.class);
                    else
                        intent = new Intent(SignupActivity.this, PatientDetailsActivity.class);

                    intent.putExtra("email", email);
                    startActivity(intent);
                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), R.string.registered, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }
}

