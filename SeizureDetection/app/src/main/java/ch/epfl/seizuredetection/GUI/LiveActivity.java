package ch.epfl.seizuredetection.GUI;

import android.Manifest;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.reactivestreams.Publisher;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ch.epfl.seizuredetection.Bluetooth.BluetoothLeService;
import ch.epfl.seizuredetection.Bluetooth.SampleGattAttributes;
import ch.epfl.seizuredetection.R;
import ch.epfl.seizuredetection.ml.CompressionNn0;

import static android.graphics.Color.RED;
import static android.graphics.Color.TRANSPARENT;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.errors.PolarInvalidArgument;
import polar.com.sdk.api.model.PolarAccelerometerData;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarEcgData;
import polar.com.sdk.api.model.PolarExerciseData;
import polar.com.sdk.api.model.PolarExerciseEntry;
import polar.com.sdk.api.model.PolarHrBroadcastData;
import polar.com.sdk.api.model.PolarHrData;
import polar.com.sdk.api.model.PolarOhrPPGData;
import polar.com.sdk.api.model.PolarOhrPPIData;
import polar.com.sdk.api.model.PolarSensorSetting;


public class LiveActivity extends AppCompatActivity {

    // Fields related to the Bluetooth connexion
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static int THREE_SEC_SIGNAL_LEN =741;
    private BluetoothLeService mBluetoothLeService;
    //private ServiceConnection mServiceConnection;
    private String mDeviceName; // Name of the device
    private String mDeviceAddress; // Address of the device
    private boolean mConnected; // True if device connected
    private ArrayList<Integer> hrArray = new ArrayList();
    // plot attributes
    private long startTime = System.currentTimeMillis() / 1000;
    private final static String TAG = LiveActivity.class.getSimpleName();
    // Firebase
    private DatabaseReference recordingRef;
    private String userID;
    private String recID;

    // Polar API
    private PolarBleApi api;
    private Disposable ecgDisposable = null;
    private String deviceId;


    //HR Plot
    private static XYPlot heartRatePlot;
    private static final int MIN_HR = 40; //Minimal heart rate value to display on the graph
    private static final int MAX_HR = 200; //Maximum heart rate value to display on the graph
    private static final int NUMBER_OF_POINTS = 50; //Number of data points to be displayed on the graph
    private XYplotSeriesList xyPlotSeriesList;
    public static final String HR_PLOT = "HR Polar H7";

    /*private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
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
                Integer hr = intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0);
                displayData(hr);
                hrArray.add(hr);

                // Update PLOT
                xyPlotSeriesList.updateSeries(HR_PLOT, hr);
                XYSeries hrSeries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList
                        (HR_PLOT), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT);
                LineAndPointFormatter formatter = xyPlotSeriesList.getFormatterFromList
                        (HR_PLOT);

                heartRatePlot.clear();
                heartRatePlot.addSeries(hrSeries, formatter);
                heartRatePlot.redraw();
            }


        }
    };*/
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
        //deviceId = getIntent().getStringExtra("id");
        deviceId = "CF4F6013";

        if (Build.VERSION.SDK_INT >= 23) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        api = PolarBleApiDefaultImpl.defaultImplementation(this,
                PolarBleApi.FEATURE_POLAR_SENSOR_STREAMING |
                        PolarBleApi.FEATURE_BATTERY_INFO |
                        PolarBleApi.FEATURE_DEVICE_INFO |
                        PolarBleApi.FEATURE_HR);
        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean b) {
                Log.d(TAG, "BluetoothStateChanged " + b);
            }

            @Override
            public void deviceConnected(@NonNull PolarDeviceInfo s) {
                Log.d(TAG, "Device connected " + s.deviceId);
                Toast.makeText(LiveActivity.this, R.string.connected, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceConnecting(@NonNull PolarDeviceInfo polarDeviceInfo) {

            }

            @Override
            public void deviceDisconnected(@NonNull PolarDeviceInfo s) {
                Log.d(TAG, "Device disconnected " + s);
            }

            @Override
            public void ecgFeatureReady(@NonNull String s) {
                Log.d(TAG, "ECG Feature ready " + s);
                streamECG();
            }

            @Override
            public void accelerometerFeatureReady(@NonNull String s) {
                Log.d(TAG, "ACC Feature ready " + s);
            }

            @Override
            public void ppgFeatureReady(@NonNull String s) {
                Log.d(TAG, "PPG Feature ready " + s);
            }

            @Override
            public void ppiFeatureReady(@NonNull String s) {
                Log.d(TAG, "PPI Feature ready " + s);
            }

            @Override
            public void biozFeatureReady(@NonNull String s) {

            }

            @Override
            public void hrFeatureReady(@NonNull String s) {
                Log.d(TAG, "HR Feature ready " + s);

            }

            @Override
            public void disInformationReceived(@NonNull String s, @NonNull UUID u, @NonNull String s1) {
                if (u.equals(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"))) {
                    String msg = "Firmware: " + s1.trim();
                    Log.d(TAG, "Firmware: " + s + " " + s1.trim());
                    //textViewFW.append(msg + "\n");
                }
            }

            @Override
            public void batteryLevelReceived(@NonNull String s, int i) {
                String msg = "ID: " + s + "\nBattery level: " + i;
                Log.d(TAG, "Battery level " + s + " " + i);
                Toast.makeText(LiveActivity.this, msg, Toast.LENGTH_LONG).show();
                //textViewFW.append(msg + "\n");
            }

            @Override
            public void hrNotificationReceived(@NonNull String s, @NonNull PolarHrData polarHrData) {
                Log.d(TAG, "HR " + polarHrData.hr);
                displayData(polarHrData.hr);
                hrArray.add(polarHrData.hr);

                // Update PLOT
                xyPlotSeriesList.updateSeries(HR_PLOT, polarHrData.hr);
                XYSeries hrSeries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList
                        (HR_PLOT), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT);
                LineAndPointFormatter formatter = xyPlotSeriesList.getFormatterFromList
                        (HR_PLOT);

                heartRatePlot.clear();
                heartRatePlot.addSeries(hrSeries, formatter);
                heartRatePlot.redraw();
                //textViewHR.setText(String.valueOf(polarHrData.hr));
            }

            @Override
            public void polarFtpFeatureReady(@NonNull String s) {
                Log.d(TAG, "Polar FTP ready " + s);
            }
        });

        try {
            api.connectToDevice(deviceId);
        } catch (PolarInvalidArgument a) {
            a.printStackTrace();
        }


        Button stopRecording = findViewById(R.id.stopRecording);
        stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LiveActivity.this, "Recording stopped", Toast.LENGTH_SHORT).show();
                // Divide the signal
                int i=1;
                while(hrArray.toArray().length > THREE_SEC_SIGNAL_LEN*i++) {
                    // Compress the signal
                    float[] compressedSignal =compressor((ArrayList<Integer>) hrArray.subList(THREE_SEC_SIGNAL_LEN*(i-1),THREE_SEC_SIGNAL_LEN*i));
                    // Upload everything in Firebase
                    recordingRef.child("hr_compressed_data ").setValue(compressedSignal);
                }
            }
        });

        Intent intentFromRec = getIntent();
        userID = intentFromRec.getStringExtra(EditProfileActivity.USER_ID);
        recID = intentFromRec.getStringExtra(MainActivity.RECORDING_ID);
