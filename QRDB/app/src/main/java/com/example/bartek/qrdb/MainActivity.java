package com.example.bartek.qrdb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.Result;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    DatabaseHelper mDatabaseHelper;
    private ZXingScannerView mScannerView;
    private Button turnOnQRCam, buttonHistroy, buttonInstruction;
    private View viewQRCam;
    private boolean cameraIsOn = false;
    private GsmCellLocation cellLocation;
    private Location location;
    private double latitude;
    private double longitude;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        mDatabaseHelper = new DatabaseHelper(this);
        viewQRCam = (View) findViewById(R.id.view);
        turnOnQRCam = (Button) findViewById(R.id.TurnOnQRCam);
        buttonHistroy = (Button) findViewById(R.id.buttonHistory);
        buttonInstruction = (Button) findViewById(R.id.buttonInstruction);

        final TelephonyManager telMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        permission(telMgr, lm);


        turnOnQRCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRReader();
            }
        });

        buttonHistroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        buttonInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startQRReader() {
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);  // It's opensorce api, so it work only with setContentView(...)
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void handleResult(Result result) {

        String newEntry = result.getText();
        String cellLocationString = "";

        if (result.toString().length() != 0) {

            if (location != null) { //cellLocation != null || location != null
                try {
                    cellLocationString = latitude + " " + longitude;
                } catch (Exception e) {

                }
            }

            addData(newEntry, cellLocationString);
            mScannerView.stopCamera();
            cameraIsOn = false;

            finish();  //It's necessary to operate the buttons, after using setContentView(...) more than once in the same activity
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "QR code is broken", Toast.LENGTH_LONG).show();
        }
    }

    public void addData(String newEntry, String location) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //2016/11/16 12:08:43
        Date date = new Date();

        boolean insertData = mDatabaseHelper.addData(newEntry, location, dateFormat.format(date));

        if (insertData) {
            Toast.makeText(this, "Data Successfully Inserted!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    public void permission(TelephonyManager telMgr, LocationManager lM) {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            permission(telMgr, lM);
            return;
        }
        try {
            location = lM.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                lM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
            } else {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            cellLocation = (GsmCellLocation) telMgr.getCellLocation();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraIsOn) {
            mScannerView.stopCamera();
            setContentView(R.layout.activity_main);
            cameraIsOn = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraIsOn) {
            mScannerView.stopCamera();
            setContentView(R.layout.activity_main);
            cameraIsOn = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraIsOn) {
            mScannerView.stopCamera();
            setContentView(R.layout.activity_main);
            cameraIsOn = false;
        }
    }


}
