package com.stiliyana.beaconproject.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stiliyana.beaconproject.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.distance.CurveFittedDistanceCalculator;
import org.altbeacon.beacon.distance.DistanceCalculator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String ISO_8601_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private BeaconsRecyclerViewAdapter beaconsRecyclerViewAdapter;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner scanner;
    boolean searchStarted = true;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        gson = new Gson();
        setSupportActionBar(toolbar);
        setUpViews(toolbar);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        setUpRecyclerView();
    }

    public String getCurrentTime() {
        //date output format
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    private void initialise() {
        scanner.startScan(Collections.<ScanFilter>emptyList(), new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mCallback);
        searchStarted = false;
    }

    public void askForPermissionsIfPossible() {
        if (!permissionsGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else initialise();
    }

    private Boolean permissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                initialise();
            } else {
                // User refused to grant permission. You can add AlertDialog here
                Toast.makeText(this, "You didn't give permission to access device location", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void setUpViews(Toolbar toolbar) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Snackbar snackbarSearchStopped = Snackbar.make(view, "Beacon Search stopped", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null);
                Snackbar snackbarSearchStarted = Snackbar.make(view, "Beacon Search started", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null);
                if (searchStarted) {
                    askForPermissionsIfPossible();
                    snackbarSearchStarted.show();
                    snackbarSearchStopped.dismiss();
                } else {
                    snackbarSearchStarted.dismiss();
                    snackbarSearchStopped.show();
                    scanner.stopScan(mCallback);
                    searchStarted = true;
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUpRecyclerView() {
        beacons = getDataFromSharedPreferences();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        beaconsRecyclerViewAdapter = new BeaconsRecyclerViewAdapter(beacons);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(beaconsRecyclerViewAdapter);
    }

    ScanCallback mCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null && result.getDevice() != null && result.getScanRecord() != null) {
                appendDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                beaconsRecyclerViewAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            if (results != null) {
                for (ScanResult result : results) {
                    if (result != null && result.getDevice() != null && result.getScanRecord() != null) {
                        appendDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                        beaconsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
        }
    };

    List<MyBeacon> beacons = new ArrayList<>();

    public void appendDevice(BluetoothDevice bluetoothDevice, int scanResult, byte[] scanRecord) {
        if ("OnyxBeacon".equals(bluetoothDevice.getName())) {
            BeaconParser beaconParser;
            beaconParser = new BeaconParser();
            //Random Layout
            beaconParser.setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24");
            //Default data for Nexus 5
            DistanceCalculator distanceCalculator = new CurveFittedDistanceCalculator(0.1820634, 0.8229884, 6.6525179);
            Beacon.setDistanceCalculator(distanceCalculator);
            Beacon beacon = beaconParser.fromScanData(scanRecord, scanResult, bluetoothDevice);
            Log.d(TAG, "appendDevice: " + beacon.toString());
            if (beacons.isEmpty()) {
                MyBeacon myBeacon = new MyBeacon();
                myBeacon.setBeacon(beacon);
                myBeacon.setDateTime(getCurrentTime());
                beacons.add(myBeacon);
            } else {
                boolean exist = false;
                for (int i = 0; i < beacons.size(); i++) {
                    if (beacon.getBluetoothAddress().equals(beacons.get(i).getBeacon().getBluetoothAddress())) {
                        MyBeacon myBeacon = new MyBeacon();
                        myBeacon.setBeacon(beacon);
                        myBeacon.setDateTime(getCurrentTime());
                        beacons.set(i, myBeacon);
                        exist = true;
                    }
                }
                if (exist == false) {
                    MyBeacon myBeacon = new MyBeacon();
                    myBeacon.setBeacon(beacon);
                    myBeacon.setDateTime(getCurrentTime());
                    beacons.add(myBeacon);
                }
            }
            storeDataInSharedPreferences();
        }
    }

    private void storeDataInSharedPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sp.edit();
        String jsonText = gson.toJson(beacons);
        prefsEditor.putString("key", jsonText);
        prefsEditor.commit();
    }

    private List<MyBeacon> getDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonText = sharedPreferences.getString("key", null);
        List<MyBeacon> beacons = new ArrayList<>();
        if (jsonText != null) {
            beacons = gson.fromJson(jsonText, new TypeToken<List<MyBeacon>>() {
            }.getType());
        }
        return beacons;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
