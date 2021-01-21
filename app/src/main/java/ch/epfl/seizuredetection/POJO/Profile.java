package ch.epfl.seizuredetection.POJO;

public class Profile {
    //private String userID;
    private String username;
    private String password;
    private float weight;
    private int height;
    //private String photoPath;

    public Profile(){

    }

    public Profile(String username, String password, int height, float weight) {
        this.username = username;
        this.password = password;
        this.weight = weight;
        this.height = height;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

   /*public String getPhotoPath() { return photoPath; }

   public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }*/

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
