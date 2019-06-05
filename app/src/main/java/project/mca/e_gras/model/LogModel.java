package project.mca.e_gras.model;

import com.google.gson.annotations.SerializedName;

public class LogModel {

    @SerializedName("NAME")
    private String name;

    @SerializedName("GRNNO")
    private String grnNo;

    @SerializedName("ACTIVITY")
    private String activity;

    @SerializedName("DATETIME")
    private String dateTime;

    public LogModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrnNo() {
        return grnNo;
    }

    public void setGrnNo(String grnNo) {
        this.grnNo = grnNo;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "LogModel{" +
                "name='" + name + '\'' +
                ", grnNo='" + grnNo + '\'' +
                ", activity='" + activity + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
