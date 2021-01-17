package ch.epfl.seizuredetection.Data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProfileDAO {
    //Declare methods and actions for the db

    //Number of users
    @Query("SELECT COUNT(*) FROM " + ProfileEntity.table_name)
    int count();

    //Select everything
    @Query("SELECT * FROM " + ProfileEntity.table_name)
    List<ProfileEntity> getAllValues();

    //Insert
    @Insert
    long insert(ProfileEntity users);

    //Delete
    @Query("DELETE FROM " + ProfileEntity.table_name + " WHERE " + ProfileEntity.column_id + "= :id")
    int deleteById(long id); //Pasamos parámetro id

    //Update
    @Update
    int updateEntity(ProfileEntity obj);
}
