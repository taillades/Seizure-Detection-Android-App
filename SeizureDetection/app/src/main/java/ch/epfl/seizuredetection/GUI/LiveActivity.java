package ch.epfl.seizuredetection.GUI;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ch.epfl.seizuredetection.Bluetooth.BluetoothLeService;
import ch.epfl.seizuredetection.Bluetooth.SampleGattAttributes;
import ch.epfl.seizuredetection.R;

import static android.graphics.Color.RED;
import static android.graphics.Color.TRANSPARENT;



public class LiveActivity extends AppCompatActivity {

    // Fields related to the Bluetooth connexion
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private BluetoothLeService mBluetoothLeService;
    //private ServiceConnection mServiceConnection;
    private String mDeviceName; // Name of the device
    private String mDeviceAddress; // Address of the device
    private boolean mConnected; // True if device connected
    // plot attributes
    private long startTime = System.currentTimeMillis() / 1000;
    private final static String TAG = LiveActivity.class.getSimpleName();
    // Firebase
    private DatabaseReference recordingRef;
    private String userID;
    private String recID;




    //HR Plot
    private static XYPlot heartRatePlot;
    private static final int MIN_HR = 40; //Minimal heart rate value to display on the graph
    private static final int MAX_HR = 200; //Maximum heart rate value to display on the graph
    private static final int NUMBER_OF_POINTS = 50; //Number of data points to be displayed on the graph
    private XYplotSeriesList xyPlotSeriesList;
    public static final String HR_PLOT_WATCH = "HR Smart Watch";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                registerHeartRateService(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0));
            }
        }
    };
    // TODO: editar esta función para que haga display de los datos
    private void displayData(int intExtra) {

        TextView txtBpm = findViewById(R.id.bpm);
        txtBpm.setText(String.valueOf(intExtra));
        float time = System.currentTimeMillis() / 1000 - startTime;
/*        HRseriesBelt.addLast(time, intExtra);
        while (HRseriesBelt.size() > 0 && (time - HRseriesBelt.getX(0).longValue()) > NUMBER_OF_SECONDS) {
            HRseriesBelt.removeFirst();
            heartRatePlot.setDomainBoundaries(0, 0, BoundaryMode.AUTO);
        }

        heartRatePlot.redraw();*/

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent intentFromRec = getIntent();
        userID = intentFromRec.getStringExtra(EditProfileActivity.USER_ID);
        recID = intentFromRec.getStringExtra(MainActivity.RECORDING_ID);

        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED || checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED || checkSelfPermission("android.permission.INTERNET") == PackageManager.PERMISSION_DENIED)) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"}, 0);
        }

        //Configure HR Plot
        heartRatePlot = findViewById(R.id.HRplot);
        configurePlot();

        //Initialize plot
        xyPlotSeriesList = new XYplotSeriesList();
        LineAndPointFormatter formatter = new LineAndPointFormatter(RED, TRANSPARENT,
                TRANSPARENT, null);
        formatter.getLinePaint().setStrokeWidth(8);
        xyPlotSeriesList.initializeSeriesAndAddToList(HR_PLOT_WATCH, MIN_HR, NUMBER_OF_POINTS,
                formatter);
        XYSeries HRseries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(HR_PLOT_WATCH),
                SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT_WATCH);
        heartRatePlot.clear();
        heartRatePlot.addSeries(HRseries, formatter);
        heartRatePlot.redraw();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    private void configurePlot() {
        // Get background color from Theme
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        int backgroundColor = typedValue.data;
        // Set background colors
        heartRatePlot.setPlotMargins(0, 0, 0, 0);
        heartRatePlot.getBorderPaint().setColor(backgroundColor);
        heartRatePlot.getBackgroundPaint().setColor(backgroundColor);
        heartRatePlot.getGraph().getBackgroundPaint().setColor(backgroundColor);
        heartRatePlot.getGraph().getGridBackgroundPaint().setColor(backgroundColor);
        // Set the grid color
        heartRatePlot.getGraph().getRangeGridLinePaint().setColor(Color.DKGRAY);
        heartRatePlot.getGraph().getDomainGridLinePaint().setColor(Color.DKGRAY);
        // Set the origin axes colors
        heartRatePlot.getGraph().getRangeOriginLinePaint().setColor(Color.DKGRAY);
        heartRatePlot.getGraph().getDomainOriginLinePaint().setColor(Color.DKGRAY);
        // Set the XY axis boundaries and step values
        heartRatePlot.setRangeBoundaries(MIN_HR, MAX_HR, BoundaryMode.FIXED);
        heartRatePlot.setDomainBoundaries(0, NUMBER_OF_POINTS - 1, BoundaryMode.FIXED);
        heartRatePlot.setRangeStepValue(9); // 9 values 40 60 ... 200
        heartRatePlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new
                DecimalFormat("#")); // Force the Axis to be integer
        heartRatePlot.setRangeLabel(getString(R.string.heart_rate));

        // Get recording information from Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference profileGetRef = database.getReference("profiles");
        recordingRef = profileGetRef.child(userID).child("recordings").child(recID);

        recordingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /*TextView exerciseDatetime = findViewById(R.id.exerciseDateTimeLive);
                Long datetime = Long.parseLong(dataSnapshot.child("datetime").getValue().toString());
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
                exerciseDatetime.setText(formatter.format(new Date(datetime)));*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void registerHeartRateService(
            List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic
                    gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                // Find heart rate measurement (0x2A37)
                if (SampleGattAttributes.lookup(uuid, "unknown")
                        .equals("Heart Rate Measurement")) {
                    Log.i(TAG, "Registering for HR measurement");
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                }
            }
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public void stopRecording(View view){


    }
}