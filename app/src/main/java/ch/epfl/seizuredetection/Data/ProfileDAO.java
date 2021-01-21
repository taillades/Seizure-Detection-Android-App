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

    //Select pwd from email
    @Query("SELECT password FROM " + ProfileEntity.table_name + " WHERE email = :email")
    String getPwd(String email);

    //Select id from given email
    @Query(" SELECT " + ProfileEntity.column_id + " FROM " + ProfileEntity.table_name + " WHERE email = :email")
    int getId(String email);

    //Select pwd from column
    @Query("SELECT password FROM " + ProfileEntity.table_name+ " WHERE " + ProfileEntity.column_id + "= :id")
    String getPassword(int id);

    //Select height from column
    @Query("SELECT height FROM " + ProfileEntity.table_name+ " WHERE " + ProfileEntity.column_id + "= :id")
    int getHeight(int id);

    //Select weight from column
    @Query("SELECT weight FROM " + ProfileEntity.table_name+ " WHERE " + ProfileEntity.column_id + "= :id")
    float getWeight(int id);

    //Insert
    @Insert
    long insert(ProfileEntity users); //1 correct, 0 fail

    //Delete
    @Query("DELETE FROM " + ProfileEntity.table_name + " WHERE " + ProfileEntity.column_id + "= :id")
    int deleteById(long id); //Pasamos parámetro id

    //Update password
    @Query("UPDATE " + ProfileEntity.table_name + " SET password = :pwd WHERE " + ProfileEntity.column_id + "= :id")
     void updatePassword(String pwd, int id);

    //Update email
    @Query("UPDATE " + ProfileEntity.table_name + " SET email = :email WHERE " + ProfileEntity.column_id + "= :id")
    void updateEmail(String email, int id);

    //Update weight
    @Query("UPDATE " + ProfileEntity.table_name + " SET weight = :weight WHERE " + ProfileEntity.column_id + "= :id")
    void updateWeight(float weight, int id);

    //Update height
    @Query("UPDATE " + ProfileEntity.table_name + " SET height = :height WHERE " + ProfileEntity.column_id + "= :id")
    void updateHeight(int height, int id);

    //Update @Query("UPDATE" table_name SET text = :sText WHERE ID =:sID") void update(int sID, String sText)
}