/*
        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
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
        xyPlotSeriesList.initializeSeriesAndAddToList(HR_PLOT, MIN_HR, NUMBER_OF_POINTS,
                formatter);
        XYSeries HRseries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(HR_PLOT),
                SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT);
        heartRatePlot.clear();
        heartRatePlot.addSeries(HRseries, formatter);
        heartRatePlot.redraw();
    }

    @Override
    protected void onResume() {
        super.onResume();
    /*    registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
/*
        unregisterReceiver(mGattUpdateReceiver);
*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private float[] compressor(ArrayList<Integer> input_sig) {
        try {
            int[] int_input_signal = ArrayUtils.toPrimitiveArray(Arrays.asList(input_sig.toArray(new Integer[0])));
            float[] input_signal = {};
            int sum = 0;
            long variance = 0;
            float[] x = {};
            for(int i=0; i<int_input_signal.length;i++){
                input_signal[i]=(float) int_input_signal[i];
                sum += int_input_signal[i];
                x[i]= (float) i;
                variance += Math.pow(int_input_signal[i],2);
            }
            float mean = sum/int_input_signal.length;
            float std = (float) Math.sqrt(variance);
            for(int i=0; i<int_input_signal.length;i++){
                input_signal[i]= (input_signal[i]-mean)/std; // signal standardization
            }

            //remove trend()
            input_signal = detrend(x,input_signal);

            Context context=getApplicationContext();
            CompressionNn0 model = CompressionNn0.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 768}, DataType.FLOAT32);
            inputFeature0.loadArray(input_signal);
            // Runs model inference and gets result.
            CompressionNn0.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            // Releases model resources if no longer used.
            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exception
            Toast.makeText(LiveActivity.this, "Failed to compress the signal", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static float[] detrend(float[] x, float[] y) {

        // TO DO !!!!

     /*   if (x.length != y.length)
            throw new IllegalArgumentException("The x and y data elements needs to be of the same length");

        SimpleRegression regression = new SimpleRegression();

        for (int i = 0; i < x.length; i++) {
            regression.addData(x[i], y[i]);
        }

        double slope = regression.getSlope();
        double intercept = regression.getIntercept();

        for (int i = 0; i < x.length; i++) {
            //y -= intercept + slope * x
            y[i] -= intercept + (x[i] * slope);
        }*/
        return y;
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

    public void streamECG() {
        if (ecgDisposable == null) {
            ecgDisposable =
                    api.requestEcgSettings(deviceId)
                            .toFlowable()
                            .flatMap((Function<PolarSensorSetting, Publisher<PolarEcgData>>) sensorSetting -> api.startEcgStreaming(deviceId, sensorSetting.maxSettings()))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    polarEcgData -> {
                                        Log.d(TAG, "ecg update");
                                        for (Integer data : polarEcgData.samples) {
                                            //plotter.sendSingleSample((float) ((float) data / 1000.0));
                                            Toast.makeText(LiveActivity.this, "sendSignSample", Toast.LENGTH_SHORT);
                                        }
                                    },
                                    throwable -> {
                                        Log.e(TAG,
                                                "" + throwable.getLocalizedMessage());
                                        ecgDisposable = null;
                                    },
                                    () -> Log.d(TAG, "complete")
                            );
        } else {
            // NOTE stops streaming if it is "running"
            ecgDisposable.dispose();
            ecgDisposable = null;
        }
    }
}