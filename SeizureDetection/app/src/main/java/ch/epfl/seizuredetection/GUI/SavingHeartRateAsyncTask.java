package ch.epfl.seizuredetection.GUI;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.seizuredetection.Data.AppDatabase;
import ch.epfl.seizuredetection.Data.SensorDataEntity;

public class SavingHeartRateAsyncTask extends AsyncTask<List<Integer>, Void, Void> {

    private AppDatabase db;

    SavingHeartRateAsyncTask(AppDatabase db) {
        this.db = db;
    }

    @SafeVarargs
    @Override
    protected final Void doInBackground(List<Integer>... lists) {
        List<Integer> hrValueList = lists[0];
        List<SensorDataEntity> sensorDataEntityList = new ArrayList<SensorDataEntity>();
        for (Integer hrValue : hrValueList) {
            SensorDataEntity sensorData = new SensorDataEntity();
            sensorData.timestamp = System.nanoTime();
            sensorData.type = SensorDataEntity.HEART_RATE;
            sensorData.value = hrValue;
            sensorDataEntityList.add(sensorData);
        }
        db.sensorDataDAO().insertSensorDataEntityList(sensorDataEntityList);
        return null;
    }
}