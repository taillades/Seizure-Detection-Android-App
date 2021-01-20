package ch.epfl.seizuredetection.Data;

import android.provider.BaseColumns;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = ProfileEntity.table_name)
public class ProfileEntity {
    public final static String table_name ="profiles";
    public final static String column_name = "name"; //Column name
    public final static String column_id = BaseColumns._ID; //Name of the ID of the column

    // Primary key to access the row of SQLite table for the entity SensorData
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = column_id) //index so that values don't get repeated - we set the name column_id for it
    public long id;

    //Different columns for different attributes of the entity
    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "weight")
    public float weight;

    @ColumnInfo(name = "height")
    public int height;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ProfileEntity() {
    }

    public ProfileEntity(long id, String email, String password, float weight, int height) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.weight = weight;
        this.height = height;
    }


}
