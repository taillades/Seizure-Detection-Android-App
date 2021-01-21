package ch.epfl.seizuredetection.Data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SensorDataDAO {
    //Implementation of the queries to use to access the database

    //Get all the values of a specific sensor type sorted by timestamp in descending order
    @Query("SELECT * FROM SensorDataEntity WHERE type = :sensorType ORDER BY timestamp DESC")
    List<SensorDataEntity> getAllValues(int sensorType);

    //Insert new data list into the database
    @Insert
    void insertSensorDataEntityList(List<SensorDataEntity> sensorDataEntityList);

    //Delete all the data
    @Query("DELETE FROM SensorDataEntity")
    void deleteAll();
}

