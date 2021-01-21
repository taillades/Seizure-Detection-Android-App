package ch.epfl.seizuredetection.Data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ProfileEntity.class,SensorDataEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    //Abstract class for inheritance: you don't implement the methods but you
    // can extend this class and implement them and add other features

    //Dao to associate to the database and use the queries implemented
    public abstract ProfileDAO profileDAO();
    public abstract SensorDataDAO sensorDataDAO();

    //Instance of the database that will be used later
    private static AppDatabase INSTANCE;

    //Constructor of the class. It's "synchronized" to avoid that concurrent
    // threads corrupts the instance.
    public static synchronized AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "database").build();
        }
        return INSTANCE;
    }

    //Method to destroy the instance of the database
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
