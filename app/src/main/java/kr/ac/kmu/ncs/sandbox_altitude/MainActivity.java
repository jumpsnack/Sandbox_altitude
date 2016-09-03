package kr.ac.kmu.ncs.sandbox_altitude;

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    public static boolean WEB_SERVICE_FETCHING = false;
    public static float THIS_MSLP = -1;

    TextView tvAltitude;
    TextView tvLat;
    TextView tvLng;

    SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAltitude = (TextView) findViewById(R.id.tv_altitude);
        tvLat = (TextView) findViewById(R.id.tv_lat);
        tvLng = (TextView) findViewById(R.id.tv_lng);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);

        } catch (SecurityException e) {
            Log.w("Security check", "failed");
        }


        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "권한허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {
                Toast.makeText(MainActivity.this, "권한거부\n" + arrayList.toString(), Toast.LENGTH_LONG).show();
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("Map사용을 위한 권한 필요")
                .setDeniedCloseButtonText("수동으로 권한을 설정해주세요")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.INTERNET
                        , Manifest.permission.ACCESS_NETWORK_STATE)
                .check();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.w("Altitude", "Has been checked");
//        tvAltitude.setText(location.getAltitude() + "");
        tvLat.setText(location.getLatitude() + "");
        tvLng.setText(location.getLongitude() + "");

        if(!WEB_SERVICE_FETCHING){
            WEB_SERVICE_FETCHING = true;
//            new Meter().execute(location.getLatitude(), location.getLongitude());
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        String newStatus = "";
        switch (i) {
            case LocationProvider.OUT_OF_SERVICE:
                newStatus = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                newStatus = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                newStatus = "AVAILABLE";
                break;
        }
        String msg = String.format(getResources().getString(R.string.provider_disabled), s, newStatus);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderEnabled(String s) {
        String msg = String.format(getResources().getString(R.string.provider_enabled), s);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        String msg = String.format(getResources().getString(R.string.provider_disabled), s);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float[] values = sensorEvent.values;
//            tvAltitude.setText(44300*(1-Math.pow(values[0]/1013.25, 1/5.255))-30+"");
            tvAltitude.setText(sensorManager.getAltitude((float)1011, values[0])+"");
//            tvAltitude.setText(sensorManager.getAltitude(sensorManager.PRESSURE_STANDARD_ATMOSPHERE, values[0])+"");
//            tvAltitude.setText(sensorManager.getAltitude(THIS_MSLP, values[0])+"");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}