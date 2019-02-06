package com.example.abdulsalam.ecgandroidclient;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abdulsalam.ecgandroidclient.models.Doctor;
import com.example.abdulsalam.ecgandroidclient.models.Patient;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_INT = 177;
    private static int flag;
    private final Activity context = this;
    DatabaseReference myRef;
    String userID, email;
    String userType = "";
    LatLng latLng;
    //JSON Packet Variables
    JSONObject inbound;
    JSONObject rawData;
    JSONArray ecgValues;
    JSONArray spo2Values;
    JSONArray respValues;
    //Local Variables
    String ecgStatus;
    String globalTemp;
    String globalSpo2;
    String globalResp;
    String globalHeartRate;
    //Draw Variables
    DataPoint[] dataPointEcg;
    DataPoint[] dataPointSpo2;
    DataPoint[] dataPointResp;
    private Socket mSocket;
    private FusedLocationProviderClient client;
    private LocationRequest mLocationRequest;
    private List<Patient> patients;
    private FloatingActionButton fab;
    private String doctorPhone = "", doctorName = "";
    //View Variables
    private TextView txtSpo2;
    private TextView txtHeartRate;
    private TextView txtRespiration;
    private Button txtTemp;
    //graphs
    private GraphView graphEcg;
    private GraphView graphSpo2;
    private GraphView graphResp;


    {
        try {
            //mSocket = IO.socket("http://10.0.2.2:3000");
            mSocket = IO.socket("http://192.168.43.27:3000");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationRequest = LocationRequest.create();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        userID = user.getUid();
        patients = new ArrayList<>();
        client = LocationServices.getFusedLocationProviderClient(this);


        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        String source = intent.getStringExtra("source");
        if (source != null && source.equals("patient"))
            flag = 0;
        else if (source != null && source.equals("doctor"))
            flag = 1;
        getUserType(new GetUsersCallback() {
            @Override
            public void getUsers(String userType, List<Patient> patientList, String phone) {
                if (userType.equals("patient")) {
                    flag = 0;
                } else if (userType.equals("doctor")) {
                    flag = 1;
                }
                patients = patientList;
                doctorPhone = phone;
            }
        });

        //Define View Variables
        txtHeartRate = findViewById(R.id.txt_heartrate);
        txtSpo2 = findViewById(R.id.txt_spo2);
        txtRespiration = findViewById(R.id.txt_resp);
        txtTemp = findViewById(R.id.txt_temp);
        fab = findViewById(R.id.floatingActionButton);

        graphEcg = findViewById(R.id.graph_ecg);
        graphSpo2 = findViewById(R.id.graph_spo2);
        graphResp = findViewById(R.id.graph_resp);


        //fix spo2 graph
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMaximumIntegerDigits(4);
        graphSpo2.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));

        mSocket.on("event", dataPacket);
        mSocket.connect();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri u = Uri.parse("tel:" + doctorPhone);
                Intent i = new Intent(Intent.ACTION_DIAL, u);

                try {
                    // Launch the Phone app's dialer with a phone
                    // number to dial a call.
                    startActivity(i);
                } catch (SecurityException s) {
                    // show() method display the toast with
                    // exception message.
                    Toast.makeText(MainActivity.this, "Error !!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    private Emitter.Listener dataPacket = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    inbound = (JSONObject) args[0];
                    try {
                        //Parse Atrial Fibrillation
                        //ecgStatus = inbound.getString("status");
                        //Toast.makeText(context,ecgStatus , Toast.LENGTH_SHORT).show();

                        //Get JSON object from Raspberry
                        rawData = inbound.getJSONObject("raw_data");

                        //Parse Global Data
                        globalTemp = rawData.getString("global_temp");
                        globalSpo2 = rawData.getString("global_spo2");
                        globalResp = rawData.getString("global_RespirationRate");
                        globalHeartRate = rawData.getString("global_HeartRate");

                        //Parse Array Data
                        ecgValues = rawData.getJSONArray("ecg_values");
                        respValues = rawData.getJSONArray("resp_values");
                        spo2Values = rawData.getJSONArray("spo2_values");

                        //Fill Data Into Views
                        dataPointEcg = new DataPoint[ecgValues.length()];
                        dataPointSpo2 = new DataPoint[spo2Values.length()];
                        dataPointResp = new DataPoint[respValues.length()];

                        for (int i = 0; i < ecgValues.length(); i++) {
                            dataPointEcg[i] = new DataPoint(i, Float.parseFloat(ecgValues.getString(i)));
                            dataPointSpo2[i] = new DataPoint(i, Float.parseFloat(spo2Values.getString(i)));
                            dataPointResp[i] = new DataPoint(i, Float.parseFloat(respValues.getString(i)));
                        }

                        txtHeartRate.setTextColor(Color.parseColor("#FFF3063E"));
                        txtHeartRate.setText(globalHeartRate + " " + "BPM");
                        txtSpo2.setTextColor(Color.parseColor("#FFF3063E"));
                        txtSpo2.setText(globalSpo2 + "%");
                        txtRespiration.setTextColor(Color.parseColor("#FFF3063E"));
                        txtRespiration.setText(globalResp + " RPM");
                        if (globalTemp.length() > 6)
                            globalTemp = globalTemp.substring(0, 5);
                        txtTemp.setText(globalTemp);


                    } catch (JSONException e) {
                        return;
                    }


                    //draw graphs

                    //ecg graph
                    graphEcg.removeAllSeries();
                    LineGraphSeries<DataPoint> seriesEcg = new LineGraphSeries<>(dataPointEcg);
                    graphEcg.addSeries(seriesEcg);

                    //spo2 graph
                    graphSpo2.removeAllSeries();
                    LineGraphSeries<DataPoint> seriesSpo2 = new LineGraphSeries<>(dataPointSpo2);
                    graphSpo2.addSeries(seriesSpo2);

                    //Resp graph
                    graphResp.removeAllSeries();
                    LineGraphSeries<DataPoint> seriesResp = new LineGraphSeries<>(dataPointResp);
                    graphResp.addSeries(seriesResp);


                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        getUserType(new GetUsersCallback() {
            @Override
            public void getUsers(String userType, List<Patient> patientList, String phone) {
                if (userType.equals("patient")) {
                    fab.show();
                    inflater.inflate(R.menu.patient_menu, menu);
                } else if (userType.equals("doctor")) {
                    fab.hide();
                    inflater.inflate(R.menu.doctor_menu, menu);                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else if (id == R.id.share_location) {

            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_INT);
                    }

                    return false;
                }
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            myRef.child(userID).child("location").child("latitude").setValue(location.getLatitude());
                            myRef.child(userID).child("location").child("longitude").setValue(location.getLongitude());

                        } else
                            Toast.makeText(MainActivity.this, "There is a problem !!", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                Toast.makeText(context, "Location not ready !!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else if (id == R.id.patients_list) {
            Intent intent = new Intent(this, PatientsActivity.class);
            intent.putParcelableArrayListExtra("patients_list", (ArrayList<? extends Parcelable>) patients);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public interface GetUsersCallback {
        void getUsers(String userType, List<Patient> patientList, String phone);
    }

    public void getUserType(final MainActivity.GetUsersCallback myCallback) {

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userType = "";
                Patient patient;
                double latitude = 0, longitude = 0;
                {
                    userType = dataSnapshot.child(userID).getValue(Doctor.class).getType();
                }
                patients.clear();
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getValue(Patient.class).getType().equals("patient")) {
                            patient = ds.getValue(Patient.class);
                            for (DataSnapshot location : ds.getChildren()) {
                                if (location.getKey().equals("location"))
                                    for (DataSnapshot location_data : location.getChildren()) {
                                        if (location_data.getKey().equals("latitude"))
                                            latitude = (location_data.getValue(Double.class));
                                        else if (location_data.getKey().equals("longitude"))
                                            longitude = (location_data.getValue(Double.class));
                                    }
                                if (location.getKey().equals("doctor_name")) {
                                    doctorName = location.getValue(String.class);
                                }
                            }
                            patient.setCurrentLocation(new LatLng(latitude, longitude));
                            patients.add(patient);
                        }
                    }

                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        if (ds.getValue(Patient.class).getName().equals(doctorName)) {
                            doctorPhone = ds.getValue(Patient.class).getPhone();
                        }
                    }

                }

                myCallback.getUsers(userType, patients, doctorPhone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
