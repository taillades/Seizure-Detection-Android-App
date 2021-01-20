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

    //Select password from specific profile
    @Query("SELECT password FROM " + ProfileEntity.table_name + " WHERE email = :email")
    String getPwd(String email);

    //Select height from specific profile
    @Query("SELECT height FROM " + ProfileEntity.table_name + " WHERE email = :email")
    int getHeight(String email);

    //Select weight from specific profile
    @Query("SELECT weight FROM " + ProfileEntity.table_name + " WHERE email = :email")
    Float getWeight(String email);

    //Insert
    @Insert
    long insert(ProfileEntity users); //1 correct, 0 fail

    //Delete
    @Query("DELETE FROM " + ProfileEntity.table_name + " WHERE " + ProfileEntity.column_id + "= :id")
    int deleteById(long id); //Pasamos parámetro id

    //Update
    @Update
    int updateEntity(ProfileEntity obj);

    //Update @Query("UPDATE" table_name SET text = :sText WHERE ID =:sID") void update(int sID, String sText)
}
